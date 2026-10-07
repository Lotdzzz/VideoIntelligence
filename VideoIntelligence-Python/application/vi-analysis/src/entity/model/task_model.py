from pydantic import BaseModel

from entity.schemas.dto.file_dto import ViFileDTO


# 设置任务模型
class Task(BaseModel):
    # 任务id
    task_id: float
    # 任务状态
    task_status: str
    # 任务对象
    task_data: ViFileDTO
