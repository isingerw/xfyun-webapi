package com.xfyun.webapi.service;

import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import com.xfyun.webapi.config.XfyunConfigProperties;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.HashMap;
import java.util.Map;

/**
 * 科大讯飞签名服务
 *
 * 提供统一的签名生成和加密功能，支持多种讯飞API服务：
 * - IAT (语音听写)
 * - TTS (语音合成) 
 * - RTASR (实时语音转写)
 * - DTS (长文本语音合成)
 *
 * @author xfyun-webapi
 * @version 1.0
 * @since 2025-09-15
 */
@Service
public class XfyunSignatureService {

    @Resource
    private XfyunConfigProperties xfyunConfigProperties;

    // 加密开关，设置为false关闭加密
    private static final boolean ENABLE_ENCRYPTION = false;

    // 协议常量
    private static final String WSS_KEY = "wss://";
    private static final String HTTPS_KEY = "https://";

    // IAT配置
    private static final String IAT_HOST = "iat-api.xfyun.cn";
    private static final String IAT_PATH = "/v2/iat";

    // TTS配置
    private static final String TTS_HOST = "tts-api.xfyun.cn";
    private static final String TTS_PATH = "/v2/tts";

    // RTASR配置
    private static final String RTASR_HOST = "rtasr.xfyun.cn";
    private static final String RTASR_PATH = "/v1/ws";

    // DTS配置
    private static final String DTS_HOST = "api-dx.xf-yun.com";
    private static final String DTS_CREATE_PATH = "/v1/private/dts_create";
    private static final String DTS_QUERY_PATH = "/v1/private/dts_query";

    /**
     * 生成IAT签名
     *
     * @param authorizationHeader 授权头信息
     * @return 包含url和appId的签名结果
     * @throws Exception 签名生成异常
     */
    public Map<String, Object> generateIatSignature(String authorizationHeader) throws Exception {
        XfyunConfigProperties.Iat cfg = xfyunConfigProperties.getIat();
        return buildWsSignedEncrypted(IAT_HOST, IAT_PATH, cfg.getApiKey(), cfg.getApiSecret(), cfg.getAppId(), authorizationHeader);
    }

    /**
     * 生成TTS签名
     *
     * @param authorizationHeader 授权头信息
     * @return 包含url和appId的签名结果
     * @throws Exception 签名生成异常
     */
    public Map<String, Object> generateTtsSignature(String authorizationHeader) throws Exception {
        XfyunConfigProperties.Tts cfg = xfyunConfigProperties.getTts();
        return buildWsSignedEncrypted(TTS_HOST, TTS_PATH, cfg.getApiKey(), cfg.getApiSecret(), cfg.getAppId(), authorizationHeader);
    }

    /**
     * 生成RTASR签名
     *
     * @param authorizationHeader 授权头信息
     * @return 包含url、appId、ts、signa的签名结果
     * @throws Exception 签名生成异常
     */
    public Map<String, Object> generateRtasrSignature(String authorizationHeader) throws Exception {
        XfyunConfigProperties.Rtasr cfg = xfyunConfigProperties.getRtasr();
        return buildRtasrSignedEncrypted(cfg.getAppId(), cfg.getApiKey(), authorizationHeader);
    }

    /**
     * 生成DTS创建任务签名
     *
     * @param authorizationHeader 授权头信息
     * @return 包含完整API URL的签名结果
     * @throws Exception 签名生成异常
     */
    public Map<String, Object> generateDtsCreateSignature(String authorizationHeader) throws Exception {
        XfyunConfigProperties.Dts cfg = xfyunConfigProperties.getDts();
        String baseUrl = HTTPS_KEY + DTS_HOST + DTS_CREATE_PATH;
        return buildDtsSignedEncrypted(DTS_HOST, DTS_CREATE_PATH, "POST", cfg.getApiKey(), cfg.getApiSecret(), cfg.getAppId(), authorizationHeader, baseUrl);
    }

