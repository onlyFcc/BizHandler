package com.fcc.biz.handler;

import com.fcc.biz.handler.config.OneBizConfig;

import java.util.List;
import java.util.Map;

public interface HandlerApplication {

    void init();

    boolean run(String scene, Object data);

    boolean runWithConfig(String scene, Object data, OneBizConfig configDTO);

    boolean batchRun(String scene, List<Object> dataMap);

    boolean batchRunWithConfig(String scene, List<Object> dataList, OneBizConfig configDTO);

    boolean runMap(String scene, Map<String, Object> data);

    boolean runMapWithConfig(String scene, Map<String, Object> data, OneBizConfig configDTO);

    boolean batchRunMap(String scene, List<Map<String, Object>> dataMap);

    boolean batchRunMapWithConfig(String scene, List<Map<String, Object>> dataList, OneBizConfig configDTO);
}
