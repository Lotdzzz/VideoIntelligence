#!/bin/bash
# 原 Windows 注册表环境变量转 Linux Shell 环境变量
# 建议保存为 /etc/profile.d/app-env.sh
# 生效：source /etc/profile.d/app-env.sh

# nacos配置
export NACOS_SERVER_HOST_PORT=""
export NACOS_SERVER_IP=""
export NACOS_GROUP="DOTM_GROUP"
export NACOS_DEFAULT_GROUP="DEFAULT_GROUP"
export NACOS_NAMESPACE="public"
export VI_GROUP="VI_GROUP"

# 消息队列配置
export RABBITMQ_SERVER_HOST=""
export RABBITMQ_SERVER_PORT=""
export RABBITMQ_SERVER_USERNAME=""
export RABBITMQ_SERVER_PASSWORD=""

# 数据库配置
export DATASOURCE_HOST_PORT=""
export DATASOURCE_DATABASES=""
export DATASOURCE_USERNAME=""
export DATASOURCE_PASSWORD=""

# redis配置
export REDIS_HOST=""
export REDIS_PORT=""
export REDIS_PASSWORD=""

# 网关配置
export GATEWAY_PROFILES="gateway,common,auth"
# 认证中心配置
export AUTH_PROFILES="auth,dev,common,ai"
# 认证监控配置
export AUTH_MONITOR_PROFILE="common,dev"
# 系统监控配置
export SYSTEM_MONITOR_PROFILE="common,dev"
# 文件上传模块配置
export FILE_PROFILE="dev,file"

# VI文件模块
export SERVICE_FILE_PROFILE="dev,file,common"

# 微服务端口配置
export GATEWAY_PORT="8080"          # 网关
export AUTH_PORT="1000"             # 认证中心
export AUTH_MONITOR_PORT="9999"     # 认证中心监控
export SYSTEM_MONITOR_PORT="9998"   # 系统监控
export FILE_PORT="9997"             # 文件
# 业务服务端口配置
export SERVICE_FILE="1001"          # VI文件服务
export SERVICE_FILE_PYTHON="1002"   # python端文件服务

# jwt密钥配置
export SECRET_KEY=""

# github认证密钥配置
export CLIENT_ID=""
export CLIENT_SECRET=""

# AI的密钥配置
export API_KEY=""

# 阿里云OSS密钥配置
export OSS_ACCESS_KEY_ID=""
export OSS_ACCESS_KEY_SECRET=""

# MinIO密钥配置
export MINIO_ACCESS_KEY=""
export MINIO_SECRET_KEY=""