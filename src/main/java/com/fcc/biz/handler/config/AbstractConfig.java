package com.fcc.biz.handler.config;

import com.fcc.biz.handler.handler.HandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class AbstractConfig implements Config{

    @Autowired
    private HandlerFactory handlerFactory;

    @Override
    public void init(List<OneBizConfig> configDTOList){
        configDTOList.forEach(configDTO -> configDTO.setHandlerList(handlerFactory.initFilter(configDTO.getHandlers())));
    }
}
