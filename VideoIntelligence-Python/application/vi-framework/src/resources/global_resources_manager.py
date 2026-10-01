from resources.attribute_init.minio_init import minio_init


# 全局资源管理器预处理器
async def init_resources(config: dict):
    # minio初始化
    minio_init(config["minio"])
