# 项目架构文档

## 概述

科大讯飞WebAPI后端服务是一个基于Spring Boot的微服务，专门为前端SDK提供安全的签名生成和API密钥管理功能。本文档详细描述了项目的整体架构、设计原则和核心组件。

## 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    前端应用层                                │
├─────────────────────────────────────────────────────────────┤
│  React  │  Vue  │  原生 JavaScript  │  移动端  │  其他客户端  │
├─────────────────────────────────────────────────────────────┤
│                    HTTP/HTTPS 请求                          │
├─────────────────────────────────────────────────────────────┤
│                    Spring Boot 应用层                       │
├─────────────────────────────────────────────────────────────┤
│  Controller  │  Service  │  Configuration  │  Domain       │
├─────────────────────────────────────────────────────────────┤
│                    业务逻辑层                               │
├─────────────────────────────────────────────────────────────┤
│  签名生成  │  密钥管理  │  加密处理  │  错误处理  │  日志记录   │
├─────────────────────────────────────────────────────────────┤
│                    数据访问层                               │
├─────────────────────────────────────────────────────────────┤
│  配置管理  │  环境变量  │  文件系统  │  外部API   │  缓存      │
├─────────────────────────────────────────────────────────────┤
│                   讯飞API服务                               │
├─────────────────────────────────────────────────────────────┤
│  IAT  │  TTS  │  RTASR  │  DTS  │  其他讯飞服务            │
└─────────────────────────────────────────────────────────────┘
```

## 核心设计原则

### 1. 单一职责原则
- 每个类只负责一个功能
- 签名生成与业务逻辑分离
- 配置管理与服务逻辑分离

### 2. 开闭原则
- 对扩展开放，对修改关闭
- 易于添加新的API服务支持
- 插件化的签名算法

### 3. 依赖倒置原则
- 依赖抽象而非具体实现
- 接口驱动的设计
- 易于测试和扩展

### 4. 安全性优先
- API密钥安全存储
- 签名算法安全实现
- 输入验证和过滤

### 5. 高性能设计
- 无状态服务设计
- 缓存机制
- 异步处理

## 核心组件

### 1. 控制器层 (Controller)

#### XfyunSignController
**职责**: 处理HTTP请求，提供RESTful API

**核心接口**:
```java
@RestController
@RequestMapping("/api/v1/xfyun")
public class XfyunSignController {
    
    @GetMapping("/sign/iat")
    public Result<Map<String, Object>> signIat(String authorization);
    
    @GetMapping("/sign/tts")
    public Result<Map<String, Object>> signTts(String authorization);
    
    @GetMapping("/sign/rtasr")
    public Result<Map<String, Object>> signRtasr(String authorization);
    
    @GetMapping("/sign/dts/create")
    public Result<Map<String, Object>> signDtsCreate(String authorization);
    
    @GetMapping("/sign/dts/query")
    public Result<Map<String, Object>> signDtsQuery(String authorization);
}
```

**特点**:
- 统一的响应格式
- 可选的授权头支持
- 详细的API文档

### 2. 服务层 (Service)

#### XfyunSignatureService
**职责**: 核心业务逻辑，签名生成算法

**核心方法**:
```java
@Service
public class XfyunSignatureService {
    
    public Map<String, Object> generateIatSignature(String authorization);
    public Map<String, Object> generateTtsSignature(String authorization);
    public Map<String, Object> generateRtasrSignature(String authorization);
    public Map<String, Object> generateDtsCreateSignature(String authorization);
    public Map<String, Object> generateDtsQuerySignature(String authorization);
    
    // 私有方法
    private Map<String, Object> buildWsSignedEncrypted(...);
    private Map<String, Object> buildRtasrSignedEncrypted(...);
    private Map<String, Object> buildDtsSignedEncrypted(...);
}
```

**签名算法**:
- HMAC-SHA256 (IAT/TTS/DTS)
- HMAC-SHA1 + MD5 (RTASR)
- Base64编码
- URL编码

### 3. 配置层 (Configuration)

#### XfyunConfigProperties
**职责**: 管理所有API服务的配置信息

**配置结构**:
```java
@ConfigurationProperties(prefix = "xfyun")
public class XfyunConfigProperties {
    
    private Iat iat;
    private Tts tts;
    private Rtasr rtasr;
    private Dts dts;
    
    public static class Iat {
        private String appId;
        private String apiKey;
        private String apiSecret;
    }
    
    // ... 其他服务配置
}
```

**配置来源**:
- application.yml
- application-dev.yml
- application-prod.yml
- 环境变量

### 4. 领域层 (Domain)

#### Result
**职责**: 统一的响应格式

```java
public class Result<T> {
    private int errorCode;
    private String message;
    private T data;
    
    public static <T> Result<T> success(T data);
    public static <T> Result<T> error(int errorCode, String message);
}
```

## 签名算法详解

### 1. IAT/TTS签名算法 (HMAC-SHA256)

```java
// 1. 生成签名原文
String signatureOrigin = String.format(
    "host: %s\ndate: %s\nGET %s HTTP/1.1", 
    host, date, path
);

