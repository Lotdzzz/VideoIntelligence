from config.exception.file.file_exception import FileException


# 链接参数异常
class URLParamException(FileException):
    def __init__(self, msg: str):
        self.msg = msg
