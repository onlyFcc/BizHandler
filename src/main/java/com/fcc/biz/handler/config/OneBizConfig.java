package com.fcc.biz.handler.config;

import com.alibaba.fastjson.JSONArray;
import com.fcc.biz.handler.handler.Handler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OneBizConfig {
    /**
     * 可为空
     * 若输入数据为String类型，序列化之前判断操作：若该配置不空，则消息中，必须包含以下字符串至少一个，否则不符合
     */
    private List<String> preHandlerStrings;
    /**
     * 是否需要重试，默认不需要
     */
    private boolean needRetry;
    /**
     * 异步时，hash字段，hash key字段
     */
    private String threadHashKeyField;



    //处理流程
    private JSONArray handlers;
    //转换后的处理流程器
    private List<Handler> handlerList;

    //最后的所需数据字段处理配置，为空则表示不处理，直接返回最后所得数据
    private FieldConfig fieldConfig;

    //若是流处理，可能有多个
    private List<TargetConfig> targetList;
}
