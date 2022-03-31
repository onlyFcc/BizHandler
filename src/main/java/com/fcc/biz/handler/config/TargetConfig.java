package com.fcc.biz.handler.config;

import lombok.Data;

/**
 * 最终处理后所得的数据，可能另有配置处理
 */
@Data
public class TargetConfig {
    /**
     * 目标数据类型，决定了最终处理的handler
     */
    private String targetDataType;
    private Boolean targetListErrorContinue;

    //最后的所需数据字段处理配置，为空则表示不处理，直接返回最后所得数据
    private FieldConfig fieldConfig;
}
