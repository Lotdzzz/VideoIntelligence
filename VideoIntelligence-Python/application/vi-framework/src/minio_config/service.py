from minio import Minio
from constants.video_constants import VIDEOS_DIR
from minio_config.config import create_minio_client
from resources.global_resources import resources

# 声明minio客户端全局变量
minio_service: Minio


# minio启动器
async def minio_starter():
    global minio_service
    minio_client = create_minio_client()
    bucket = resources.minio.video_upload_bucket_name
    # 判断桶的存在性
    if bucket and not minio_client.bucket_exists(bucket):
        minio_client.make_bucket(bucket)

    minio_service = minio_client
    print("minio started successfully")


# 给出文件名并获取下载到本地并返回视频本地地址
def get_file_to_local(objectname: str) -> str:
    global minio_service
    minio_service.fget_object(
        bucket_name=resources.minio.video_upload_bucket_name,
        object_name=objectname,
        file_path=str(VIDEOS_DIR / objectname)
    )
    return objectname
