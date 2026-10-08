import os
from dotenv import load_dotenv
from openai import AsyncOpenAI

load_dotenv()

client = AsyncOpenAI(
    api_key=os.environ["API_KEY"],
    base_url="https://api.deepseek.com",
)


# 调用deepseek 因为请求方法是自动aio异步的 所以不需要线程池
async def ask_deepseek(context: str, question: str) -> str | None:
    global client

    response = await client.chat.completions.create(
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
