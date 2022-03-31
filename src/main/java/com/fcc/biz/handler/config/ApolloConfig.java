package com.fcc.biz.handler.config;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.fcc.biz.handler.constant.ConfigConstant;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConfigType
public class ApolloConfig extends AbstractConfig {

    @com.ctrip.framework.apollo.spring.annotation.ApolloConfig(ConfigConstant.NAMESPACE)
    private Config config;

    private int size = 1;

    /**
     * 配置加载标志位
     */
    private static Boolean SCHEMA_LOADED = false;

    /**
     * schema 配置
     */
    private static Map<String, List<OneBizConfig>> SCHEMA_MAP = new HashMap<>();

    @ApolloConfigChangeListener(ConfigConstant.NAMESPACE)
    private void someChangeHandler(ConfigChangeEvent changeEvent) {
        for (String key : changeEvent.changedKeys()){
            if (key.startsWith(ConfigConstant.SYNC_CONFIG_PREFIX)){
                //重置，里面重新拉取了配置
                setSchema();
                break;
            }
            if (key.equals(ConfigConstant.SYNC_CONFIG_SIZE)){
                this.size = Integer.parseInt(changeEvent.getChange(key).getNewValue());
            }
        }
    }

    /**
     * 获取对应topic的配置
     */
    @Override
    public List<OneBizConfig> getConfig(String topic) {
        //第一次获取schema时加载
        if (!SCHEMA_LOADED) {
            setSchema();
            SCHEMA_LOADED = true;
        }

        return SCHEMA_MAP.get(topic);
    }

    /**
     * 加载apollo schema
     * 还需要初始化init
     */
    private void setSchema() {
        log.info("65.DataTransformerConfigFactory, start set config!");
        SCHEMA_MAP = getSyncConfigString();
        log.info("81.DataTransformerConfigFactory, set config success!");
    }

    private Map<String, List<OneBizConfig>> getSyncConfigString(){
        Map<String, List<OneBizConfig>> result = Maps.newHashMap();
        for (int i = 0; i < size; i++){
            result.putAll(getSyncConfigString(ConfigConstant.SYNC_CONFIG_PREFIX + i));
        }
        return result;
    }
    private Map<String, List<OneBizConfig>> getSyncConfigString(String configString){
        String schema = config.getProperty(configString, "");
        Map<String, List<OneBizConfig>> schemaMap = ConfigConstant.GSON.fromJson(schema, new TypeToken<Map<String, List<OneBizConfig>>>() {
        }.getType());

        if (schemaMap == null || schemaMap.isEmpty()) {
            log.warn("[73.DataTransformerConfigFactory] 获取数据同步配置 Schema 为空！");
            return Maps.newHashMap();
        } else {
            schemaMap.forEach((topic,config) -> init(config));
            return schemaMap;
        }
    }
}
