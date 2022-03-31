package com.fcc.biz.handler;

import com.fcc.biz.handler.config.OneBizConfig;
import com.fcc.biz.handler.config.BizFlowConfig;
import com.fcc.biz.handler.handler.Handler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * 默认应用处理器
 *  使用Apollo配置
 *  使用aviator脚本语言处理
 * @param <T>
 */
@Component
public class DefaultHandlerApplication<T> {


    @PostConstruct
    public void init0(){

    }



    public boolean execute(String scene, T data){
        //获取配置
        //List<BizConfigDTO> bizConfigDTOS =
        //数据流处理
        //数据转换成Map
        //异常这边统一处理
        //返回处理结果
    }



    public List<OneBizConfig> getConfigs(String scene, int configType){

    }

    public BizFlowConfig getFlowConfigs(){

    }


    public void run(Map<String, Object> data){}

    public void runWithConfig(Map<String, Object> data, OneBizConfig configDTO){

    }

    public void batchRun(List<Map<String, Object>> dataMap){

    }

    public boolean batchRunWithConfig(List<Map<String, Object>> dataList, OneBizConfig configDTO){


        try {
            for (Handler handler : configDTO.getHandlerList()) {
                final Boolean res = handler.batchHandler(dataList);
                if (!res) {
                    return false;
                }
            }
            return true;
        } catch (Exception e){

        }
        return false;
    }
}
