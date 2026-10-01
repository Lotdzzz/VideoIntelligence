from entity.schemas.dto.file_dto import ViFileDTO
from exception.file.file_not_found_exception import FileNotFoundException
from minio_config.service import get_file_to_local


# 视频预处理层
def video_pre_handler(video: ViFileDTO):
    if video is None:
        raise FileNotFoundException(msg="video not found")
    url = fetch_video(video.objectName)
    print(url)


# 获取视频
def fetch_video(url: str | None):
    if url is None:
        return FileNotFoundException(msg="objectname is not exist")
    return get_file_to_local(objectname=url)
