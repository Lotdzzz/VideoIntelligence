import asyncio
import task.thread_factory.service_pool.normal_mission_pool as pool

from transformers.utils.notebook import format_time


# 构建上下文
async def build_context(timeline: list):
    return await asyncio.get_running_loop().run_in_executor(
        pool.normal_mission_thread_pool,
        build_context_service,
        timeline,
        6000
    )


def build_context_service(timeline: list, max_chars: int = 6000) -> str:
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
