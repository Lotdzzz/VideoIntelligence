from service.LLM_build_result.service import ask_deepseek, video_prompt
from service.build_context.service import build_context
from service.build_timeline.service import build_timeline
from service.describe_frames.service import describe_frames
from service.extract_audio.service import extract_audio
from service.extract_frames.service import extract_frames
from service.transcribe_audio_to_word.service import transcribe_audio
from service.video_fetch.service import fetch_video
from entity.schemas.dto.file_dto import ViFileDTO

"""
视频分析主要核心任务步骤：
处理阶段使用线程池承载各个阶段的业务处理
因为本模块在aio的loop内
所以await时不会阻塞而是异步执行其他任务
"""


# 视频预处理层
async def video_handler(video: ViFileDTO):
    # 获取视频到本地
    filename = await fetch_video(video.objectName)
    if filename is None:
        print("filename is None")
        return None

    # 提取音频
    audio_path = await extract_audio(filename=filename)
    if audio_path is None:
        print("audio_path is None")
        return None

    # 语音转文字
    segments = await transcribe_audio(audio_path=audio_path)
    if segments is None:
        print("segments is None")
        return None

    # 抽帧
    frames_path = await extract_frames(filename=filename, segments=segments)
    if frames_path is None:
        print("frames_path is None")
        return None

    # 关键帧画面描述
    descriptions = await describe_frames(frame_paths=frames_path)
    if descriptions is None:
        print("descriptions is None")
        return None

    # 合并时间线
    timelines = await build_timeline(segments=segments, descriptions=descriptions)
    if timelines is None or len(timelines) == 0:
        print("timelines is None")
        return None

    # 构建上下文
    context = await build_context(timeline=timelines)
    if context is None:
        print("context is None")
        return None

    # 调用大语言模型
    response = await ask_deepseek(context=context, question=video_prompt)
    if response is None:
        print("response is None")
        return None
    return response
