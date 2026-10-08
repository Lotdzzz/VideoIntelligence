from cosumer.analysis_consumer import consumer
from task.thread_factory.consumer_thread_pool import worker_destroy, worker_pool_init
from task.thread_factory.service_pool.audio_translation_to_word_pool import audio_translation_pool_init, \
    audio_translation_pool_destroy
from task.thread_factory.service_pool.extract_audio_pool import extract_audio_pool_destroy, extract_audio_pool_init
from task.thread_factory.service_pool.extract_frames_pool import extract_frames_pool_init, extract_frames_pool_destroy
from task.thread_factory.service_pool.normal_mission_pool import normal_mission_pool_init, normal_mission_pool_destroy
from task.thread_factory.task_thread_pool import task_pool_init, task_pool_destroy


# 初始化启动线程池
async def init_thread_pool():
    # 启动mq的消费者线程组
    await worker_pool_init(consumer)
    # 启动任务管理器线程池
    await task_pool_init()
    # 启动获取视频的线程池
    await normal_mission_pool_init()
    # 启动提取音频线程池
    await extract_audio_pool_init()
    # 启动语音转文字线程池
    await audio_translation_pool_init()
    # 启动抽帧线程池
    await extract_frames_pool_init()


# 关机线程池
async def destroy_thread_pool():
    # 关闭mq的消费者线程组
    await worker_destroy()
    # 管理任务管理器线程池
    await task_pool_destroy()
    # 获取视频的线程池销毁
    await normal_mission_pool_destroy()
    # 提取音频线程池销毁
    await extract_audio_pool_destroy()
    # 语音转文字线程池销毁
    await audio_translation_pool_destroy()
    # 销毁抽帧线程池
    await extract_frames_pool_destroy()