    /**
     * 生成DTS查询任务签名
     *
     * @param authorizationHeader 授权头信息
     * @return 包含完整API URL的签名结果
     * @throws Exception 签名生成异常
     */
    public Map<String, Object> generateDtsQuerySignature(String authorizationHeader) throws Exception {
        XfyunConfigProperties.Dts cfg = xfyunConfigProperties.getDts();
        String baseUrl = HTTPS_KEY + DTS_HOST + DTS_QUERY_PATH;
        return buildDtsSignedEncrypted(DTS_HOST, DTS_QUERY_PATH, "POST", cfg.getApiKey(), cfg.getApiSecret(), cfg.getAppId(), authorizationHeader, baseUrl);
    }

    /**
     * 构建WebSocket签名（IAT/TTS通用）
     *
     * @param host 主机地址
     * @param path 路径
     * @param apiKey API密钥
     * @param apiSecret API密钥
     * @param appId 应用ID
     * @param authorizationHeader 授权头
     * @return 签名结果
     * @throws Exception 签名异常
     */
    private Map<String, Object> buildWsSignedEncrypted(String host, String path, String apiKey, String apiSecret, String appId, String authorizationHeader) throws Exception {
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now());
        String signatureOrigin = String.format("host: %s\ndate: %s\nGET %s HTTP/1.1", host, date, path);
        String signature = hmacSha256Base64(signatureOrigin, apiSecret);
        String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"", apiKey, signature);
        String authB64 = Base64Utils.encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));

        String wss = WSS_KEY + host + path;
        Map<String, Object> resp = new HashMap<>();
        resp.put("url", wss + "?authorization=" + urlEncode(authB64) + "&date=" + urlEncode(date) + "&host=" + host);
        resp.put("appId", appId);

        return processEncryption(resp, authorizationHeader);
    }

    /**
     * 构建RTASR专用签名
     *
     * @param appId 应用ID
     * @param apiKey API密钥
     * @param authorizationHeader 授权头
     * @return 签名结果
     * @throws Exception 签名异常
     */
    private Map<String, Object> buildRtasrSignedEncrypted(String appId, String apiKey, String authorizationHeader) throws Exception {
        long ts = System.currentTimeMillis() / 1000;
        String baseString = appId + ts;
        String md5Hash = md5(baseString);
        String signa = hmacSha1Base64(md5Hash, apiKey);

        String url = WSS_KEY + RTASR_HOST + RTASR_PATH + "?appid=" + urlEncode(appId) + "&ts=" + ts + "&signa=" + urlEncode(signa) + "&pd=edu";

        Map<String, Object> raw = new HashMap<>();
        raw.put("url", url);
        raw.put("appId", appId);
        raw.put("ts", String.valueOf(ts));
        raw.put("signa", signa);

        return processEncryption(raw, authorizationHeader);
    }

    /**
     * 构建DTS签名
     *
     * @param host 主机地址
     * @param path 路径
     * @param method HTTP方法
     * @param apiKey API密钥
     * @param apiSecret API密钥
     * @param appId 应用ID
     * @param authorizationHeader 授权头
     * @param baseUrl 基础URL
     * @return 签名结果
     * @throws Exception 签名异常
     */
    private Map<String, Object> buildDtsSignedEncrypted(String host, String path, String method, String apiKey, String apiSecret, String appId, String authorizationHeader, String baseUrl) throws Exception {
        // 使用与官方demo完全一致的日期格式
        String date = generateDtsDate();
        String requestLine = String.format("%s %s HTTP/1.1", method, path);
        String signatureOrigin = String.format("host: %s\ndate: %s\n%s", host, date, requestLine);
        String signature = hmacSha256Base64(signatureOrigin, apiSecret);
        String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"", apiKey, signature);
        String authB64 = Base64Utils.encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));

        String fullUrl = baseUrl + "?authorization=" + urlEncode(authB64) + "&date=" + urlEncode(date) + "&host=" + host;

        // 添加调试日志
        System.out.println("DTS签名调试信息:");
        System.out.println("原始日期: " + date);
        System.out.println("URL编码后日期: " + urlEncode(date));
        System.out.println("签名原文: " + signatureOrigin);
        System.out.println("签名结果: " + signature);
        System.out.println("完整URL: " + fullUrl);

        Map<String, Object> raw = new HashMap<>();
        raw.put("url", fullUrl);
        raw.put("appId", appId);
        raw.put("host", host);
        raw.put("date", date);
        raw.put("authorization", authB64);

        return processEncryption(raw, authorizationHeader);
    }

    /**
     * 生成DTS API专用的日期格式
     * 与官方demo完全一致
     *
     * @return RFC1123格式的GMT时间
     */
    private String generateDtsDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }

    /**
     * 处理加密逻辑
     *
     * @param data 原始数据
     * @param authorizationHeader 授权头
     * @return 处理后的数据
     * @throws Exception 加密异常
     */
    private Map<String, Object> processEncryption(Map<String, Object> data, String authorizationHeader) throws Exception {
        if (ENABLE_ENCRYPTION) {
            String key16 = deriveKeyFromAuthorization(authorizationHeaderValue(authorizationHeader));
            if (key16.length() < 16) {
                throw new IllegalArgumentException("Authorization 缺失或无效");
            }
            return encryptResponseFields(data, key16);
        } else {
            return data;
        }
    }

    /**
     * HMAC-SHA256签名
     *
     * @param data 待签名数据
     * @param secret 密钥
     * @return Base64编码的签名
     * @throws Exception 签名异常
     */
    private static String hmacSha256Base64(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64Utils.encodeToString(result);
    }

    /**
     * HMAC-SHA1签名
     *
     * @param data 待签名数据
     * @param secret 密钥
     * @return Base64编码的签名
     * @throws Exception 签名异常
     */
    private static String hmacSha1Base64(String data, String secret) throws Exception {
        byte[] rawHmac;
        try {
            byte[] keyBytes = secret.getBytes("UTF-8");
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
            byte[] textBytes = data.getBytes("UTF-8");
            rawHmac = mac.doFinal(textBytes);
        } catch (Exception e) {
            throw new Exception("HMAC-SHA1计算失败: " + e.getMessage());
        }
        return Base64Utils.encodeToString(rawHmac);
    }

    /**
     * MD5哈希
     *
     * @param data 待哈希数据
     * @return MD5哈希值
     * @throws Exception 哈希异常
     */
    private static String md5(String data) throws Exception {
        char md5String[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] btInput = data.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = md5String[byte0 >>> 4 & 0xf];
                str[k++] = md5String[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            throw new Exception("MD5计算失败: " + e.getMessage());
        }
    }

    /**
     * 从授权头提取值
     *
     * @param authorization 授权头
     * @return 提取的值
     */
    private static String authorizationHeaderValue(String authorization) {
        return authorization == null ? "" : authorization;
    }

    /**
     * 从授权头派生AES密钥
     *
     * @param authorization 授权头
     * @return 16位AES密钥
     * @throws Exception 密钥派生异常
     */
    private static String deriveKeyFromAuthorization(String authorization) throws Exception {
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(token.getBytes(StandardCharsets.UTF_8));
        String hex = bytesToHex(digest);
        return hex.substring(0, Math.min(16, hex.length()));
    }

    /**
     * 加密响应字段
     *
     * @param src 源数据
     * @param key16 16位AES密钥
     * @return 加密后的数据
     * @throws Exception 加密异常
     */
    private static Map<String, Object> encryptResponseFields(Map<String, Object> src, String key16) throws Exception {
        Map<String, Object> ret = new HashMap<>();

        for (Map.Entry<String, Object> entry : src.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                ret.put(key, null);
            } else {
                try {
                    ret.put(key, aesEncryptToHex(value.toString(), key16));
                } catch (Exception e) {
                    System.err.println("加密字段失败: " + key + ", 错误: " + e.getMessage());
                    ret.put(key, null);
                }
            }
        }

        return ret;
    }

    /**
     * AES加密为十六进制
     *
     * @param plaintext 明文
     * @param key16 16位密钥
     * @return 加密后的十六进制字符串
     * @throws Exception 加密异常
     */
    private static String aesEncryptToHex(String plaintext, String key16) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key16.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encrypted);
    }

    /**
     * 字节数组转十六进制
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * URL编码
     *
     * @param value 待编码值
     * @return 编码后的值
     */
    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}
