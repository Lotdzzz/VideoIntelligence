import logging
from fastapi import FastAPI, Request

from config.exception.file.file_exception import FileException
from config.exception.system_exception import SystemException
from schemas.result import Result

logger = logging.getLogger(__name__)


# 函数声明式注册异常处理器 不依赖app
def register_exception_handlers(app: FastAPI) -> None:
    # 系统配置异常
    @app.exception_handler(SystemException)
    async def global_exception_handler(request: Request, e: SystemException):
        logger.exception(
            "Unhandled exception",
            extra={
                "path": request.url.path,
                "method": request.method,
                "client": request.client.host if request.client else None,
            },
        )
        return Result.error(msg=e.msg)

    # 文件模块异常
    @app.exception_handler(FileException)
    async def file_exception_handler(request: Request, e: FileException):
        logger.exception(
            "Unhandled exception",
            extra={
                "path": request.url.path,
                "method": request.method,
                "client": request.client.host if request.client else None,
            },
        )
        return Result.error(msg=e.msg)
