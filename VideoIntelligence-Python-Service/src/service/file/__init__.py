from typing import List
import yt_dlp as yt

from schemas.file.api_urls_info_vo import APIURLsInfoVO


# 使用yt_dlp解析url链接并返回相关数据
def analysis_url(url: str):
    ydl_opts = {
        'quiet': True,
        'no_warnings': True,
        'skip_download': True,  # 关键：只解析不下载
    }

    # noinspection PyTypeChecker
    with yt.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(url, download=False)

    result: List[APIURLsInfoVO] = []

    # 如果是合集/多P/主页，会有 entries
    if 'entries' in info:
        entries = [e for e in info['entries'] if e]
        for i, e in enumerate(entries, 1):
            # 拿到链接信息
            link = e.get('webpage_url') or e.get('url')
            result.append(APIURLsInfoVO(
                url=link,
                title=e.get('title'),
            ))
    else:
        result.append(APIURLsInfoVO(
            url=info.get('webpage_url'),
            title=info.get('title'),
        ))

    return result
