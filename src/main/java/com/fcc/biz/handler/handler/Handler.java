package com.fcc.biz.handler.handler;

import java.util.List;
import java.util.Map;

public interface Handler {
    /**
     * @param input
     * @return
     */
    Boolean handler(Map<String, Object> input) throws Exception;

    /**
     * 批量操作
     */
    Boolean batchHandler(List<Map<String, Object>> inputList) throws Exception;
}
