from typing import Generic, TypeVar, Optional, Any
from pydantic import BaseModel

T = TypeVar('T')


# 定义响应结果模型
class Result(BaseModel, Generic[T]):
    code: int
    msg: str
    data: Optional[T] = None

    # success() 静态方法
    @classmethod
    def success(cls, data: T = None, msg: str = "success") -> "Result[T]":
        return cls(code=200, msg=msg, data=data)

    # error() 静态方法
    @classmethod
    def error(cls, code: int = 500, msg: str = "error") -> "Result[Any]":
        return cls(code=code, msg=msg, data=None)
