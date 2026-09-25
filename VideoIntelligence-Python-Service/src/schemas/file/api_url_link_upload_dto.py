from pydantic import BaseModel
from typing import Optional

# 定义接收参数的模型
class APIURLLinkUploadDTO(BaseModel):
    url: Optional[str] = None