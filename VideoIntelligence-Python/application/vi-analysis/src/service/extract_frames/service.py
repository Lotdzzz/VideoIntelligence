import asyncio
import shutil
import subprocess
import tempfile

import task.thread_factory.service_pool.extract_frames_pool as pool
from pathlib import Path
from constants.video_constants import VIDEOS_DIR, FRAME_EXTRACTION


# 抽帧
async def extract_frames(filename: str, segments: list):
    if filename is None or not segments:
        print("filename or segments is None")
        return []

    return await asyncio.get_running_loop().run_in_executor(
        pool.extract_frames_thread_pool,
        ffmpeg_extract_frames,
        filename, segments
    )


# 使用ffmpeg生成帧
def ffmpeg_extract_frames(filename: str, segments: list):
    """
    使用 FFmpeg 根据 segment 中点抽帧。
    每个 segment：
        mid_time = (start + end) / 2
    每个时间点单独执行一次 FFmpeg，
    避免构造超长 select 表达式。
    """
    if not filename or not segments:
        return []

    video_path = VIDEOS_DIR / filename

    if not video_path.exists():
        raise FileNotFoundError(
            f"视频不存在: {video_path}"
        )

    # 输出目录
    video_id = Path(filename).stem.split("_", 1)[0]
    frame_dir = FRAME_EXTRACTION / video_id
    frame_dir.mkdir(
        parents=True,
        exist_ok=True
    )
    # 临时目录
    # 防止多个任务同时处理同一个视频时互相覆盖
    temp_dir = Path(
        tempfile.mkdtemp(
            prefix="extract_",
            dir=str(frame_dir)
        )
    )

    frame_paths = []

    try:

        # 逐个 segment 抽帧
        for index, seg in enumerate(segments):

            start = float(seg["start"])
            end = float(seg["end"])

            mid_time = (start + end) / 2

            output_path = (
                    frame_dir
                    / f"frame_{index:04d}.jpg"
            )

            temp_path = (
                    temp_dir
                    / f"frame_{index:04d}.jpg"
            )

            # --------------------------------------------------
            # FFmpeg
            #
            # -ss 放在 -i 前面：
            # 使用输入 seeking，速度更快
            #
            # -frames:v 1：
            # 只输出一帧
            # --------------------------------------------------

            cmd = [
                "ffmpeg",

                "-hide_banner",
                "-loglevel", "error",
                "-y",

                "-ss",
                str(mid_time),

                "-i",
                str(video_path),

                "-frames:v",
                "1",

                "-q:v",
                "2",

                str(temp_path),
            ]

            result = subprocess.run(
                cmd,
                stdout=subprocess.DEVNULL,
                stderr=subprocess.PIPE,
                text=True,
            )

            if result.returncode != 0:
                print(
                    f"抽帧失败: "
                    f"segment={index}, "
                    f"time={mid_time:.3f}"
                )

                print(result.stderr)

                continue

            # 检查 FFmpeg 是否真的生成图片
            if not temp_path.exists():
                print(
                    f"FFmpeg 未生成图片: "
                    f"segment={index}"
                )
                continue

            # 移动到最终目录
            shutil.move(
                str(temp_path),
                str(output_path)
            )

            frame_paths.append({
                "segment_index": index,
                "path": str(output_path),
            })

        return frame_paths

    finally:
        # 清理临时目录
        shutil.rmtree(
            temp_dir,
            ignore_errors=True
        )
