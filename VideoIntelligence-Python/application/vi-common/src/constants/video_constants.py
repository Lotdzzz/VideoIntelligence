from pathlib import Path

BASE_DIR = Path(__file__).resolve().parents[4] / "output"

# 全局视频存储位置
VIDEOS_DIR = BASE_DIR / "videos"
VIDEOS_DIR.mkdir(parents=True, exist_ok=True)

# 全局音频存储位置
AUDIO_DIR = BASE_DIR / "audios"
AUDIO_DIR.mkdir(parents=True, exist_ok=True)

# 视频抽帧存储位置
FRAME_EXTRACTION = BASE_DIR / "frames"
FRAME_EXTRACTION.mkdir(parents=True, exist_ok=True)


# 视频后缀
class Video:
    MP4 = ".mp4"
    MKV = ".mkv"
    AVI = ".avi"
    MOV = ".mov"
    WMV = ".wmv"
    FLV = ".flv"
    WEBM = ".webm"
    M4V = ".m4v"
    MPG = ".mpg"
    MPEG = ".mpeg"
    MPV = ".mpv"
    M2V = ".m2v"
    TS = ".ts"
    M2TS = ".m2ts"
    MTS = ".mts"
    VOB = ".vob"
    RM = ".rm"
    RMVB = ".rmvb"
    ASF = ".asf"
    THREE_GP = ".3gp"  # 数字开头不能直接做属性名
    THREE_G2 = ".3g2"
    QT = ".qt"
    MXF = ".mxf"
    DV = ".dv"
    DIF = ".dif"
    AVCHD = ".avchd"
    BRAWL = ".braw"
    R3D = ".r3d"
    F4V = ".f4v"
    OGV = ".ogv"
    OGG = ".ogg"
    OGM = ".ogm"
    DIVX = ".divx"
    XVID = ".xvid"
    YUV = ".yuv"
    AMV = ".amv"
    WTV = ".wtv"
    TOD = ".tod"
    MOD = ".mod"

    @classmethod
    def all(cls) -> set[str]:
        """返回所有视频后缀的集合（用于批量判断）"""
        return {
            v for k, v in vars(cls).items()
            if not k.startswith("_") and isinstance(v, str)
        }


# 音频后缀
class Audio:
    MP3 = ".mp3"
    WAV = ".wav"
    FLAC = ".flac"
    AAC = ".aac"
    OGG = ".ogg"
    OGA = ".oga"
    WMA = ".wma"
    M4A = ".m4a"
    M4B = ".m4b"
    M4P = ".m4p"
    OPUS = ".opus"
    APE = ".ape"
    ALAC = ".alac"
    AIFF = ".aiff"
    AIF = ".aif"
    AIFC = ".aifc"
    AU = ".au"
    SND = ".snd"
    MID = ".mid"
    MIDI = ".midi"
    AMR = ".amr"
    AC3 = ".ac3"
    DTS = ".dts"
    MKA = ".mka"
    RA = ".ra"
    RAM = ".ram"
    WV = ".wv"
    TTA = ".tta"
    SPX = ".spx"
    CAF = ".caf"
    VOC = ".voc"
    MP2 = ".mp2"
    MPA = ".mpa"
    MPC = ".mpc"
    TAK = ".tak"
    THREE_GA = ".3ga"
    GSM = ".gsm"
    DSS = ".dss"
    MSV = ".msv"
    DVF = ".dvf"
    IVS = ".ivs"
    M4R = ".m4r"
    MMF = ".mmf"
    SF2 = ".sf2"
    SF3 = ".sf3"
    IT = ".it"
    MOD = ".mod"
    XM = ".xm"
    S3M = ".s3m"

    @classmethod
    def all(cls) -> set[str]:
        """返回所有音频后缀的集合，用于批量判断"""
        return {
            v for k, v in vars(cls).items()
            if not k.startswith("_") and isinstance(v, str)
        }
