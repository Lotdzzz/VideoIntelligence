# 系统配置异常
class SystemException(Exception):
    def __init__(self, msg: str):
        self.msg = msg
