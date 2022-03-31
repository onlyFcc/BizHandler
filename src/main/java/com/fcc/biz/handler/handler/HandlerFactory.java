package com.fcc.biz.handler.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class HandlerFactory {

    public List<Handler> initFilter(JSONArray handlers) {
        if (handlers == null) {
            return Lists.newArrayList();
        }
        List<Handler> result = Lists.newArrayListWithExpectedSize(handlers.size());
        handlers.forEach(node -> {
            JSONObject handlerJson = (JSONObject) JSON.parse(JSON.toJSONString(node));
            String type = handlerJson.getString("type");
            JSONObject params = handlerJson.getJSONObject("params");
            Handler handler = getFilter(type, params);
            if (handler != null) {
                result.add(handler);
            } else {
                log.error("数据同步配置异常！filters error! type = " + type);
            }
        });

        return result;
    }

    public Handler getFilter(String type, JSONObject params) {

        return null;
    }
}
