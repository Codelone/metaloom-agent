# 基础镜像：使用 OpenJDK 17 运行时环境
FROM openjdk:17-jre-slim

# 维护者信息
LABEL maintainer="metaloom-team"
LABEL description="Metaloom AI Application - Spring AI based intelligent data analysis system (lib separation mode)"
LABEL version="1.0-SNAPSHOT"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用用户（安全考虑）
RUN addgroup --system spring && adduser --system --group spring

# 创建应用目录
WORKDIR /app

# 复制分发包并解压
ARG DIST_FILE=target/metaloom-ai-1.0-SNAPSHOT.tar.gz
COPY ${DIST_FILE} /tmp/app.tar.gz
RUN tar -xzf /tmp/app.tar.gz -C /app --strip-components=1 && \
    rm /tmp/app.tar.gz

# 或者分别复制文件（如果不使用分发包）
# COPY target/metaloom-ai-1.0-SNAPSHOT.jar .
# COPY target/lib lib/
# COPY src/main/resources/application*.yml config/

# 创建日志目录
RUN mkdir -p /app/logs && chown -R spring:spring /app

# 切换到非root用户
USER spring:spring

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# JVM参数优化
ENV JAVA_OPTS="-server -Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom"

# 启动应用（使用新的启动方式）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp '.:lib/*:config:metaloom-ai-1.0-SNAPSHOT.jar' com.metaloom.ai.MetaloomAiApplication --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]