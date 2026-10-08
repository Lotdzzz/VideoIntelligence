import asyncio
import task.thread_factory.service_pool.normal_mission_pool as pool
from minio_config.service import get_file_to_local


# 获取视频
async def fetch_video(url: str | None):
    if url is None:
        print("fetch video error because url is None")
        return None
    if pool.normal_mission_thread_pool is None:
        print("fetch video thread pool error because fetch_video_thread_pool is None")
        return None

    # 让aio把任务丢给自定义线程池并让出资源
    return await asyncio.get_running_loop().run_in_executor(
        pool.normal_mission_thread_pool,
        get_file_to_local,
        url
    )
