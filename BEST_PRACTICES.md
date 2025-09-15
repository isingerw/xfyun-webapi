# 最佳实践指南

本文档提供了使用科大讯飞WebAPI后端服务的最佳实践，帮助您构建高质量、高性能、安全的签名服务。

## 1. 项目结构最佳实践

### 1.1 分层架构
```java
// 推荐的项目结构
src/main/java/com/xfyun/webapi/
├── controller/          # 控制器层
│   └── XfyunSignController.java
├── service/            # 服务层
│   └── XfyunSignatureService.java
├── config/             # 配置层
│   ├── XfyunConfigProperties.java
│   └── XfyunConfiguration.java
├── domain/             # 领域层
│   └── Result.java
├── utils/              # 工具类
│   ├── SignatureUtils.java
│   └── ValidationUtils.java
└── exception/          # 异常处理
    ├── GlobalExceptionHandler.java
    └── XfyunException.java
```

### 1.2 包命名规范
```java
// 推荐的包结构
com.xfyun.webapi
├── controller          # 控制器
├── service            # 服务层
├── config             # 配置
├── domain             # 领域对象
├── dto                # 数据传输对象
├── utils              # 工具类
├── exception          # 异常处理
├── interceptor        # 拦截器
├── filter             # 过滤器
└── aspect             # 切面
```

## 2. 配置管理最佳实践

### 2.1 多环境配置
```yaml
# application.yml - 基础配置
spring:
  profiles:
    active: dev
  application:
    name: xfyun-webapi

server:
  port: 8080

# application-dev.yml - 开发环境
spring:
  profiles: dev

xfyun:
  iat:
    app-id: dev-iat-app-id
    api-key: dev-iat-api-key
    api-secret: dev-iat-api-secret

logging:
  level:
    com.xfyun.webapi: DEBUG

# application-prod.yml - 生产环境
spring:
  profiles: prod

xfyun:
  iat:
    app-id: ${XFyun_IAT_APP_ID}
    api-key: ${XFyun_IAT_API_KEY}
    api-secret: ${XFyun_IAT_API_SECRET}

logging:
  level:
    com.xfyun.webapi: INFO
```

### 2.2 配置验证
```java
// config/XfyunConfiguration.java
@Configuration
@EnableConfigurationProperties(XfyunConfigProperties.class)
public class XfyunConfiguration {

    @Bean
    @ConditionalOnProperty(name = "xfyun.validation.enabled", havingValue = "true", matchIfMissing = true)
    public XfyunConfigValidator xfyunConfigValidator(XfyunConfigProperties properties) {
        return new XfyunConfigValidator(properties);
    }
}

// utils/XfyunConfigValidator.java
@Component
public class XfyunConfigValidator implements InitializingBean {
    
    private final XfyunConfigProperties properties;
    
    public XfyunConfigValidator(XfyunConfigProperties properties) {
        this.properties = properties;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        validateConfig();
    }
    
    private void validateConfig() {
        validateIatConfig();
        validateTtsConfig();
        validateRtasrConfig();
        validateDtsConfig();
    }
    
    private void validateIatConfig() {
        XfyunConfigProperties.Iat iat = properties.getIat();
        if (StringUtils.isBlank(iat.getAppId()) || 
            StringUtils.isBlank(iat.getApiKey()) || 
            StringUtils.isBlank(iat.getApiSecret())) {
            throw new IllegalStateException("IAT configuration is incomplete");
        }
    }
    
    // ... 其他验证方法
}
```

## 3. 错误处理最佳实践

### 3.1 统一异常处理
```java
// exception/GlobalExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(XfyunException.class)
    public ResponseEntity<Result<Object>> handleXfyunException(XfyunException e) {
        log.error("Xfyun service error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(500, "Internal server error"));
    }
}

// exception/XfyunException.java
public class XfyunException extends RuntimeException {
    private final int errorCode;
    
    public XfyunException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public XfyunException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}
```

### 3.2 业务异常处理
```java
// service/XfyunSignatureService.java
@Service
@Slf4j
public class XfyunSignatureService {
    
    public Map<String, Object> generateIatSignature(String authorization) {
        try {
            XfyunConfigProperties.Iat cfg = xfyunConfigProperties.getIat();
            validateIatConfig(cfg);
            return buildWsSignedEncrypted(IAT_HOST, IAT_PATH, 
                cfg.getApiKey(), cfg.getApiSecret(), cfg.getAppId(), authorization);
        } catch (Exception e) {
            log.error("Failed to generate IAT signature", e);
            throw new XfyunException(500, "IAT signature generation failed", e);
        }
    }
    
    private void validateIatConfig(XfyunConfigProperties.Iat cfg) {
        if (cfg == null) {
            throw new IllegalArgumentException("IAT configuration is null");
        }
        if (StringUtils.isBlank(cfg.getAppId())) {
            throw new IllegalArgumentException("IAT app-id is required");
        }
        if (StringUtils.isBlank(cfg.getApiKey())) {
            throw new IllegalArgumentException("IAT api-key is required");
        }
        if (StringUtils.isBlank(cfg.getApiSecret())) {
            throw new IllegalArgumentException("IAT api-secret is required");
        }
    }
}
```

