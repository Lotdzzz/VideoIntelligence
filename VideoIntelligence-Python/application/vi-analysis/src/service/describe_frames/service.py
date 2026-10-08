import asyncio
import threading
import torch
import task.thread_factory.service_pool.describe_frames_pool as pool
from transformers import BlipProcessor, BlipForConditionalGeneration
from PIL import Image

# BLIP 全局单例
MODEL_NAME = "Salesforce/blip-image-captioning-base"
# 自动选择设备
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"
# 全局只加载一次 Processor
processor = BlipProcessor.from_pretrained(MODEL_NAME)
# 全局只加载一次模型
model = BlipForConditionalGeneration.from_pretrained(MODEL_NAME)
# 移动到 GPU / CPU
model = model.to(DEVICE)
# 推理模式
model.eval()
# 防止多个线程同时调用同一个模型进行推理
model_lock = threading.Lock()


# 关键帧画面描述
async def describe_frames(frame_paths: list):
    return await asyncio.get_running_loop().run_in_executor(
        pool.describe_frames_thread_pool,
        model_describe_frames,
        frame_paths
    )


def model_describe_frames(frame_paths: list):
    global model, processor
    descriptions = {}

    # 同一时间只允许一个线程执行 BLIP 推理
    with model_lock:
        # 推理模式，不计算梯度
        with torch.inference_mode():
            for item in frame_paths:
                # 读取图片
                image = Image.open(
                    item["path"]
                ).convert("RGB")
                try:
                    # 图片预处理
                    inputs = processor(images=image, return_tensors="pt")

                    # 输入移动到 CPU / GPU
                    inputs = {key: value.to(DEVICE) for key, value in inputs.items()}

                    # BLIP 推理
                    out = model.generate(
                        **inputs,
                        max_new_tokens=40,
                        num_beams=3
                    )

                    # 解码
                    caption = processor.decode(
                        out[0],
                        skip_special_tokens=True
                    ).strip()

                    # 保存结果
                    descriptions[
                        item["segment_index"]
                    ] = caption

                finally:
                    # 释放图片资源
                    image.close()

    return descriptions
