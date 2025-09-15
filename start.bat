@echo off
REM 科大讯飞WebAPI后端服务启动脚本

echo 正在启动科大讯飞WebAPI服务...

REM 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请先安装JDK 8或更高版本
    pause
    exit /b 1
)

REM 检查Maven环境
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven环境，请先安装Maven 3.6或更高版本
    pause
    exit /b 1
)

REM 设置环境变量
if "%SPRING_PROFILES_ACTIVE%"=="" set SPRING_PROFILES_ACTIVE=dev

echo 使用环境: %SPRING_PROFILES_ACTIVE%

REM 编译并运行
echo 正在编译项目...
mvn clean compile

if %errorlevel% equ 0 (
    echo 编译成功，正在启动应用...
    mvn spring-boot:run
) else (
    echo 编译失败，请检查代码
    pause
    exit /b 1
)
