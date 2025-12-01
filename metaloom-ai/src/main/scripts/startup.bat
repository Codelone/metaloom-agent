@echo off
setlocal EnableDelayedExpansion

REM Metaloom AI Application Windows启动脚本
REM Version: 1.0
REM Author: metaloom-team

title Metaloom AI Application

REM 设置变量
set APP_NAME=metaloom-ai
set APP_VERSION=1.0-SNAPSHOT
set JAR_NAME=%APP_NAME%-%APP_VERSION%.jar
set APP_DIR=%~dp0..
set JAR_FILE=%APP_DIR%\%JAR_NAME%
set LIB_DIR=%APP_DIR%\lib
set CONFIG_DIR=%APP_DIR%\config
set LOG_DIR=%APP_DIR%\logs
set PID_FILE=%APP_DIR%\%APP_NAME%.pid

REM 创建日志目录
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM 检查参数
if "%1"=="" goto usage
if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="status" goto status
if "%1"=="logs" goto logs
goto usage

:check_java
echo [INFO] 检查Java环境...

REM 检查JAVA_HOME
if defined JAVA_HOME (
    set JAVA_CMD=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_CMD=java
)

REM 检查Java是否可用
%JAVA_CMD% -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java 未找到，请安装 Java 17 或设置 JAVA_HOME
    exit /b 1
)

REM 检查Java版本
for /f "tokens=3" %%i in ('%JAVA_CMD% -version 2^>^&1 ^| findstr "version"') do (
    set JAVA_VERSION_STRING=%%i
    set JAVA_VERSION_STRING=!JAVA_VERSION_STRING:"=!
    for /f "tokens=1 delims=." %%j in ("!JAVA_VERSION_STRING!") do set JAVA_VERSION=%%j
)

if !JAVA_VERSION! LSS 17 (
    echo [ERROR] 需要 Java 17 或更高版本，当前版本: !JAVA_VERSION!
    exit /b 1
)

echo [INFO] Java 环境检查通过，版本: !JAVA_VERSION!
goto :eof

:check_files
echo [INFO] 检查应用文件...

if not exist "%JAR_FILE%" (
    echo [ERROR] 应用 JAR 文件不存在: %JAR_FILE%
    exit /b 1
)

if not exist "%LIB_DIR%" (
    echo [ERROR] 依赖库目录不存在: %LIB_DIR%
    exit /b 1
)

echo [INFO] 应用文件检查通过
goto :eof

:build_classpath
echo [INFO] 构建类路径...

set CLASSPATH=%JAR_FILE%

REM 添加配置目录到类路径
if exist "%CONFIG_DIR%" (
    set CLASSPATH=%CONFIG_DIR%;!CLASSPATH!
)

REM 添加lib目录下的所有jar到类路径
for %%f in ("%LIB_DIR%\*.jar") do (
    set CLASSPATH=!CLASSPATH!;%%f
)

echo [INFO] 类路径构建完成
goto :eof

:get_pid
set PID=
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    
    REM 检查进程是否存在
    tasklist /FI "PID eq !PID!" 2>nul | find "!PID!" >nul
    if errorlevel 1 (
        del "%PID_FILE%" 2>nul
        set PID=
    )
)
goto :eof

:start
call :get_pid
if defined PID (
    echo [WARN] 应用已在运行，PID: !PID!
    goto :eof
)

echo [INFO] 正在启动 %APP_NAME%...

call :check_java
if errorlevel 1 goto :eof

call :check_files
if errorlevel 1 goto :eof

call :build_classpath

REM 设置JVM参数
set JVM_OPTS=%JVM_OPTS% -server
set JVM_OPTS=%JVM_OPTS% -Xms512m -Xmx1024m
set JVM_OPTS=%JVM_OPTS% -XX:+UseG1GC
set JVM_OPTS=%JVM_OPTS% -XX:+UseStringDeduplication
set JVM_OPTS=%JVM_OPTS% -Djava.awt.headless=true
set JVM_OPTS=%JVM_OPTS% -Djava.security.egd=file:/dev/./urandom
set JVM_OPTS=%JVM_OPTS% -Dspring.config.additional-location=file:%CONFIG_DIR%/
set JVM_OPTS=%JVM_OPTS% -Dlogging.file.path=%LOG_DIR%

REM 设置应用参数
if not defined SPRING_PROFILES_ACTIVE set SPRING_PROFILES_ACTIVE=prod
set APP_OPTS=--spring.profiles.active=%SPRING_PROFILES_ACTIVE%

REM 启动应用
echo [INFO] 启动命令: %JAVA_CMD% %JVM_OPTS% -cp "%CLASSPATH%" com.metaloom.ai.MetaloomAiApplication %APP_OPTS%
start /B %JAVA_CMD% %JVM_OPTS% -cp "%CLASSPATH%" com.metaloom.ai.MetaloomAiApplication %APP_OPTS% > "%LOG_DIR%\application.out" 2>&1

REM 获取进程ID (Windows下比较复杂，这里简化处理)
timeout /t 3 /nobreak >nul

echo [SUCCESS] %APP_NAME% 启动成功
echo [INFO] 日志文件: %LOG_DIR%\application.out
echo [INFO] 应用地址: http://localhost:8080
echo [INFO] 健康检查: http://localhost:8080/api/actuator/health
goto :eof

:stop
call :get_pid
if not defined PID (
    echo [WARN] 应用未在运行
    goto :eof
)

echo [INFO] 正在停止 %APP_NAME%，PID: %PID%

REM 结束进程
taskkill /PID %PID% /F >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 停止进程失败
    goto :eof
)

del "%PID_FILE%" 2>nul
echo [INFO] %APP_NAME% 已停止
goto :eof

:restart
call :stop
timeout /t 2 /nobreak >nul
call :start
goto :eof

:status
call :get_pid
if defined PID (
    echo [INFO] %APP_NAME% 正在运行，PID: %PID%
    
    REM 检查健康状态
    curl -s http://localhost:8080/api/actuator/health >nul 2>&1
    if errorlevel 1 (
        echo [WARN] 健康检查: 异常
    ) else (
        echo [INFO] 健康检查: 正常
    )
) else (
    echo [WARN] %APP_NAME% 未在运行
)
goto :eof

:logs
if exist "%LOG_DIR%\application.out" (
    echo [INFO] 显示应用日志...
    type "%LOG_DIR%\application.out"
    echo.
    echo [INFO] 按 Ctrl+C 退出日志查看
    timeout /t -1 >nul
) else (
    echo [ERROR] 日志文件不存在: %LOG_DIR%\application.out
)
goto :eof

:usage
echo Metaloom AI Application Windows 启动脚本
echo.
echo 用法: %~nx0 {start^|stop^|restart^|status^|logs}
echo.
echo 命令说明:
echo   start   - 启动应用
echo   stop    - 停止应用
echo   restart - 重启应用
echo   status  - 查看应用状态
echo   logs    - 查看应用日志
echo.
echo 环境变量:
echo   JAVA_HOME              - Java安装目录
echo   JVM_OPTS               - JVM参数
echo   SPRING_PROFILES_ACTIVE - Spring激活的配置文件
echo.
echo 示例:
echo   %~nx0 start
echo   set SPRING_PROFILES_ACTIVE=dev ^& %~nx0 start
echo   set JVM_OPTS=-Xmx2g ^& %~nx0 start
echo.
pause
goto :eof