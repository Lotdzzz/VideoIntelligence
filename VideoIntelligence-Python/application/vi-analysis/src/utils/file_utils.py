def remove_suffix(filename: str) -> str:
    """去掉字符串中的最后一个后缀。
    例: "demo.mp4" -> "demo"；"a.tar.gz" -> "a.tar"
    """
    return filename.rsplit(".", 1)[0] if "." in filename else filename
