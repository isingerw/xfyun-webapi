package com.xfyun.webapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 讯飞 IAT/TTS/RTASR/DTS 配置属性。
 *
 * <p>支持在 Spring 配置文件中使用前缀 {@code xfyun} 进行配置：
 * <pre>
 * xfyun:
 *   iat:
 *     app-id: xxx
 *     api-key: xxx
 *     api-secret: xxx
 *   tts:
 *     app-id: xxx
 *     api-key: xxx
 *     api-secret: xxx
 *   rtasr:
 *     app-id: xxx
 *     api-key: xxx
 *   dts:
 *     app-id: xxx
 *     api-key: xxx
 *     api-secret: xxx
 * </pre>
 * </p>
 * 
 * @author xfyun-webapi
 * @version 1.0
 * @since 2025-09-15
 */
@ConfigurationProperties(prefix = "xfyun")
public class XfyunConfigProperties {

    private Iat iat = new Iat();
    private Tts tts = new Tts();
    private Rtasr rtasr = new Rtasr();
    private Dts dts = new Dts();

    public Iat getIat() {
        return iat;
    }

    public void setIat(Iat iat) {
        this.iat = iat;
    }

    public Tts getTts() {
        return tts;
    }

    public void setTts(Tts tts) {
        this.tts = tts;
    }

    public Rtasr getRtasr() {
        return rtasr;
    }

    public void setRtasr(Rtasr rtasr) {
        this.rtasr = rtasr;
    }

    public Dts getDts() {
        return dts;
    }

    public void setDts(Dts dts) {
        this.dts = dts;
    }

    /**
     * IAT(语音听写) 配置。
     */
    public static class Iat {
        private String appId;
        private String apiKey;
        private String apiSecret;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiSecret() {
            return apiSecret;
        }

        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }
    }

    /**
     * TTS(语音合成) 配置。
     */
    public static class Tts {
        private String appId;
        private String apiKey;
        private String apiSecret;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiSecret() {
            return apiSecret;
        }

        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }
    }

    /**
     * RTASR(实时语音转写) 配置。
     */
    public static class Rtasr {
        private String appId;
        private String apiKey;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    /**
     * DTS(长文本语音合成) 配置。
     */
    public static class Dts {
        private String appId;
        private String apiKey;
        private String apiSecret;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiSecret() {
            return apiSecret;
        }

        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }
    }
}
