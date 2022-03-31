package com.fcc.biz.handler.config;

import java.util.List;

public interface Config {
    /**
     *
     * @param scene
     * @return
     */
    List<OneBizConfig> getConfig(String scene);

    /**
     * 实现初始化相关设置
     */
    void init(List<OneBizConfig> configDTOList);
}
