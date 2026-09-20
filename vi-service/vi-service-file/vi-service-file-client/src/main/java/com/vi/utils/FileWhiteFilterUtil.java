package com.vi.utils;

import java.io.File;
import java.util.List;

/**
 * 文件白名单过滤器
 *
 * @author dotm
 */
public class FileWhiteFilterUtil {

    /**
     * 过滤文件名是否在白名单中
     *
     * @param whiteList 白名单列表
     * @param fileName  文件名
     * @return true表示通过 白名单过滤，false表示不通过
     */
    public static boolean whiteFilter(List<String> whiteList, String fileName) {
        // 判空
        if (whiteList == null || whiteList.isEmpty()) {
            return true;
        }
        // 获取文件扩展名
        String extension = getExtension(fileName);
        return !whiteList.contains(extension);

    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（不含点），如果没有扩展名则返回空字符串
     */
    public static String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        // 去掉路径，只保留文件名（兼容 / 和 \）
        String name = new File(filename).getName();

        int dotIndex = name.lastIndexOf('.');
        // 无扩展名 / 隐藏文件（.gitignore）/ 以点结尾
        if (dotIndex <= 0 || dotIndex == name.length() - 1) {
            return "";
        }
        return name.substring(dotIndex + 1);
    }
}
