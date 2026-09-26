from pydantic import BaseModel
from typing import Optional


# 定义接收参数的模型
class APIURLsInfoVO(BaseModel):
    title: Optional[str] = None  # 视频标题
    url: Optional[str] = None  # 视频链接
    cover: Optional[str] = None # 视频封面