## 4. 性能优化最佳实践

### 4.1 缓存机制
```java
// service/XfyunSignatureService.java
@Service
@Slf4j
public class XfyunSignatureService {
    
    @Cacheable(value = "signature", key = "#type + '_' + #authorization")
    public Map<String, Object> generateSignature(String type, String authorization) {
        // 签名生成逻辑
        return buildSignature(type, authorization);
    }
    
    @CacheEvict(value = "signature", allEntries = true)
    @Scheduled(fixedRate = 300000) // 5分钟清理一次缓存
    public void clearSignatureCache() {
        log.info("Clearing signature cache");
    }
}

// config/CacheConfiguration.java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
```

### 4.2 连接池配置
```yaml
# application.yml
server:
  tomcat:
    max-connections: 8192
    accept-count: 100
    threads:
      max: 200
      min-spare: 10
    connection-timeout: 20000
    keep-alive-timeout: 15000
    max-keep-alive-requests: 100

spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 4.3 异步处理
```java
// service/AsyncSignatureService.java
@Service
@Slf4j
public class AsyncSignatureService {
    
    @Async("signatureTaskExecutor")
    public CompletableFuture<Map<String, Object>> generateSignatureAsync(
            String type, String authorization) {
        try {
            Map<String, Object> result = generateSignature(type, authorization);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Async signature generation failed", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}

// config/AsyncConfiguration.java
@Configuration
@EnableAsync
public class AsyncConfiguration {
    
    @Bean("signatureTaskExecutor")
    public TaskExecutor signatureTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("signature-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

## 5. 安全最佳实践

### 5.1 输入验证
```java
// utils/ValidationUtils.java
@Component
public class ValidationUtils {
    
    private static final Pattern APP_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9]{32}$");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9]{32}$");
    private static final Pattern API_SECRET_PATTERN = Pattern.compile("^[a-zA-Z0-9]{32}$");
    
    public void validateAppId(String appId) {
        if (StringUtils.isBlank(appId)) {
            throw new IllegalArgumentException("App ID cannot be blank");
        }
        if (!APP_ID_PATTERN.matcher(appId).matches()) {
            throw new IllegalArgumentException("Invalid App ID format");
        }
    }
    
    public void validateApiKey(String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("API Key cannot be blank");
        }
        if (!API_KEY_PATTERN.matcher(apiKey).matches()) {
            throw new IllegalArgumentException("Invalid API Key format");
        }
    }
    
    public void validateApiSecret(String apiSecret) {
        if (StringUtils.isBlank(apiSecret)) {
            throw new IllegalArgumentException("API Secret cannot be blank");
        }
        if (!API_SECRET_PATTERN.matcher(apiSecret).matches()) {
            throw new IllegalArgumentException("Invalid API Secret format");
        }
    }
}
```

### 5.2 请求限流
```java
// config/RateLimitConfiguration.java
@Configuration
public class RateLimitConfiguration {
    
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}

// interceptor/RateLimitInterceptor.java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler) throws Exception {
        String clientId = getClientId(request);
        String key = RATE_LIMIT_KEY_PREFIX + clientId;
        
        String count = redisTemplate.opsForValue().get(key);
        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
        } else {
            int currentCount = Integer.parseInt(count);
            if (currentCount >= MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded");
                return false;
            }
            redisTemplate.opsForValue().increment(key);
        }
        
        return true;
    }
    
    private String getClientId(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

### 5.3 敏感信息保护
```java
// utils/SecurityUtils.java
@Component
public class SecurityUtils {
    
    private static final String ENCRYPTION_KEY = "your-encryption-key";
    
    public String encryptSensitiveData(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    public String decryptSensitiveData(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
```

## 6. 监控和日志最佳实践

### 6.1 结构化日志
```java
// config/LoggingConfiguration.java
@Configuration
public class LoggingConfiguration {
    
    @Bean
    public LoggingEventEnhancer loggingEventEnhancer() {
        return new LoggingEventEnhancer();
    }
}

// utils/LoggingEventEnhancer.java
public class LoggingEventEnhancer implements LoggingEventEnhancer {
    
    @Override
    public void enhance(ILoggingEvent event, Object[] objects) {
        MDC.put("timestamp", Instant.now().toString());
        MDC.put("thread", Thread.currentThread().getName());
        MDC.put("level", event.getLevel().toString());
    }
}
```

### 6.2 性能监控
```java
// aspect/PerformanceAspect.java
@Aspect
@Component
@Slf4j
public class PerformanceAspect {
    
    @Around("@annotation(Monitored)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("Method {} executed in {} ms", methodName, executionTime);
            
            // 记录性能指标
            recordPerformanceMetric(methodName, executionTime);
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Method {} failed after {} ms", methodName, executionTime, e);
            throw e;
        }
    }
    
    private void recordPerformanceMetric(String methodName, long executionTime) {
        // 实现性能指标记录逻辑
        // 可以发送到监控系统如Prometheus、InfluxDB等
    }
}

// annotation/Monitored.java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitored {
    String value() default "";
}
```

### 6.3 健康检查
```java
// health/XfyunHealthIndicator.java
@Component
public class XfyunHealthIndicator implements HealthIndicator {
    
    @Autowired
    private XfyunSignatureService signatureService;
    
    @Override
    public Health health() {
        try {
            // 检查签名服务是否正常
            signatureService.generateIatSignature("test");
            return Health.up()
                    .withDetail("status", "UP")
                    .withDetail("timestamp", Instant.now())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "DOWN")
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", Instant.now())
                    .build();
        }
    }
}
```

## 7. 测试最佳实践

### 7.1 单元测试
```java
// test/service/XfyunSignatureServiceTest.java
@ExtendWith(MockitoExtension.class)
class XfyunSignatureServiceTest {
    
    @Mock
    private XfyunConfigProperties configProperties;
    
    @InjectMocks
    private XfyunSignatureService signatureService;
    
    @Test
    void testGenerateIatSignature() {
        // Given
        XfyunConfigProperties.Iat iatConfig = new XfyunConfigProperties.Iat();
        iatConfig.setAppId("test-app-id");
        iatConfig.setApiKey("test-api-key");
        iatConfig.setApiSecret("test-api-secret");
        
        when(configProperties.getIat()).thenReturn(iatConfig);
        
        // When
        Map<String, Object> result = signatureService.generateIatSignature("test-auth");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).containsKey("url");
        assertThat(result).containsKey("appId");
        assertThat(result.get("appId")).isEqualTo("test-app-id");
    }
    
    @Test
    void testGenerateIatSignatureWithInvalidConfig() {
        // Given
        when(configProperties.getIat()).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> signatureService.generateIatSignature("test-auth"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("IAT configuration is null");
    }
}
```

### 7.2 集成测试
```java
// test/integration/XfyunSignControllerIntegrationTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class XfyunSignControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testIatSignatureEndpoint() {
        // Given
        String url = "/api/v1/xfyun/sign/iat";
        
        // When
        ResponseEntity<Result> response = restTemplate.getForEntity(url, Result.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(0);
    }
}
```

## 8. 部署最佳实践

### 8.1 Docker配置
```dockerfile
# Dockerfile
FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 创建非root用户
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 复制JAR文件
COPY target/xfyun-webapi-1.2.5.jar app.jar

# 设置文件权限
RUN chown appuser:appgroup app.jar

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 8.2 Kubernetes配置
```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: xfyun-webapi
spec:
  replicas: 3
  selector:
    matchLabels:
      app: xfyun-webapi
  template:
    metadata:
      labels:
        app: xfyun-webapi
    spec:
      containers:
      - name: xfyun-webapi
        image: xfyun-webapi:1.2.5
        ports:
        - containerPort: 8080
        env:
        - name: XFyun_IAT_APP_ID
          valueFrom:
            secretKeyRef:
              name: xfyun-secrets
              key: iat-app-id
        - name: XFyun_IAT_API_KEY
          valueFrom:
            secretKeyRef:
              name: xfyun-secrets
              key: iat-api-key
        - name: XFyun_IAT_API_SECRET
          valueFrom:
            secretKeyRef:
              name: xfyun-secrets
              key: iat-api-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

## 9. 总结

遵循这些最佳实践将帮助您：

1. **提高代码质量**: 通过分层架构和异常处理
2. **提升性能**: 通过缓存和异步处理
3. **增强安全性**: 通过输入验证和限流
4. **简化维护**: 通过监控和日志
5. **确保可靠性**: 通过测试和健康检查

记住，最佳实践是指导原则，根据您的具体需求和环境特点，适当调整这些实践。

---

如有疑问或建议，请通过 [Issue](https://github.com/isingerw/xfyun-webapi/issues) 或邮件联系我们。
