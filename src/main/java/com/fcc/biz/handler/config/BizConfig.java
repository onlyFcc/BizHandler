package com.fcc.biz.handler.config;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 业务配置，包含整体业务配置以及具体业务配置列表
 */
@Data
public class BizConfig implements Serializable {
    //是否需要返回实体数据，返回实体数据则每一个场景只有一条处理业务，即bizConfigs.size = 1
    private boolean returnData;
    //本业务下的配置列表是否需要并发处理，默认false
    private boolean parallel;
    //具体业务配置列表
    private List<OneBizConfig> bizConfigs;
}
