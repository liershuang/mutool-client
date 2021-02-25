package com.mutool.app.config;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 描述：<br>
 * 作者：les<br>
 * 日期：2021/1/5 23:32<br>
 */
@Slf4j
@Component
public class StartRunner implements ApplicationRunner {

    /**
     * 服务域名
     */
    @Value("${mutool.server.domain}")
    private String serverDoamin;
    /**
     * 服务端口
     */
    @Value("${server.port}")
    private String serverPort;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ReflectUtil.setFieldValue(com.mutool.core.config.SystemConfig .class, "serverDoamin", serverDoamin);
        ReflectUtil.setFieldValue(com.mutool.core.config.SystemConfig.class, "serverPort", serverPort);
        log.info("系统启动设置属性，配置内容, serverDoamin:{}, serverPort:{}", serverDoamin, serverPort);
    }
}
