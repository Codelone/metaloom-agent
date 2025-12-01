#!/bin/bash

# Metaloom MCP Server 启动脚本
# Version: 1.0
# Author: metaloom-team

# 设置变量
APP_NAME="metaloom-mcp-server"
APP_VERSION="1.0-SNAPSHOT"
JAR_NAME="${APP_NAME}-${APP_VERSION}.jar"
APP_DIR="$(cd "$(dirname "$0")/.." && pwd)"
JAR_FILE="${APP_DIR}/${JAR_NAME}"
LIB_DIR="${APP_DIR}/lib"
CONFIG_DIR="${APP_DIR}/config"
LOG_DIR="${APP_DIR}/logs"
PID_FILE="${APP_DIR}/${APP_NAME}.pid"

# 创建日志目录
mkdir -p "${LOG_DIR}"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') $1"
}

# 检查Java环境
check_java() {
    if [ -z "$JAVA_HOME" ]; then
        JAVA_CMD=java
    else
        JAVA_CMD="$JAVA_HOME/bin/java"
    fi
    
    if ! command -v $JAVA_CMD &> /dev/null; then
        log_error "Java 未找到，请安装 Java 17 或设置 JAVA_HOME"
        exit 1
    fi
    
    # 检查Java版本
    JAVA_VERSION=$($JAVA_CMD -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
    if [ "$JAVA_VERSION" -lt 17 ]; then
        log_error "需要 Java 17 或更高版本，当前版本: $JAVA_VERSION"
        exit 1
    fi
    
    log_info "Java 环境检查通过，版本: $JAVA_VERSION"
}

# 检查应用文件
check_files() {
    if [ ! -f "$JAR_FILE" ]; then
        log_error "应用 JAR 文件不存在: $JAR_FILE"
        exit 1
    fi
    
    if [ ! -d "$LIB_DIR" ]; then
        log_error "依赖库目录不存在: $LIB_DIR"
        exit 1
    fi
    
    log_info "应用文件检查通过"
}

# 构建类路径
build_classpath() {
    CLASSPATH="$JAR_FILE"
    
    # 添加配置目录到类路径
    if [ -d "$CONFIG_DIR" ]; then
        CLASSPATH="$CONFIG_DIR:$CLASSPATH"
    fi
    
    # 添加lib目录下的所有jar到类路径
    for jar in "$LIB_DIR"/*.jar; do
        if [ -f "$jar" ]; then
            CLASSPATH="$CLASSPATH:$jar"
        fi
    done
    
    log_info "类路径构建完成"
}

# 获取进程ID
get_pid() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "$PID"
        else
            rm -f "$PID_FILE"
            echo ""
        fi
    else
        echo ""
    fi
}

# 启动应用
start() {
    PID=$(get_pid)
    if [ -n "$PID" ]; then
        log_warn "应用已在运行，PID: $PID"
        return 1
    fi
    
    log_info "正在启动 $APP_NAME..."
    
    check_java
    check_files
    build_classpath
    
    # 设置JVM参数
    JVM_OPTS="${JVM_OPTS} -server"
    JVM_OPTS="${JVM_OPTS} -Xms256m -Xmx512m"
    JVM_OPTS="${JVM_OPTS} -XX:+UseG1GC"
    JVM_OPTS="${JVM_OPTS} -XX:+UseStringDeduplication"
    JVM_OPTS="${JVM_OPTS} -Djava.awt.headless=true"
    JVM_OPTS="${JVM_OPTS} -Djava.security.egd=file:/dev/./urandom"
    JVM_OPTS="${JVM_OPTS} -Dspring.config.additional-location=file:${CONFIG_DIR}/"
    JVM_OPTS="${JVM_OPTS} -Dlogging.file.path=${LOG_DIR}"
    
    # 设置应用参数
    APP_OPTS="--spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"
    
    # 启动应用
    nohup $JAVA_CMD $JVM_OPTS -cp "$CLASSPATH" com.metaloom.mcp.McpServerApplication $APP_OPTS \
        > "$LOG_DIR/application.out" 2>&1 &
    
    echo $! > "$PID_FILE"
    
    # 等待应用启动
    sleep 3
    PID=$(get_pid)
    if [ -n "$PID" ]; then
        log_info "$APP_NAME 启动成功，PID: $PID"
        log_info "日志文件: $LOG_DIR/application.out"
        log_info "应用地址: http://localhost:8081"
    else
        log_error "$APP_NAME 启动失败"
        return 1
    fi
}

# 停止应用
stop() {
    PID=$(get_pid)
    if [ -z "$PID" ]; then
        log_warn "应用未在运行"
        return 1
    fi
    
    log_info "正在停止 $APP_NAME，PID: $PID"
    
    # 优雅停止
    kill "$PID"
    
    # 等待进程结束
    for i in {1..30}; do
        if ! ps -p "$PID" > /dev/null 2>&1; then
            rm -f "$PID_FILE"
            log_info "$APP_NAME 已停止"
            return 0
        fi
        sleep 1
    done
    
    # 强制停止
    log_warn "优雅停止超时，强制终止进程"
    kill -9 "$PID"
    rm -f "$PID_FILE"
    log_info "$APP_NAME 已强制停止"
}

# 重启应用
restart() {
    stop
    sleep 2
    start
}

# 查看状态
status() {
    PID=$(get_pid)
    if [ -n "$PID" ]; then
        log_info "$APP_NAME 正在运行，PID: $PID"
        
        # 显示内存使用情况
        if command -v ps &> /dev/null; then
            MEMORY=$(ps -p "$PID" -o rss= | awk '{printf "%.1f MB", $1/1024}')
            log_info "内存使用: $MEMORY"
        fi
        
        # 检查健康状态
        if command -v curl &> /dev/null; then
            if curl -s http://localhost:8081/mcp/actuator/health > /dev/null; then
                log_info "健康检查: 正常"
            else
                log_warn "健康检查: 异常"
            fi
        fi
    else
        log_warn "$APP_NAME 未在运行"
        return 1
    fi
}

# 查看日志
logs() {
    if [ -f "$LOG_DIR/application.out" ]; then
        tail -f "$LOG_DIR/application.out"
    else
        log_error "日志文件不存在: $LOG_DIR/application.out"
        return 1
    fi
}

# 显示帮助信息
usage() {
    echo "用法: $0 {start|stop|restart|status|logs}"
    echo ""
    echo "命令说明:"
    echo "  start   - 启动应用"
    echo "  stop    - 停止应用"
    echo "  restart - 重启应用"
    echo "  status  - 查看应用状态"
    echo "  logs    - 查看应用日志(实时)"
    echo ""
    echo "环境变量:"
    echo "  JAVA_HOME            - Java安装目录"
    echo "  JVM_OPTS             - JVM参数"
    echo "  SPRING_PROFILES_ACTIVE - Spring激活的配置文件"
    echo ""
    echo "示例:"
    echo "  $0 start"
    echo "  SPRING_PROFILES_ACTIVE=dev $0 start"
    echo "  JVM_OPTS='-Xmx1g' $0 start"
}

# 主逻辑
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    logs)
        logs
        ;;
    *)
        usage
        exit 1
        ;;
esac

exit $?