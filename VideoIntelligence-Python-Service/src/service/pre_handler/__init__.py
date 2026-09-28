import ffmpeg
from typing import Optional
from pathlib import Path
from config.exception import SystemException
from config.resources import resources
from schemas.file.file_dto import ViFileDTO

# 项目根目录下的 downloads 文件夹
DOWNLOADS_DIR = Path(__file__).resolve().parents[3] / "downloads"
DOWNLOADS_DIR.mkdir(parents=True, exist_ok=True)


# 视频预处理模块
def pre_handler(file: ViFileDTO):
    # 获取视频文件
    file_path = get_video_file(file)
    if encode_video(file_path):
        # 进行下一步处理
        pass
    else:
        # 视频转码
        pass


# 判断视频编码进行转码
def encode_video(url: Optional[str] = None):
    probe = ffmpeg.probe(url)
    for stream in probe['streams']:
        if stream['codec_type'] == 'video':
            if stream.get("codec_name") == "h264":
                return True
    return False


# 从minio获取视频文件
def get_video_file(file: ViFileDTO) -> str | None:
    minio_client = resources.minio
    if minio_client is None:
        raise SystemException("minio client not initialized")
    if file.bucketName and file.objectName:
        minio_client.fget_object(
            bucket_name=file.bucketName,
            object_name=file.objectName,
            file_path=str(DOWNLOADS_DIR / file.objectName)
        )
        return str(DOWNLOADS_DIR / file.objectName)
    return None
