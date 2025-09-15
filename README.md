# 科大讯飞WebAPI后端服务

这是一个基于Spring Boot的科大讯飞WebAPI后端服务，提供讯飞各种AI服务的签名生成和API密钥管理功能。

## 功能特性

- **语音听写 (IAT)**: 提供语音转文字的签名生成
- **语音合成 (TTS)**: 提供文字转语音的签名生成
- **实时语音转写 (RTASR)**: 提供长时间语音转文字的签名生成
- **长文本语音合成 (DTS)**: 提供长文本语音合成的签名生成

## 技术栈

- Java 8+
- Spring Boot 2.7.18
- Maven 3.6+

## 快速开始

### 1. 环境要求

- JDK 8 或更高版本
- Maven 3.6 或更高版本

### 2. 配置API密钥

在 `src/main/resources/application.yml` 中配置您的科大讯飞API密钥：

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

### 3. 运行应用

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/xfyun-webapi-1.0.0.jar
```

### 4. 访问服务

应用启动后，可以通过以下地址访问：

- 服务地址: http://localhost:8080
- 健康检查: http://localhost:8080/actuator/health

## API接口

### 1. 语音听写 (IAT) 签名

```http
GET /api/v1/xfyun/sign/iat
Authorization: Bearer your-token (可选)
```

**响应示例:**
```json
{
  "errorCode": 0,
  "data": {
    "url": "wss://iat-api.xfyun.cn/v2/iat?authorization=...&date=...&host=iat-api.xfyun.cn",
    "appId": "your-app-id"
  }
}
```

### 2. 语音合成 (TTS) 签名

```http
GET /api/v1/xfyun/sign/tts
Authorization: Bearer your-token (可选)
```

**响应示例:**
```json
{
  "errorCode": 0,
  "data": {
    "url": "wss://tts-api.xfyun.cn/v2/tts?authorization=...&date=...&host=tts-api.xfyun.cn",
    "appId": "your-app-id"
  }
}
```

### 3. 实时语音转写 (RTASR) 签名

```http
GET /api/v1/xfyun/sign/rtasr
Authorization: Bearer your-token (可选)
```

**响应示例:**
```json
{
  "errorCode": 0,
  "data": {
    "url": "wss://rtasr.xfyun.cn/v1/ws?appid=...&ts=...&signa=...&pd=edu",
    "appId": "your-app-id",
    "ts": "1640995200",
    "signa": "your-signature"
  }
}
```

### 4. 长文本语音合成 (DTS) 创建任务签名

```http
GET /api/v1/xfyun/sign/dts/create
Authorization: Bearer your-token (可选)
```

**响应示例:**
```json
{
  "errorCode": 0,
  "data": {
    "url": "https://api-dx.xf-yun.com/v1/private/dts_create?authorization=...&date=...&host=api-dx.xf-yun.com",
    "appId": "your-app-id",
    "host": "api-dx.xf-yun.com",
    "date": "Mon, 01 Jan 2024 12:00:00 GMT",
    "authorization": "your-authorization"
  }
}
```

### 5. 长文本语音合成 (DTS) 查询任务签名

```http
GET /api/v1/xfyun/sign/dts/query
Authorization: Bearer your-token (可选)
```

**响应示例:**
```json
{
  "errorCode": 0,
  "data": {
    "url": "https://api-dx.xf-yun.com/v1/private/dts_query?authorization=...&date=...&host=api-dx.xf-yun.com",
    "appId": "your-app-id",
    "host": "api-dx.xf-yun.com",
    "date": "Mon, 01 Jan 2024 12:00:00 GMT",
    "authorization": "your-authorization"
  }
}
```

## 环境配置

### 开发环境

使用 `application-dev.yml` 配置文件：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 生产环境

使用 `application-prod.yml` 配置文件，支持环境变量：

```bash
export XFyun_IAT_APP_ID=your-app-id
export XFyun_IAT_API_KEY=your-api-key
export XFyun_IAT_API_SECRET=your-api-secret
# ... 其他环境变量

mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 项目结构

```
xfyun-webapi/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── xfyun/
│       │           └── webapi/
│       │               ├── XfyunWebapiApplication.java
│       │               ├── controller/
│       │               │   └── XfyunSignController.java
│       │               ├── service/
│       │               │   └── XfyunSignatureService.java
│       │               ├── config/
│       │               │   ├── XfyunConfigProperties.java
│       │               │   └── XfyunConfiguration.java
│       │               └── domain/
│       │                   └── Result.java
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           └── application-prod.yml
├── pom.xml
└── README.md
```

## 安全说明

1. **API密钥管理**: 所有API密钥都通过配置文件管理，生产环境建议使用环境变量
2. **加密功能**: 当前版本关闭了响应加密功能，如需启用请修改 `XfyunSignatureService` 中的 `ENABLE_ENCRYPTION` 常量
3. **授权头**: 所有接口都支持可选的 `Authorization` 头，用于后续的权限控制

## 许可证

本项目采用 MIT 许可证，详情请参阅 [LICENSE](LICENSE) 文件。

## 贡献

欢迎提交 Issue 和 Pull Request 来改进本项目。详细的贡献指南请参阅 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 相关项目

- **前端SDK**: [xfyun-webapi-sdk](https://github.com/isingerw/xfyun-webapi-sdk) - 科大讯飞WebAPI前端SDK
- **后端服务**: [xfyun-webapi](https://github.com/isingerw/xfyun-webapi) - 科大讯飞WebAPI后端服务

## 联系方式

如有问题，请通过以下方式联系：

- 邮箱: zhangsingerw@gmail.com
- 项目地址: https://github.com/isingerw/xfyun-webapi
- 问题反馈: https://github.com/isingerw/xfyun-webapi/issues
