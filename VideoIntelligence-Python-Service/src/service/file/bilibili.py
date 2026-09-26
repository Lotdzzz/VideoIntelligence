import re
from urllib.parse import urlencode

import requests
from typing import Optional

from schemas.file.api_urls_info_vo import APIURLsInfoVO

# 用来判断b站视频类型属于单视频还是多视频
BILI_VIEW_API = "https://api.bilibili.com/x/web-interface/view"
# 头信息
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                  "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Referer": "https://www.bilibili.com/"
}
# 基本拼接的下载地址
BASE_DOWNLOAD_URL = "https://api.bilibili.com/x/player/playurl"


# 专门处理b站视频的函数
def bilibili_video_handler(url: str):
    # 判断视频类型 单视频 视频集合 收藏夹
    video_info = extract_url_info(url)

    if video_info["type"] == "favorites":
        pass
    if video_info["type"] == "video":
        data = call_view_api(video_info.get("bvid"), None)
        result = parse_video_link(data["bvid"], data["aid"])
        return result
    return None


# 调用b站api
def call_view_api(bvid: Optional[str] = None, aid: Optional[int] = None) -> dict:
    # 调用b站api拿到结构进行后续视频类型判断
    params = {}
    if bvid or aid:
        params['bvid'] = bvid
        params['aid'] = aid
    else:
        raise ValueError("params error")

    # 开始调用
    resp = requests.get(BILI_VIEW_API, params=params, headers=HEADERS, timeout=10)
    resp.raise_for_status()
    json_data = resp.json()
    if json_data.get("code") != 0:
        # 常见：-404 稿件不存在，-403 权限不足，62002 稿件不可见
        raise RuntimeError(f"B站API错误 code={json_data.get('code')} msg={json_data.get('message')}")
    return json_data["data"]


# 判断视频类型函数
def extract_url_info(url: str):
    # 收藏夹
    m = re.search(r'/list/ml(\d+)', url)
    if m:
        return {'type': 'favorites', 'media_id': m.group(1)}
    m = re.search(r'[?&]fid=(\d+)', url)
    if m:
        return {'type': 'favorites', 'media_id': m.group(1)}
    m = re.search(r'/medialist/detail/ml(\d+)', url)
    if m:
        return {'type': 'favorites', 'media_id': m.group(1)}

    # 视频
    m = re.search(r'/video/(BV[0-9A-Za-z]+)', url)
    if m:
        return {'type': 'video', 'bvid': m.group(1)}
    m = re.search(r'/video/av(\d+)', url)
    if m:
        return {'type': 'video', 'aid': m.group(1)}

    return {'type': 'unsupported'}


# 拼接下载地址
def append_download_url(bvid: Optional[str] = None, cid: Optional[str] = None) -> str:
    if bvid is None or cid is None:
        raise ValueError("bvid 和 cid 不能为空")
    params = {
        "bvid": bvid,
        "cid": cid,
        "qn": 32,
    }
    return f"{BASE_DOWNLOAD_URL}?{urlencode(params)}"


# 分析视频链接属于单p还是多p
def parse_video_link(bvid: Optional[str] = None, aid: Optional[int] = None) -> list:
    data = call_view_api(bvid=bvid, aid=aid)

    bvid = data["bvid"]  # 接口会回填真实的 bvid
    title = data["title"]
    cover = data.get("pic", "")
    owner = data.get("owner", {}).get("name", "")
    duration = data.get("duration", 0)

    # 进行分类之后封装vo结果集
    result = []

    # ---- 情况1：属于某个合集（UGC Season） ----
    if data.get("ugc_season"):
        season = data["ugc_season"]
        idx = 1
        for section in season.get("sections", []):
            for ep in section.get("episodes", []):
                item = APIURLsInfoVO()
                item.title = ep["title"]
                item.cover = ep.get("cover", "") or ep.get("pic", "")
                item.url = append_download_url(ep["bvid"], ep["cid"])
                result.append(item)
                idx += 1

        return result

    # ---- 情况2：多P视频 ----
    pages = data.get("pages", [])
    if len(pages) > 1:
        for page in pages:
            item = APIURLsInfoVO()
            item.title = page["part"] or title
            item.cover = cover
            item.url = append_download_url(bvid, page["cid"])
            result.append(item)
        return result

    # ---- 情况3：单视频（单P） ----
    page = pages[0] if pages else {}
    item = APIURLsInfoVO()
    item.title = title
    item.cover = cover
    item.url = append_download_url(bvid, page.get("cid") or data.get("cid"))
    result.append(item)
    return result
