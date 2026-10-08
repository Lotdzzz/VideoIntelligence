import asyncio
import threading

"""
这是一个aio的loop循环的模型
方便初始化和调度者使用
"""


class AsyncWorker:

    def __init__(self, worker_id: int):
        self.worker_id = worker_id
        self.loop = asyncio.new_event_loop()

        self.thread = threading.Thread(
            target=self._run,
            name=f"async-worker-{worker_id}",
            daemon=True,
        )

    def _run(self):
        asyncio.set_event_loop(self.loop)

        print(
            f"[AsyncWorker-{self.worker_id}] "
            f"started"
        )

        self.loop.run_forever()

    def start(self):
        self.thread.start()

    def submit(self, coro):
        return asyncio.run_coroutine_threadsafe(
            coro,
            self.loop,
        )
