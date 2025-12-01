# 基础镜像：使用 OpenJDK 17 运行时环境
FROM openjdk:17-jre-slim

# 维护者信息
LABEL maintainer="metaloom-team"
LABEL description="Metaloom MCP Server - Spring AI MCP tool provider service"
LABEL version="1.0-SNAPSHOT"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用用户（安全考虑）
RUN addgroup --system spring && adduser --system --group spring

# 创建应用目录
WORKDIR /app

# 复制jar文件（使用构建参数传递jar文件名）
ARG JAR_FILE=target/metaloom-mcp-server-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R spring:spring /app

# 切换到非root用户
USER spring:spring

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# 暴露端口
EXPOSE 8081

# JVM参数优化
ENV JAVA_OPTS="-server -Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}"]