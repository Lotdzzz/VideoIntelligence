# 文件模块异常
class FileException(Exception):
    def __init__(self, msg: str):
        self.msg = msg
