package com.xfyun.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 科大讯飞WebAPI后端服务启动类
 * 
 * 提供讯飞各种AI服务的签名生成和API密钥管理功能
 * 
 * @author xfyun-webapi
 * @version 1.0
 * @since 2025-09-15
 */
@SpringBootApplication
public class XfyunWebapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XfyunWebapiApplication.class, args);
    }
}
