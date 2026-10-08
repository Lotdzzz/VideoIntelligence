import asyncio
import task.thread_factory.service_pool.audio_translation_to_word_pool as pool
from funasr.auto.auto_model import AutoModel
from funasr.utils.postprocess_utils import rich_transcription_postprocess

# 加载模型
# 全局加载一次
whisper_model = AutoModel(
    model="iic/SenseVoiceSmall",
    vad_model="fsmn-vad",
    vad_kwargs={
        "max_single_segment_time": 30000
    },
    device="cuda:0",
)


# 开启异步线程调用GPU处理语音转文字
async def transcribe_audio(audio_path: str):
    if pool.audio_translation_thread_pool is None:
        print("audio translation thread pool available")
        return None

    return await asyncio.get_running_loop().run_in_executor(
        pool.audio_translation_thread_pool,
        whisper_transcribe,
        audio_path
    )


# 语音转文字
def whisper_transcribe(audio_path: str):
    # 开始转录
    result = whisper_model.generate(
        input=audio_path,
        language="zh",
        use_itn=True,
        batch_size_s=60,
        sentence_timestamp=True,
        disable_pbar=True,
    )

    # 整理结果
    result_segments = []

    for sentence in result[0].get("sentence_info", []):
        text = sentence.get("text", "").strip()

        if not text:
            continue

        result_segments.append({
            "start": sentence["start"] / 1000.0,
            "end": sentence["end"] / 1000.0,
            "text": rich_transcription_postprocess(text),
        })

    return result_segments
