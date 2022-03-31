package com.fcc.biz.handler.handler;

import lombok.Getter;

public enum HandlerTypeEnum {
    AVIATOR(1, "aviator"),
    ;

    HandlerTypeEnum(int type, String desc){
        this.type = type;
        this.desc = desc;
    }

    @Getter
    private int type;
    @Getter
    private String desc;
}
