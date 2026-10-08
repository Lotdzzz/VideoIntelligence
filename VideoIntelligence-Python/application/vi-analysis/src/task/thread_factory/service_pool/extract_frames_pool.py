from concurrent.futures import ThreadPoolExecutor

from constants.service_thread_pool_constants import EXTRACT_FRAMES_THREAD_POOL_MAX_WORKERS

# 这是一个抽帧的专用线程池
extract_frames_thread_pool: ThreadPoolExecutor | None = None


# 抽帧线程池初始化
async def extract_frames_pool_init():
    global extract_frames_thread_pool
    extract_frames_thread_pool = ThreadPoolExecutor(max_workers=EXTRACT_FRAMES_THREAD_POOL_MAX_WORKERS)
    print(f"extract_frames thread pool created")


# 线程池销毁
async def extract_frames_pool_destroy():
    global extract_frames_thread_pool
    if extract_frames_thread_pool is not None:
        extract_frames_thread_pool.shutdown(wait=True)
        print(f"extract_frames thread pool destroyed")
