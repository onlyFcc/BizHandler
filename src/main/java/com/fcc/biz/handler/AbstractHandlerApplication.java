package com.fcc.biz.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fcc.biz.handler.config.*;
import com.fcc.biz.handler.config.TargetConfig;
import com.fcc.biz.handler.handler.Handler;
import com.fcc.biz.handler.handler.HandlerTypeEnum;
import com.fcc.biz.handler.handler.target.TargetHandler;
import com.fcc.biz.handler.handler.target.TargetHandlerFactory;
import com.fcc.biz.handler.retry.RetryConfig;
import com.fcc.biz.handler.util.FieldUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 目前仅支持一个项目统一一种配置处理流程
 */
@Slf4j
public abstract class AbstractHandlerApplication implements HandlerApplication{
    private static final TypeReference<Map<String, Object>> INPUT_TYPE = new TypeReference<Map<String, Object>>() {
    };
    private static final String MESSAGE_TIMESTAMP = "auto_message_timestamp";

    //需要补充数据的，走异步
    private static final int POOL_NUM = 12;
    private static final Map<Long, ExecutorService> poolExecutorMap =
            new HashMap<>();
    static {
        for (long i = 0; i < POOL_NUM; i++){
            poolExecutorMap.put(i, Executors.newSingleThreadExecutor());
        }
    }

    private Class<?> contextClass;
    private BizHandlerContext context;

    @Autowired
    @Qualifier("dataTransformRetryTemplate")
    private RetryTemplate syncDataRetryTemplate;

    public void init(){
        //初始化Context
        //context = this.createContext();
        //上下文准备工作
        prepareContext(context);
        //run();
    }

//    protected BizHandlerContext createContext() {
//        return
//    }
    private void prepareContext(BizHandlerContext context){

    }

    public Object doRun(String scene, Object data){
        //获取流程配置
        BizFlowConfig bizFlowConfig = getFlowConfigs();
        //根据配置类型，获取对应配置
        BizConfig bizConfig = getConfig(scene, bizFlowConfig.getConfigType());
        if (bizConfig == null || CollectionUtils.isEmpty(bizConfig.getBizConfigs())){
            return null;
        }
        if (bizConfig.isReturnData()){
            //需要返回有效的实体数据，则该业务处理流程只允许有一条
            return execute(scene, bizConfig.getBizConfigs().get(0), data, true);
        }
        //数据流处理，多条业务配置时是否允许并发
        if (bizConfig.getBizConfigs().size() > 1 && bizConfig.isParallel()){
            bizConfig.getBizConfigs().parallelStream().forEach(n -> execute(scene, n, data, false));
        } else {
            bizConfig.getBizConfigs().forEach(n -> execute(scene, n, data, false));
        }
        return true;
    }

    private Object execute(String scene, OneBizConfig config, Object data, boolean returnData){
        //序列化之前操作：针对一些qps过大的topic，序列化之前判断本次消息是否需要，不需要则可跳过序列化操作
        boolean need = needSerializable(config, data);
        if (!need){
            return false;
        }
        /*
         * 由于多个业务会相互干扰，故每一个业务需要重新反序列化原始数据
         * 反序列化数据，目前maserati消息中，一条消息可能有多条数据变更消息
         */
        Map<String, Object> input = dataDeserialize(scene, data);
        try {
            return handle(scene, config, input, returnData);
        } catch (Exception e){
            // 若出现网络等异常，且配置为需要重试，使用异步重试机制
            if (config.isNeedRetry() && RetryConfig.alwaysRetry(e)){
                return handleWithRetry(scene, config, input, returnData);
            } else {
                log.error("");
                return false;
            }
        }
    }

