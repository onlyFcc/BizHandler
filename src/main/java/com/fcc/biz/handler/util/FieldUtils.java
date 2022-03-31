package com.fcc.biz.handler.util;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.fcc.biz.handler.config.FieldBO;
import com.fcc.biz.handler.config.FieldConfig;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FieldUtils {
    public static Map<String, Object> getField(Map<String, Object> input, FieldConfig fieldConfig){
        if (fieldConfig == null){
            return input;
        }
        if (fieldConfig.isLowUnderCase() || fieldConfig.isFieldCopy()){
            //若需要转换为下划线，使用copy方式；或者直接指定为copy方式
            return copyField(input, fieldConfig);
        }
        removeField(input, fieldConfig);
        return input;
    }
    private static Map<String, Object> copyField(Map<String, Object> input, FieldConfig fieldConfig){
        Map<String, Object> result = Maps.newHashMapWithExpectedSize(fieldConfig.getNeedFields().size());
        fieldConfig.getNeedFields().forEach(fieldBO ->{
            String key = fieldBO.getField();
            //是否转换成下划线格式
            if (fieldConfig.isLowUnderCase()){
                key = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key);
            }
            result.put(key, input.get(fieldBO.getField()));
        });
        //前缀匹配
        if (CollectionUtils.isNotEmpty(fieldConfig.getNeedFieldsPre())){
            fieldConfig.getNeedFieldsPre().forEach(pre -> {
                input.keySet().forEach(key -> {
                    if (key.startsWith(pre)){
                        result.put(key, input.get(key));
                    }
                });
            });
        }
        return result;
    }
    private static void removeField(Map<String, Object> input, FieldConfig fieldConfig){
        Set<String> fieldSet = fieldConfig.getNeedFields().stream().map(FieldBO::getField).collect(Collectors.toSet());
        input.keySet().removeIf(key-> {
            //前缀匹配
            if (CollectionUtils.isNotEmpty(fieldConfig.getNeedFieldsPre())){
                for (String pre : fieldConfig.getNeedFieldsPre()){
                    if (key.startsWith(pre)){
                        return false;
                    }
                }
            }
            return !fieldSet.contains(key);
        });
    }
}
