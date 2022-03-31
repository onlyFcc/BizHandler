package com.fcc.biz.handler.config;

import lombok.Getter;

public enum ConfigTypeEnum {
    APOLLO(1, "apollo"),
    DUBBO(2, "dubbo"),
    ;

    ConfigTypeEnum(int type, String desc){
        this.type = type;
        this.desc = desc;
    }

    @Getter
    private int type;
    @Getter
    private String desc;
}
