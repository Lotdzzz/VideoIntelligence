from concurrent.futures import ThreadPoolExecutor

from constants.service_thread_pool_constants import DESCRIBE_FRAMES_THREAD_POOL_MAX_WORKERS

# 这是一个获取视频并下载到本地的专用线程池
describe_frames_thread_pool: ThreadPoolExecutor | None = None


# 抽取音频线程池
async def describe_frames_pool_init():
    global describe_frames_thread_pool
    describe_frames_thread_pool = ThreadPoolExecutor(max_workers=DESCRIBE_FRAMES_THREAD_POOL_MAX_WORKERS)
    print(f"describe_frames thread pool created")


# 线程池销毁
async def describe_frames_pool_destroy():
    global describe_frames_thread_pool
    if describe_frames_thread_pool is not None:
        describe_frames_thread_pool.shutdown(wait=True)
        print(f"describe_frames thread pool destroyed")
