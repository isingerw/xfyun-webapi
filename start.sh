#!/bin/bash

# 科大讯飞WebAPI后端服务启动脚本

echo "正在启动科大讯飞WebAPI服务..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装JDK 8或更高版本"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请先安装Maven 3.6或更高版本"
    exit 1
fi

# 设置环境变量
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}

echo "使用环境: $SPRING_PROFILES_ACTIVE"

# 编译并运行
echo "正在编译项目..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "编译成功，正在启动应用..."
    mvn spring-boot:run
else
    echo "编译失败，请检查代码"
    exit 1
fi