// 2. HMAC-SHA256签名
String signature = hmacSha256Base64(signatureOrigin, apiSecret);

// 3. 生成Authorization头
String authorizationOrigin = String.format(
    "api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
    apiKey, signature
);

// 4. Base64编码
String authB64 = Base64Utils.encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));
```

### 2. RTASR签名算法 (HMAC-SHA1 + MD5)

```java
// 1. 生成时间戳
long ts = System.currentTimeMillis() / 1000;

// 2. 生成基础字符串
String baseString = appId + ts;

// 3. MD5哈希
String md5Hash = md5(baseString);

// 4. HMAC-SHA1签名
String signa = hmacSha1Base64(md5Hash, apiKey);
```

### 3. DTS签名算法 (HMAC-SHA256)

```java
// 1. 生成RFC1123格式日期
String date = generateDtsDate();

// 2. 生成请求行
String requestLine = String.format("%s %s HTTP/1.1", method, path);

// 3. 生成签名原文
String signatureOrigin = String.format(
    "host: %s\ndate: %s\n%s", 
    host, date, requestLine
);

// 4. HMAC-SHA256签名
String signature = hmacSha256Base64(signatureOrigin, apiSecret);
```

## 安全机制

### 1. API密钥管理
- 配置文件存储
- 环境变量支持
- 生产环境加密

### 2. 签名验证
- 时间戳验证
- 签名算法验证
- 参数完整性检查

### 3. 输入验证
- 参数类型检查
- 长度限制
- 格式验证

### 4. 错误处理
- 统一错误码
- 安全错误信息
- 日志记录

## 配置管理

### 1. 多环境配置

#### 开发环境 (application-dev.yml)
```yaml
xfyun:
  iat:
    app-id: dev-iat-app-id
    api-key: dev-iat-api-key
    api-secret: dev-iat-api-secret
```

#### 生产环境 (application-prod.yml)
```yaml
xfyun:
  iat:
    app-id: ${XFyun_IAT_APP_ID}
    api-key: ${XFyun_IAT_API_KEY}
    api-secret: ${XFyun_IAT_API_SECRET}
```

### 2. 配置优先级
1. 环境变量
2. application-prod.yml
3. application-dev.yml
4. application.yml

### 3. 配置验证
- 启动时配置检查
- 必填参数验证
- 格式验证

## 性能优化

### 1. 无状态设计
- 无会话状态
- 水平扩展支持
- 负载均衡友好

### 2. 缓存机制
- 配置缓存
- 签名缓存（可选）
- 响应缓存

### 3. 异步处理
- 非阻塞I/O
- 异步日志记录
- 异步监控

### 4. 连接池
- HTTP连接池
- 数据库连接池（如需要）
- 资源复用

## 监控和日志

### 1. 健康检查
```java
@RestController
public class HealthController {
    
    @GetMapping("/actuator/health")
    public ResponseEntity<Map<String, Object>> health();
}
```

### 2. 指标监控
- 请求计数
- 响应时间
- 错误率
- 系统资源

### 3. 日志记录
- 结构化日志
- 分级记录
- 性能日志
- 安全日志

### 4. 告警机制
- 错误告警
- 性能告警
- 资源告警

## 部署架构

### 1. 容器化部署
```dockerfile
FROM openjdk:8-jre-alpine
COPY target/xfyun-webapi-1.2.5.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 2. 微服务架构
- 独立部署
- 服务发现
- 配置中心
- 监控中心

### 3. 负载均衡
- Nginx负载均衡
- 健康检查
- 故障转移

### 4. 高可用
- 多实例部署
- 故障恢复
- 数据备份

## 扩展性设计

### 1. 新API服务支持
- 接口标准化
- 配置驱动
- 插件化签名算法

### 2. 新签名算法支持
- 算法抽象
- 策略模式
- 配置切换

### 3. 新功能扩展
- 模块化设计
- 接口隔离
- 依赖注入

## 测试策略

### 1. 单元测试
- 服务层测试
- 工具类测试
- 配置测试

### 2. 集成测试
- API接口测试
- 签名算法测试
- 配置加载测试

### 3. 性能测试
- 压力测试
- 负载测试
- 稳定性测试

### 4. 安全测试
- 签名验证测试
- 输入验证测试
- 权限测试

## 运维管理

### 1. 部署管理
- 自动化部署
- 版本管理
- 回滚机制

### 2. 监控管理
- 实时监控
- 历史数据
- 告警处理

### 3. 日志管理
- 日志收集
- 日志分析
- 日志存储

### 4. 配置管理
- 配置版本控制
- 配置热更新
- 配置审计

## 未来规划

### 1. 功能扩展
- 更多API服务支持
- 签名算法优化
- 性能监控增强

### 2. 架构升级
- Spring Boot 3.x
- Java 17+
- 云原生支持

### 3. 安全增强
- 更严格的验证
- 审计日志
- 安全扫描

---

本文档会随着项目的发展持续更新，如有疑问或建议，请通过Issue或Pull Request与我们联系。
