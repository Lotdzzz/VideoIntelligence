from concurrent.futures import ThreadPoolExecutor
from constants.service_thread_pool_constants import AUDIO_TRANSLATION_THREAD_POOL_MAX_WORKERS

"""
语音转文字线程
因为GPU模型多线程一起调用会造成数据混乱的危险
所以全局单线程处理，让python微服务多实例多进程使用多模型处理达到并发
"""

# 这是一个获取视频并下载到本地的专用线程池
audio_translation_thread_pool: ThreadPoolExecutor | None = None


# 抽取音频线程池
async def audio_translation_pool_init():
    global audio_translation_thread_pool
    audio_translation_thread_pool = ThreadPoolExecutor(max_workers=AUDIO_TRANSLATION_THREAD_POOL_MAX_WORKERS)
    print(f"audio_translation thread pool created")


# 线程池销毁
async def audio_translation_pool_destroy():
    global audio_translation_thread_pool
    if audio_translation_thread_pool is not None:
        audio_translation_thread_pool.shutdown(wait=True)
        print(f"audio_translation thread pool destroyed")
