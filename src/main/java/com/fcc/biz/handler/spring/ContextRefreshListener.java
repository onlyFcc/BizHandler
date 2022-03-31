package com.fcc.biz.handler.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            log.info("\n================:{}，开始执行自定义资源初始化操作", "ApplicationReadyEvent");

            log.info("================:{}，完成自定义资源初始化操作\n", "ApplicationReadyEvent");
        }

    }
}
