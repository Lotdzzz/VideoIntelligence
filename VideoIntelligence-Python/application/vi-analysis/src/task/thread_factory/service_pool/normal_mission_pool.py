from concurrent.futures import ThreadPoolExecutor

from constants.service_thread_pool_constants import NORMAL_MISSION_THREAD_POOL_MAX_WORKERS

# 这是一个处理普通任务的线程池
normal_mission_thread_pool: ThreadPoolExecutor | None = None


# 处理普通任务的线程池初始化
async def normal_mission_pool_init():
    global normal_mission_thread_pool
    normal_mission_thread_pool = ThreadPoolExecutor(max_workers=NORMAL_MISSION_THREAD_POOL_MAX_WORKERS)
    print(f"fetch video thread pool created")


# 线程池销毁
async def normal_mission_pool_destroy():
    global normal_mission_thread_pool
    if normal_mission_thread_pool is not None:
        normal_mission_thread_pool.shutdown(wait=True)
        print(f"normal_mission thread pool destroyed")
