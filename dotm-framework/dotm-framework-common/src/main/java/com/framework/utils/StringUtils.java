package com.framework.utils;

import com.framework.constants.Constants;
import org.apache.commons.lang3.Strings;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author dotm
 */
public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否非空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断对象是否为 null
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * 判断对象是否非 null
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * 比较两个字符串是否相等
     */
    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    /**
     * 忽略大小写比较两个字符串
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    /**
     * 截取字符串
     */
    public static String substring(String str, int beginIndex) {
        if (str == null) {
            return null;
        }
        return str.substring(beginIndex);
    }

    /**
     * 截取指定范围的字符串
     */
    public static String substring(String str, int beginIndex, int endIndex) {
        if (str == null) {
            return null;
        }
        return str.substring(beginIndex, endIndex);
    }

    /**
     * 截取分隔符之前的字符串
     */
    public static String substringBefore(String str, String separator) {
        if (isEmpty(str) || isEmpty(separator)) {
            return str;
        }

        int index = str.indexOf(separator);
        return index < 0 ? str : str.substring(0, index);
    }

    /**
     * 截取分隔符之后的字符串
     */
    public static String substringAfter(String str, String separator) {
        if (isEmpty(str) || isEmpty(separator)) {
            return "";
        }

        int index = str.indexOf(separator);
        return index < 0 ? "" : str.substring(index + separator.length());
    }

    /**
     * 去除字符串首尾空白
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 判断字符串是否包含实际文本内容
     */
    public static boolean hasText(String str) {
        if (isEmpty(str)) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否为http(s)://开头
     *
     * @param link 链接
     * @return 结果
     */
    public static boolean isHttp(String link) {
        return StringUtils.startsWithAny(link, Constants.HTTP, Constants.HTTPS);
    }

    /**
     * 检查字符串是否以任意前缀开始
     *
     * @param sequence      要检查的字符串
     * @param searchStrings 区分大小写的字符串前缀数组
     * @return 结果
     */
    public static boolean startsWithAny(final CharSequence sequence, final CharSequence... searchStrings) {
        return Strings.CS.startsWithAny(sequence, searchStrings);
    }

    /**
     * 查找指定字符串是否匹配指定字符串列表中的任意一个字符串
     *
     * @param str  指定字符串
     * @param stringList 需要检查的字符串数组
     * @return 是否匹配
     */
    public static boolean matches(String str, List<String> stringList) {
        if (isEmpty(str) || isEmpty(stringList)) {
            return false;
        }
        for (String pattern : stringList) {
            if (str.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * * 判断一个Collection是否为空， 包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Collection<?> coll) {
        return isNull(coll) || coll.isEmpty();
    }
}
