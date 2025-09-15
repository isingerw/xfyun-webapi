package com.xfyun.webapi.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xfyun.webapi.service.XfyunSignatureService;
import com.xfyun.webapi.domain.Result;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 科大讯飞签名控制器
 * 
 * 提供讯飞各种AI服务的签名接口，包括：
 * - 语音听写 (IAT)
 * - 语音合成 (TTS)
 * - 实时语音转写 (RTASR)
 * - 长文本语音合成 (DTS)
 * 
 * 所有接口都通过后端统一管理API密钥，确保安全性。
 * 
 * @author xfyun-webapi
 * @version 1.0
 * @since 2025-09-15
 */
@RestController
@RequestMapping(value = "/api/v1/xfyun", produces = MediaType.APPLICATION_JSON_VALUE)
public class XfyunSignController {

    @Resource
    private XfyunSignatureService signatureService;

    /**
     * 获取语音听写(IAT)签名
     * 
     * 用于语音转文字服务，支持实时音频流识别
     * 
     * @param authorization 授权头信息
     * @return 包含WebSocket连接URL和应用ID的签名结果
     * @throws Exception 签名生成异常
     */
    @GetMapping("/sign/iat")
    public Result<Map<String, Object>> signIat(@RequestHeader(value = "Authorization", required = false) String authorization) throws Exception {
        return Result.success(signatureService.generateIatSignature(authorization));
    }

    /**
     * 获取语音合成(TTS)签名
     * 
     * 用于文字转语音服务，支持实时音频流合成
     * 
     * @param authorization 授权头信息
     * @return 包含WebSocket连接URL和应用ID的签名结果
     * @throws Exception 签名生成异常
     */
    @GetMapping("/sign/tts")
    public Result<Map<String, Object>> signTts(@RequestHeader(value = "Authorization", required = false) String authorization) throws Exception {
        return Result.success(signatureService.generateTtsSignature(authorization));
    }

    /**
     * 获取实时语音转写(RTASR)签名
     * 
     * 用于长时间语音转文字服务，支持连续音频流识别
     * 文档：https://www.xfyun.cn/doc/asr/rtasr/API.html
     * 
     * @param authorization 授权头信息
     * @return 包含WebSocket连接URL、应用ID、时间戳和签名的结果
     * @throws Exception 签名生成异常
     */
    @GetMapping("/sign/rtasr")
    public Result<Map<String, Object>> signRtasr(@RequestHeader(value = "Authorization", required = false) String authorization) throws Exception {
        return Result.success(signatureService.generateRtasrSignature(authorization));
    }

    /**
     * 获取长文本语音合成(DTS)创建任务签名
     * 
     * 用于长文本语音合成任务创建，支持10万字左右的长文本
     * 文档：https://www.xfyun.cn/doc/tts/long_text_tts/API.html
     * 
     * @param authorization 授权头信息
     * @return 包含完整API URL的签名结果，前端可直接调用
     * @throws Exception 签名生成异常
     */
    @GetMapping("/sign/dts/create")
    public Result<Map<String, Object>> signDtsCreate(@RequestHeader(value = "Authorization", required = false) String authorization) throws Exception {
        return Result.success(signatureService.generateDtsCreateSignature(authorization));
    }

    /**
     * 获取长文本语音合成(DTS)查询任务签名
     * 
     * 用于查询长文本语音合成任务状态和结果
     * 文档：https://www.xfyun.cn/doc/tts/long_text_tts/API.html
     * 
     * @param authorization 授权头信息
     * @return 包含完整API URL的签名结果，前端可直接调用
     * @throws Exception 签名生成异常
     */
    @GetMapping("/sign/dts/query")
    public Result<Map<String, Object>> signDtsQuery(@RequestHeader(value = "Authorization", required = false) String authorization) throws Exception {
        return Result.success(signatureService.generateDtsQuerySignature(authorization));
    }
}
