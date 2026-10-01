from exception.file.file_exception import FileException


# 链接参数异常
class FileNotFoundException(FileException):
    def __init__(self, msg: str):
        self.msg = msg
