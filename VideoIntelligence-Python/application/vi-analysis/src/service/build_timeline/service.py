import asyncio
import task.thread_factory.service_pool.normal_mission_pool as pool


# 合并时间线
async def build_timeline(segments: list, descriptions: dict) -> list:
    return await asyncio.get_running_loop().run_in_executor(
        pool.normal_mission_thread_pool,
        build,
        segments,
        descriptions,
    )


def build(segments: list, descriptions: dict) -> list:
    timeline = []
    for i, seg in enumerate(segments):
        timeline.append({
            "start": seg["start"],
            "end": seg["end"],
            "asr_text": seg["text"],
            "caption": descriptions.get(i, ""),
        })
    return timeline
