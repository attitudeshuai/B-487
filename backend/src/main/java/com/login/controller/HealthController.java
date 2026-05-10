package com.login.controller;

import com.login.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author Login System
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    /**
     * 健康检查接口
     * 
     * @return 健康状态信息
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        log.debug("健康检查请求");
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "Login System Backend");
        data.put("version", "1.0.0");
        data.put("timestamp", LocalDateTime.now().toString());
        return Result.success(data);
    }
}
