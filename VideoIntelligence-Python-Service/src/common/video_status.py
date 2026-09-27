from enum import Enum

"""文件状态：0上传中 1已上传 2处理中 3处理完成 4上传失败 5已删除"""


class video_status(Enum):
    UPLOADING = 0
    UPLOADED = 1
    PROCESSING = 2
    SUCCESS = 3
    FAILED = 4
    CANCELED = 5
