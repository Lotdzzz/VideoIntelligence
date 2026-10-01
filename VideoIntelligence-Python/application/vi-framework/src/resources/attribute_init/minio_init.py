from resources.global_resources import resources
from resources.properties.minio_properties import MinioProperties


# minio属性赋值
def minio_init(minio_config: dict):
    minio_properties = MinioProperties(
        endpoint=minio_config["endpoint"],
        video_upload_bucket_name=minio_config["video-upload-bucket-name"],
        cover_upload_bucket_name=minio_config["cover-upload-bucket-name"],
    )
    resources.minio = minio_properties

