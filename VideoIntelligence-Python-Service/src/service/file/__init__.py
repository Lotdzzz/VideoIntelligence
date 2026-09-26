from service.file.bilibili import bilibili_video_handler


# 使用yt_dlp解析url链接并返回相关数据
def analysis_url(url: str, file_config: dict):
    # 拿到视频前缀
    video_prefix = file_config["video-prefix"]
    bilibili_prefix = video_prefix["bilibili"]

    if url.startswith(bilibili_prefix):
        return bilibili_video_handler(url)

    return None
