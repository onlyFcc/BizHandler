package com.fcc.biz.handler.config;

import lombok.Data;

@Data
public class BizFlowConfig {
    /**
     * 配置方式，默认Apollo配置
     * @see com.fcc.biz.handler.config.ConfigTypeEnum
     */
    private int configType;
    //流程处理方式，默认使用aviator脚本语言
    private int handleType;
}
