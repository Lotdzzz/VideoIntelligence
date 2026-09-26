from config.exception.file.file_exception import FileException


# 调用api权限异常
class PermissionException(FileException):
    def __init__(self, msg: str):
        self.msg = msg
