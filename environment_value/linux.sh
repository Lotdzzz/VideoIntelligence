#!/usr/bin/env bash

# nacos配置
export NACOS_SERVER_HOST_PORT="192.168.99.128:8848"
export NACOS_GROUP="DOTM_GROUP"

# 消息队列配置
export RABBITMQ_SERVER_HOST="192.168.99.128"
export RABBITMQ_SERVER_PORT="5672"
export RABBITMQ_SERVER_USERNAME="rabbitmq"
export RABBITMQ_SERVER_PASSWORD="rabbitmq"

# 数据库配置
export DATASOURCE_HOST_PORT="192.168.99.128:3306"
export DATASOURCE_DATABASES="vi"
export DATASOURCE_USERNAME="root"
export DATASOURCE_PASSWORD="mysql_AWHzE5"

# redis配置
export REDIS_HOST="192.168.99.128"
export REDIS_PORT="6379"
export REDIS_PASSWORD="redis_Btc43t"

# 网关配置
export GATEWAY_PROFILES="gateway,common"

# 认证中心配置
export AUTH_PROFILES="auth,dev,common"

# 微服务端口配置
export GATEWAY_PORT="8080"          # 网关
export AUTH_PORT="1000"             # 认证中心
export AUTH_MONITOR_PORT="9999"     # 认证中心监控
export SYSTEM_MONITOR_PORT="9998"   # 系统监控
export FILE_PORT="9997"             # 文件