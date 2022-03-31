package com.fcc.biz.handler.config;

import lombok.Data;

import java.util.List;

@Data
public class FieldConfig {
    /**
     * 最终的数据字段，是否需要转化成下划线格式，默认否
     */
    private boolean lowUnderCase;
    /**
     * 是否采用复制的方法，将字段复制到一个新的JsonObject对象中，默认否，即采用remove的方法
     */
    private boolean fieldCopy;
    /**
     * 需要的字段
     */
    private List<FieldBO> needFields;
    /**
     * 字段保留，前缀匹配方式
     */
    private List<String> needFieldsPre;
}
