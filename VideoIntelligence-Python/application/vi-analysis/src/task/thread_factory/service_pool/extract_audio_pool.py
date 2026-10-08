from concurrent.futures import ThreadPoolExecutor

from constants.service_thread_pool_constants import EXTRACT_AUDIO_THREAD_POOL_MAX_WORKERS

# 这是一个获取视频并下载到本地的专用线程池
extract_audio_thread_pool: ThreadPoolExecutor | None = None


# 抽取音频线程池
async def extract_audio_pool_init():
    global extract_audio_thread_pool
    extract_audio_thread_pool = ThreadPoolExecutor(max_workers=EXTRACT_AUDIO_THREAD_POOL_MAX_WORKERS)
    print(f"extract_audio thread pool created")


# 线程池销毁
async def extract_audio_pool_destroy():
    global extract_audio_thread_pool
    if extract_audio_thread_pool is not None:
        extract_audio_thread_pool.shutdown(wait=True)
        print(f"extract_audio thread pool destroyed")
