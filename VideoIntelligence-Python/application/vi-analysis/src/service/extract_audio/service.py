import asyncio
import subprocess
import task.thread_factory.service_pool.extract_audio_pool as pool
from utils.file_utils import remove_suffix
from constants.video_constants import VIDEOS_DIR, AUDIO_DIR, Audio


# 让aio把任务丢给自定义线程池并让出资源
async def extract_audio(filename: str) -> str | None:
    if pool.extract_audio_thread_pool is None:
        print("extract audio thread pool error")
        return None

    return await asyncio.get_running_loop().run_in_executor(
        pool.extract_audio_thread_pool,
        subprocess_run,
        filename
    )


# 提取音频 返回音频位置
def subprocess_run(filename: str) -> str:
    subprocess.run([
        "ffmpeg", "-y",
        "-i", VIDEOS_DIR / filename,
        "-vn",  # 不要视频流
        "-acodec", "pcm_s16le",
        "-ar", "16000",  # 16kHz 采样率，Whisper 要求
        "-ac", "1",  # 单声道
              AUDIO_DIR / (remove_suffix(filename) + Audio.WAV)
    ], check=True)
    return str(AUDIO_DIR / (remove_suffix(filename) + Audio.WAV))
