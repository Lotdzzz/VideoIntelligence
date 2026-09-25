from typing import Generic, TypeVar, Optional, Any
from pydantic import BaseModel

T = TypeVar('T')

# 2. 定义响应结果模型（对应 Java 的 Result<T>）
class Result(BaseModel, Generic[T]):
    code: int
    msg: str
    data: Optional[T] = None

    # 模拟 Java 的 success() 静态方法
    @classmethod
    def success(cls, data: T = None, msg: str = "success") -> "Result[T]":
        return cls(code=200, msg=msg, data=data)

    # 模拟 Java 的 error() 静态方法
    @classmethod
    def error(cls, code: int = 500, msg: str = "error") -> "Result[Any]":
        return cls(code=code, msg=msg, data=None)