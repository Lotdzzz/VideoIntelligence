from fastapi import APIRouter, Request
from schemas.file.api_url_link_upload_dto import APIURLLinkUploadDTO
from schemas.result import Result
from service.file import analysis_url

router = APIRouter(prefix="/file")


# 接收后端文件模块传来的url链接 解析url链接并返回视频集合数据
@router.post("/geturl", response_model=Result)
def get_url(data: APIURLLinkUploadDTO, request: Request):
    if data.url is None:
        return Result.error(500, "url is none")
    result = analysis_url(data.url, request.app.state.file_config)
    return Result.success(result)
