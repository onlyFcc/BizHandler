package com.fcc.biz.handler.config;

import com.fcc.biz.handler.spring.BizHandlerContextUtils;
import com.google.common.collect.Lists;
import com.yupaopao.recommend.kafka.transform.transformer.DataTransformer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ConfigFactory {

    private Map<Integer, Config> configMap;

    public void init(){
        Map<String, Config> configMap = BizHandlerContextUtils.getBeansOfType(Config.class);


    }

    public List<DataTransformerConfigDTO> getConfig(String topic){

        //目前Apollo和配置后台共存
        List<DataTransformerConfigDTO> result = Lists.newArrayList();
        List<DataTransformerConfigDTO> apolloConfigList = config.getConfig(topic);
        if (CollectionUtils.isNotEmpty(apolloConfigList)){
            result.addAll(apolloConfigList);
        }
        List<DataTransformerConfigDTO> configList = dataSyncConfig.getConfig(topic);
        if (CollectionUtils.isNotEmpty(configList)){
            result.addAll(configList);
        }
        return result;
    }

    public List<DataTransformer> getDataTransformer(String topic){

        List<DataTransformer> result = Lists.newArrayList();
        final List<DataTransformer> apolloDataTransformers = config.getDataTransformer(topic);
        if(CollectionUtils.isNotEmpty(apolloDataTransformers)){
            result.addAll(apolloDataTransformers);
        }
        final List<DataTransformer> dataSyncConfigDataTransformers = dataSyncConfig.getDataTransformers(topic);
        if(CollectionUtils.isNotEmpty(dataSyncConfigDataTransformers)){
            result.addAll(dataSyncConfigDataTransformers);
        }
        return result;
    }
}