    //业务处理逻辑
    private Object handle(String scene, OneBizConfig configDTO, Map<String, Object> input, boolean returnData) throws Exception{
        //链式处理流程
        //1.使用sourceFilters对数据进行处理
        for (Handler handler : configDTO.getHandlerList()) {
            final Boolean res = handler.handler(input);
            if (!res || input == null || input.isEmpty()) {
                return false;
            }
        }

        if (returnData){
            //获取本次处理所需要的needFieldMap
            return FieldUtils.getField(input, configDTO.getFieldConfig());
        }

        //获取处理器
        boolean res = true;
        if (!CollectionUtils.isEmpty(configDTO.getTargetList())){
            for (TargetConfig targetConfig: configDTO.getTargetList()){
                //获取本次处理所需要的needFieldMap
                Map<String, Object> needFieldMap = FieldUtils.getField(input, targetConfig.getFieldConfig());
                boolean temp = doHandler(scene, needFieldMap, targetConfig);
                if (!temp){
                    //多个数据源的时候，其中一个失败，默认下一个继续
                    if (targetConfig.getTargetListErrorContinue() != null
                            && !targetConfig.getTargetListErrorContinue()){
                        return false;
                    }
                    res = false;
                }
            }
        }
        return res;
    }
    /**
     * 数据处理，包括：
     *  获取所需要的数据；
     *  获取对应的处理器；
     *  使用处理器处理相应数据
     */
    private boolean doHandler(String scene, Map<String, Object> input, TargetConfig targetConfig) throws Exception{
        TargetHandler targetHandler = TargetHandlerFactory.getHandler(targetConfig.getTargetDataType());
        if (targetHandler == null) {
            return false;
        }

        //处理器处理所需要的数据
        boolean res = targetHandler.handle(scene, input, targetConfig);
        if (!res) {
            log.error("BizHandler: {}, Handler RetryFail!", scene);
            return false;
        }
        return true;
    }
    /**
     * 重试：有序或者无序
     * @return 是否有重试，若无重试，则当失败处理
     */
    private Object handleWithRetry(String scene, OneBizConfig config, Map<String, Object> input, boolean returnData){
        try {
            //为保持有序，需要对指定id进行hash，使用同一个线程执行，若不需要有序，则随机使用线程执行
            long thread;
            String threadHashKey = input.get(config.getThreadHashKeyField()).toString();
            if (threadHashKey == null){
                Random random = new Random();
                thread = random.nextInt(POOL_NUM);
            } else {
                thread = NumberUtils.toLong(threadHashKey, -1)%POOL_NUM;
            }
            //若配置了，threadHashKeyField，但结果数据为空或者错误，不重试
            if (thread >= 0){
                final Future<Object> responseFuture = poolExecutorMap.get(thread).submit(() -> doHandleWithRetry(scene, config, input, returnData));
                return responseFuture.get();
            } else {
                log.error("BizHandler: {}, Handler RetryFail!", scene);
            }
        } catch (Exception e){
            log.error("BizHandler: {}, Handler RetryError!", scene, e);
        }
        return false;
    }
    /**
     * 处理+重试
     * 重试：只针对超时等网络异常，无限重试，重试机制：指数退避等待重试，详见syncDataRetryTemplate
     */
    private Object doHandleWithRetry(String scene, OneBizConfig config, Map<String, Object> input, boolean returnData){
        try {
            return syncDataRetryTemplate.execute(retryContext -> {
                //相关判断应该放在执行方法前面
                int retryCount = retryContext.getRetryCount();

                //次数过多打个点
                if (retryCount == 10) {
                    log.warn("165.bizHandler, value = {}", input);
                }
                Object res = handle(scene, config, input, returnData);

                //本次重试只针对超时等网络异常，重试成功打点
                if (retryCount > 0) {
                    log.warn("BizHandler: {}, RetrySuccess, retryCount: {}", scene, retryCount);
                }

                return res;
            },context -> {
                int retryCount = context.getRetryCount();
                //重试次数记录
                if (retryCount > 0) {
                    log.warn("BizHandler: {}, Retrying, retryCount: {}", scene, retryCount);
                }

                if (context.isExhaustedOnly()){
                    log.warn("BizHandler: {}, Retry max, retryCount: {}", scene, retryCount);
                }
                return true;
            });
        } catch (Exception e){
            log.error("BizHandler: {}, RetryError!", scene, e);
            return null;
        }
    }

    /**
     * 是否需要反序列化，某些高qps场景，需要反序列化的，可以提前筛选，无需反序列化
     */
    private boolean needSerializable(OneBizConfig configDTO, Object data){
        if (!(data instanceof String) || CollectionUtils.isEmpty(configDTO.getPreHandlerStrings())){
            //非String类型或者为空，返回true
            return true;
        }
        for (String preHandlerString: configDTO.getPreHandlerStrings()){
            if (((String)data).contains(preHandlerString)){
                //包含其中一个，即认为需要解析处理该消息
                return true;
            }
        }
        return false;
    }
    //反序列化消息数据
    private Map<String, Object> dataDeserialize(String scene, Object data){
        Map<String, Object> result;
        if (data instanceof String){
            result = JSON.parseObject((String)data, INPUT_TYPE);
        } else {
            result = JSON.parseObject(JSON.toJSONString(data), INPUT_TYPE);
        }

        //添加消息创建时间戳字段
        result.put(MESSAGE_TIMESTAMP, System.currentTimeMillis());

        //数据后置处理
        dataPostDeserialize(scene, result);
        return result;
    }
    public void dataPostDeserialize(String scene, Map<String, Object> input){

    }

    boolean runWithConfig(String scene, T data, OneBizConfig configDTO);

    boolean batchRun(String scene, List<T> dataMap);

    boolean batchRunWithConfig(String scene, List<T> dataList, OneBizConfig configDTO);

    boolean runMap(String scene, Map<String, Object> data);

    boolean runMapWithConfig(String scene, Map<String, Object> data, OneBizConfig configDTO);

    boolean batchRunMap(String scene, List<Map<String, Object>> dataMap);

    boolean batchRunMapWithConfig(String scene, List<Map<String, Object>> dataList, OneBizConfig configDTO);

    /**
     * 默认Apollo配置
     * 默认aviator处理
     * @return
     */
    protected BizFlowConfig getFlowConfigs(){
        BizFlowConfig bizFlowConfig = new BizFlowConfig();
        bizFlowConfig.setConfigType(ConfigTypeEnum.APOLLO.getType());
        bizFlowConfig.setHandleType(HandlerTypeEnum.AVIATOR.getType());
        return bizFlowConfig;
    }

    protected abstract BizConfig getConfig(String scene, int configType);

}
