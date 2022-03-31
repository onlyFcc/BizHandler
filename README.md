# biz-handler
业务处理工厂：复杂业务插件化处理，包括异步数据源、数据合并、过滤排序，各流程相应的降级方案
#### 说明
1.基于Spring  
2.默认基于Apollo配置，使用Aviator表达式

## 1.配置方式
### 1.1 Apollo配置
Apollo的namespace: biz.handler.apollo  
配置名称的格式为：biz.handler.apollo.config{n}（n为从0开始的数字）
### 1.2 dubbo配置

## 2.编译方式
### 2.1 Aviator
详见https://github.com/javalibrary/aviatorscript
### 2.2 
待补充

## 3.主要处理器
### dubbo handler

### 并发handler