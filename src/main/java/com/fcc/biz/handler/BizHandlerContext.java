package com.fcc.biz.handler;

import com.ctrip.framework.apollo.Config;
import com.fcc.biz.handler.constant.ConfigConstant;

public class BizHandlerContext {

    @com.ctrip.framework.apollo.spring.annotation.ApolloConfig(ConfigConstant.NAMESPACE)
    private Config config;
}
