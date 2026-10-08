"""
任务调度者
内部使用轮询机制把任务分配给aio loop
"""


class TaskDispatcher:
    def __init__(self, workers):
        self.workers = workers
        self.index = 0

    def submit(self, coro):
        worker = self.workers[self.index]
        self.index = (self.index + 1) % len(self.workers)
        return worker.submit(coro)
