package com.xfyun.webapi.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 讯飞配置装配类。
 *
 * <p>启用 {@link XfyunConfigProperties} 的属性绑定。</p>
 * 
 * @author xfyun-webapi
 * @version 1.0
 * @since 2025-09-15
 */
@Configuration
@EnableConfigurationProperties(XfyunConfigProperties.class)
public class XfyunConfiguration {
}
