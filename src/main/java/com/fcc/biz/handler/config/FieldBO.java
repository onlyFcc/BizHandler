package com.fcc.biz.handler.config;

import lombok.Data;

@Data
public class FieldBO {
    private String field;
    private String type;
    private Object value;
}
