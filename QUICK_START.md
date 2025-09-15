# 快速开始指南

本指南将帮助您在5分钟内快速部署科大讯飞WebAPI后端服务，为前端SDK提供签名生成功能。

## 前置条件

- Java 8 或更高版本
- Maven 3.6 或更高版本
- 科大讯飞开发者账号和API密钥
- 现代操作系统（Windows/macOS/Linux）

## 1. 获取项目

### 克隆项目
```bash
git clone https://github.com/isingerw/xfyun-webapi.git
cd xfyun-webapi
```

### 检查环境
```bash
# 检查Java版本
java -version

# 检查Maven版本
mvn -version
```

## 2. 配置API密钥

### 编辑配置文件
编辑 `src/main/resources/application-dev.yml`：

```yaml
xfyun:
  iat:
    app-id: your-iat-app-id
    api-key: your-iat-api-key
    api-secret: your-iat-api-secret
  tts:
    app-id: your-tts-app-id
    api-key: your-tts-api-key
    api-secret: your-tts-api-secret
  rtasr:
    app-id: your-rtasr-app-id
    api-key: your-rtasr-api-key
  dts:
    app-id: your-dts-app-id
    api-key: your-dts-api-key
    api-secret: your-dts-api-secret
```

### 获取API密钥
1. 访问 [科大讯飞开放平台](https://www.xfyun.cn/)
2. 注册并登录账号
3. 创建应用获取API密钥
4. 将密钥填入配置文件

## 3. 启动服务

### 开发环境启动
```bash
mvn spring-boot:run
```

### 生产环境启动
```bash
# 使用环境变量
export XFyun_IAT_APP_ID=your-iat-app-id
export XFyun_IAT_API_KEY=your-iat-api-key
export XFyun_IAT_API_SECRET=your-iat-api-secret
# ... 其他环境变量

mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 打包部署
```bash
# 打包
mvn clean package

# 运行JAR包
java -jar target/xfyun-webapi-1.2.5.jar
```

## 4. 验证服务

### 健康检查
```bash
curl http://localhost:8080/actuator/health
```

预期响应：
```json
{
  "status": "UP"
}
```

### 测试IAT签名接口
```bash
curl http://localhost:8080/api/v1/xfyun/sign/iat
```

预期响应：
```json
{
  "errorCode": 0,
  "data": {
    "url": "wss://iat-api.xfyun.cn/v2/iat?authorization=...&date=...&host=iat-api.xfyun.cn",
    "appId": "your-app-id"
  }
}
```

### 测试TTS签名接口
```bash
curl http://localhost:8080/api/v1/xfyun/sign/tts
```

### 测试RTASR签名接口
```bash
curl http://localhost:8080/api/v1/xfyun/sign/rtasr
```

### 测试DTS签名接口
```bash
curl http://localhost:8080/api/v1/xfyun/sign/dts/create
curl http://localhost:8080/api/v1/xfyun/sign/dts/query
```

## 5. 集成前端SDK

### 安装前端SDK
```bash
npm install xfyun-webapi-sdk
```

### 配置前端
```typescript
import { useIat } from 'xfyun-webapi-sdk';

const { status, error, open, sendFrame, close } = useIat({
  serverBase: 'http://localhost:8080', // 后端服务地址
  getAuthCode: () => 'your-auth-token', // 可选
  business: {
    language: 'zh_cn',
    vad_eos: 2000,
    ptt: 1
  },
  onResult: (text, isFinal) => {
    console.log('识别结果:', text);
  }
});
```

## 6. Docker部署

### 创建Dockerfile
```dockerfile
FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 复制JAR文件
COPY target/xfyun-webapi-1.2.5.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 构建和运行
```bash
# 构建镜像
docker build -t xfyun-webapi:1.2.5 .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e XFyun_IAT_APP_ID=your-iat-app-id \
  -e XFyun_IAT_API_KEY=your-iat-api-key \
  -e XFyun_IAT_API_SECRET=your-iat-api-secret \
  --name xfyun-webapi \
  xfyun-webapi:1.2.5
```

## 7. 生产环境配置

### 环境变量配置
```bash
# 设置环境变量
export XFyun_IAT_APP_ID=your-iat-app-id
export XFyun_IAT_API_KEY=your-iat-api-key
export XFyun_IAT_API_SECRET=your-iat-api-secret
export XFyun_TTS_APP_ID=your-tts-app-id
export XFyun_TTS_API_KEY=your-tts-api-key
export XFyun_TTS_API_SECRET=your-tts-api-secret
export XFyun_RTASR_APP_ID=your-rtasr-app-id
export XFyun_RTASR_API_KEY=your-rtasr-api-key
export XFyun_DTS_APP_ID=your-dts-app-id
export XFyun_DTS_API_KEY=your-dts-api-key
export XFyun_DTS_API_SECRET=your-dts-api-secret

# 启动服务
java -jar target/xfyun-webapi-1.2.5.jar --spring.profiles.active=prod
```

### 配置文件示例
```yaml
# application-prod.yml
server:
  port: 8080

spring:
  profiles:
    active: prod

xfyun:
  iat:
    app-id: ${XFyun_IAT_APP_ID}
    api-key: ${XFyun_IAT_API_KEY}
    api-secret: ${XFyun_IAT_API_SECRET}
  tts:
    app-id: ${XFyun_TTS_APP_ID}
    api-key: ${XFyun_TTS_API_KEY}
    api-secret: ${XFyun_TTS_API_SECRET}
  rtasr:
    app-id: ${XFyun_RTASR_APP_ID}
    api-key: ${XFyun_RTASR_API_KEY}
  dts:
    app-id: ${XFyun_DTS_APP_ID}
    api-key: ${XFyun_DTS_API_KEY}
    api-secret: ${XFyun_DTS_API_SECRET}

logging:
  level:
    com.xfyun.webapi: INFO
    org.springframework.web: WARN
```

## 8. 监控和日志

### 健康检查端点
```bash
# 基础健康检查
curl http://localhost:8080/actuator/health

# 详细信息
curl http://localhost:8080/actuator/health/details
```

### 指标监控
```bash
# 应用指标
curl http://localhost:8080/actuator/metrics

# JVM指标
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### 日志配置
```yaml
logging:
  level:
    com.xfyun.webapi: DEBUG
    org.springframework.web: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: logs/xfyun-webapi.log
    max-size: 10MB
    max-history: 30
```

## 9. 常见问题

### Q: 服务启动失败？
A: 检查Java版本、Maven配置和API密钥是否正确。

### Q: 签名生成失败？
A: 检查API密钥配置、网络连接和讯飞服务状态。

### Q: 端口被占用？
A: 修改application.yml中的server.port配置。

### Q: 内存不足？
A: 调整JVM参数：`java -Xms512m -Xmx1024m -jar app.jar`

### Q: 跨域问题？
A: 添加CORS配置或使用Nginx代理。

## 10. 性能优化

### JVM参数优化
```bash
java -server \
  -Xms512m \
  -Xmx1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar target/xfyun-webapi-1.2.5.jar
```

### 连接池配置
```yaml
server:
  tomcat:
    max-connections: 8192
    accept-count: 100
    threads:
      max: 200
      min-spare: 10
```

## 11. 安全配置

### HTTPS配置
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: PKCS12
    key-alias: tomcat
```

### 访问控制
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
```

## 12. 下一步

- 查看 [完整文档](README.md)
- 了解 [项目架构](ARCHITECTURE.md)
- 学习 [最佳实践](BEST_PRACTICES.md)
- 参与 [项目贡献](CONTRIBUTING.md)

## 13. 获取帮助

- 查看 [常见问题](README.md#常见问题)
- 提交 [Issue](https://github.com/isingerw/xfyun-webapi/issues)
- 发送邮件: zhangsingerw@gmail.com

---

恭喜！您已经成功部署了科大讯飞WebAPI后端服务。现在可以开始集成前端SDK了！
