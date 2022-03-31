package com.fcc.biz.handler.retry;

import com.alibaba.dubbo.remoting.TimeoutException;
import com.alibaba.dubbo.rpc.RpcException;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration(value = "bizHandlerRetryConfig")
public class RetryConfig {
    private final static long MINUTE = 60 * 1000;

    @Bean("bizHandlerRetryTemplate")
    public RetryTemplate getSyncDataRetry() {
        RetryTemplate template = new RetryTemplate();

        // 根据异常设置重试策略
        ExceptionClassifierRetryPolicy policy = new ExceptionClassifierRetryPolicy();
        template.setRetryPolicy(policy);

        // 通过setExceptionClassifier来为异常指定重试策略。
        Classifier<Throwable, RetryPolicy> exceptionClassifier = (Classifier<Throwable, RetryPolicy>) classifiable -> {
            if (alwaysRetry(classifiable)) {
                return new AlwaysRetryPolicy();
            }
            //其他异常不重试
            return new NeverRetryPolicy();
        };
        policy.setExceptionClassifier(exceptionClassifier);


        // 指数退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        // 每次等待时间为 等待时间 = 等待时间 * N ，即每次等待时间为上一次的N倍。 (如果等待时间超过最大等待时间，那么以后的等待时间为最大等待时间。)
        // 以下设置 初始时间间隔为1000毫秒，N = 2，¸最大间隔为5分钟，那么从第9次重试开始，以后每次等待时间都为5分钟
        backOffPolicy.setInitialInterval(1000);// 初始等待时间
        backOffPolicy.setMultiplier(2);// 等待倍数
        backOffPolicy.setMaxInterval(5 * MINUTE);//最大等待时间5分钟

        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    //是否需要无限重试：目前网络异常或者网络超时需要
    public static boolean alwaysRetry(Throwable classifiable){
        return classifiable instanceof TimeoutException || classifiable instanceof java.util.concurrent.TimeoutException
                || (classifiable instanceof RpcException &&
                (((RpcException)classifiable).getCode() == RpcException.TIMEOUT_EXCEPTION
                        || ((RpcException)classifiable).getCode() == RpcException.NETWORK_EXCEPTION));
    }
}
