import subprocess
from pathlib import Path
import os

from dotenv import load_dotenv
from numba.core.utils import format_time
from openai import OpenAI

os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"
from transformers import BlipProcessor, BlipForConditionalGeneration
from PIL import Image
import cv2
import whisper
import yt_dlp

# 设置本地存储文件路径 项目根目录下的 downloads 文件夹
DOWNLOADS_DIR = Path(__file__).resolve().parents[2] / "pin"
DOWNLOADS_DIR.mkdir(parents=True, exist_ok=True)


def download_video():
    url = "https://www.bilibili.com/video/BV1xTqnBfEaa/?image_material_id=107592232&from_bcg=bcg_11_3441980180&trackid=web_pegasus_0.router-web-pegasus-2479516-f6fct.1790930732896.469&creative_id=3441980180&title_encode=%E8%BF%99%E5%A4%A7%E6%A6%82%E5%B0%B1%E6%98%AF%E6%88%91%E4%B8%BA%E4%BB%80%E4%B9%88%E5%A4%A7%E4%B8%89%E4%BA%86%E6%89%8D%E8%80%83%E7%A0%94%E7%9A%84%E5%8E%9F%E5%9B%A0%E5%90%A7%21&track_id=pbaes.8Z5z-p2qx39BLR6dqnCLQqz_VQmBCKeBdR09cFLJ62WN3YdWR3XQDjByOlyokICdEuKCFiZaQfwhfypiZHjRMLMDQDPjjE_aoO82W9k8gyoKudjt8hUWGG1png2rBjlEfvS_aZWwasKnsC3-bhufD-mDWeAoxzpFL7P58zz4JxxgGF5C55owIFsdU7PoRPscuwOM3QNnV18ytmRuhRi-ng&caid=__CAID__&resource_id=__RESOURCEID__&source_id=5614&request_id=1790930732914q10a89a33a124q5446&from_spmid=__FROMSPMID__&title_material_id=189909213&linked_creative_id=3696171458&vd_source=cc7c91e214cab08f67a0c0a813c18647"  # 换成你的 B 站视频链接

    with yt_dlp.YoutubeDL() as ydl:
        ydl.download([url])


# 提取音频
def extract_audio(video_path: str, audio_path: str):
    subprocess.run([
        "ffmpeg", "-y",
        "-i", video_path,
        "-vn",  # 不要视频流
        "-acodec", "pcm_s16le",
        "-ar", "16000",  # 16kHz 采样率，Whisper 要求
        "-ac", "1",  # 单声道
        audio_path
    ], check=True)


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
def extract_frames(video_path: str, segments: list, output_dir: str):
    cap = cv2.VideoCapture(video_path)
    fps = cap.get(cv2.CAP_PROP_FPS)
    os.makedirs(output_dir, exist_ok=True)

    frame_paths = []
    for i, seg in enumerate(segments):
        mid_time = (seg["start"] + seg["end"]) / 2
        frame_no = int(mid_time * fps)
        cap.set(cv2.CAP_PROP_POS_FRAMES, frame_no)
        ret, frame = cap.read()
        if not ret:
            continue
        path = os.path.join(output_dir, f"frame_{i:04d}.jpg")
        cv2.imwrite(path, frame)
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

if __name__ == "__main__":
    # extract_audio(
    #     "D:\\Project\\video-intelligence\\Video-Intelligence\\VideoIntelligence-Python\\application\\vi-framework\\videos\\28考研破解版，暑假后升大三在校考研党人手一份 [BV1xTqnBfEaa].mp4",
    #     "D:\\Project\\video-intelligence\\Video-Intelligence\\VideoIntelligence-Python\\application\\vi-framework\\videos\\28考研破解版，暑假后升大三在校考研党人手一份 [BV1xTqnBfEaa].wav"
    # )
    segments = transcribe_audio(
        "D:\\Project\\video-intelligence\\Video-Intelligence\\VideoIntelligence-Python\\application\\vi-framework\\videos\\28考研破解版，暑假后升大三在校考研党人手一份 [BV1xTqnBfEaa].wav")
    print(segments)
    paths = extract_frames(
        "D:\\Project\\video-intelligence\\Video-Intelligence\\VideoIntelligence-Python\\application\\vi-framework\\videos\\28考研破解版，暑假后升大三在校考研党人手一份 [BV1xTqnBfEaa].mp4",
        segments,
        str(DOWNLOADS_DIR)
    )
    print(paths)
    describe = describe_frames(paths)
    print(describe)
    timeline = build_timeline(segments, describe)
    print(timeline)
    context = build_context(timeline)
    print(context)
    response = ask_deepseek(context, "生成知识点大纲")
    print(response)
