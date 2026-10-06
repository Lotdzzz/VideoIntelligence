import subprocess
import os
from pathlib import Path

import cv2
import whisper
from dotenv import load_dotenv
from numba.core.utils import format_time
from openai import OpenAI

os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"
from transformers import BlipProcessor, BlipForConditionalGeneration
from PIL import Image
from constants.video_constants import VIDEOS_DIR, AUDIO_DIR, Audio, FRAME_EXTRACTION
from entity.schemas.dto.file_dto import ViFileDTO
from exception.file.file_not_found_exception import FileNotFoundException
from minio_config.service import get_file_to_local
from utils.file_utils import remove_suffix


# 视频预处理层
def video_pre_handler(video: ViFileDTO):
    if video is None:
        raise FileNotFoundException(msg="video not found")
    # 获取视频到本地
    filename = fetch_video(video.objectName)
    # 提取音频
    audio_path = extract_audio(filename=filename)
    # 语音转文字
    segments = transcribe_audio(audio_path=audio_path)
    # 抽帧
    frames = extract_frames(filename=filename, segments=segments)
    # 关键帧画面描述
    descriptions = describe_frames(frame_paths=frames)
    # 合并时间线
    timelines = build_timeline(segments=segments, descriptions=descriptions)
    # 构建上下文
    context = build_context(timeline=timelines)
    # 调用大语言模型
    response = ask_deepseek(context=context, question="生成知识点大纲以及思维导图大纲")
    print(response)


# 获取视频
def fetch_video(url: str | None):
    if url is None:
        raise FileNotFoundException(msg="objectname is not exist")
    return get_file_to_local(objectname=url)


# 提取音频 返回音频位置
def extract_audio(filename: str) -> str:
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


# 语音转文字
def transcribe_audio(audio_path: str):
    model = whisper.load_model("base")
    result = model.transcribe(audio_path, language="zh")  # 中文视频指定 language="zh"
    segments = []
    for seg in result["segments"]:
        segments.append({
            "start": seg["start"],
            "end": seg["end"],
            "text": seg["text"].strip()
        })
    return segments


# 抽帧
def extract_frames(filename: str, segments: list):
    cap = cv2.VideoCapture(VIDEOS_DIR / filename)
    fps = cap.get(cv2.CAP_PROP_FPS)

    frame_paths = []
    for i, seg in enumerate(segments):
        mid_time = (seg["start"] + seg["end"]) / 2
        frame_no = int(mid_time * fps)
        cap.set(cv2.CAP_PROP_POS_FRAMES, frame_no)
        ret, frame = cap.read()
        if not ret:
            print(f"抽帧失败: segment={i}, frame_no={frame_no}, time={mid_time}")
            continue
        video_id = Path(filename).name.split("_", 1)[0]
        frame_dir = FRAME_EXTRACTION / video_id
        frame_dir.mkdir(parents=True, exist_ok=True)
        path = os.path.join(frame_dir, f"frame_{i:04d}.jpg")
        success = cv2.imwrite(str(path), frame)
        if not success:
            print(f"图片保存失败: {path}")
            continue
        frame_paths.append({"segment_index": i, "path": path})

    cap.release()
    return frame_paths


# 关键帧画面描述
def describe_frames(frame_paths: list):
    processor = BlipProcessor.from_pretrained("Salesforce/blip-image-captioning-base")
    model = BlipForConditionalGeneration.from_pretrained("Salesforce/blip-image-captioning-base")

    descriptions = {}
    for item in frame_paths:
        image = Image.open(item["path"]).convert("RGB")
        inputs = processor(image, return_tensors="pt")
        out = model.generate(**inputs)
        caption = processor.decode(out[0], skip_special_tokens=True)
        descriptions[item["segment_index"]] = caption

    return descriptions


# 合并时间线
def build_timeline(segments: list, descriptions: dict):
    timeline = []
    for i, seg in enumerate(segments):
        timeline.append({
            "start": seg["start"],
            "end": seg["end"],
            "asr_text": seg["text"],
            "caption": descriptions.get(i, ""),
        })
    return timeline


# 构建上下文
def build_context(timeline: list, max_chars: int = 6000) -> str:
    lines = []
    for item in timeline:
        start = format_time(item["start"])
        end = format_time(item["end"])
        line = f"[{start}-{end}] 画面：{item['caption']}。语音：{item['asr_text']}"
        lines.append(line)

    context = "\n".join(lines)
    if len(context) > max_chars:
        context = context[:max_chars] + "\n...(已截断)"
    return context


load_dotenv()


# 调用deepseek
def ask_deepseek(context: str, question: str) -> str:
    client = OpenAI(
        api_key=os.environ["API_KEY"],
        base_url="https://api.deepseek.com",
    )

    response = client.chat.completions.create(
        model="deepseek-chat",
        messages=[
            {
                "role": "system",
                "content": (
                    "你是一个视频理解助手。只能根据下面提供的时间线回答，"
                    "每个结论后标注时间戳。时间线没提到的内容，回答不知道。"
                    "画面描述是英文的，请理解后翻译成中文融入回答。"
                ),
            },
            {
                "role": "user",
                "content": f"时间线：\n{context}\n\n问题：{question}",
            },
        ],
        temperature=0.2,
    )
    return response.choices[0].message.content
