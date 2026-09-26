import yt_dlp
import imageio_ffmpeg

def download_first_part(video_url):
    # 获取 imageio-ffmpeg 自带的 ffmpeg 可执行文件路径
    ffmpeg_path = imageio_ffmpeg.get_ffmpeg_exe()

    ydl_opts = {
        'playlist_items': '1',
        'outtmpl': './downloads/%(title)s.%(ext)s',
        'quiet': False,
        'no_warnings': True,
        # 关键：告诉 yt-dlp 去哪里找 ffmpeg
        'ffmpeg_location': ffmpeg_path,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.download([video_url])

if __name__ == "__main__":
    download_first_part("https://www.bilibili.com/video/BV1r94y1b7eS")