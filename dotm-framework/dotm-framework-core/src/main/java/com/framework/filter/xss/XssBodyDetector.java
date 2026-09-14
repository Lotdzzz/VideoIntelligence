package com.framework.filter.xss;

import org.apache.commons.text.StringEscapeUtils;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author dotm
 */
public class XssBodyDetector {

    private static final List<Pattern> PATTERNS = new ArrayList<>();

    static {
        // 脚本标签及其变种
        add("(?i)<\\s*script[^>]*>.*?<\\s*/\\s*script\\s*>");
        add("(?i)<\\s*script[^>]*src\\s*=");
        // HTML 事件处理器
        add("(?i)<[^>]+on\\w+\\s*=\\s*['\"]?[^>]*>");
        // 伪协议
        add("(?i)javascript\\s*:");
        add("(?i)vbscript\\s*:");
        add("(?i)data\\s*:\\s*text/html");
        // 动态执行函数
        add("(?i)expression\\s*\\(");
        add("(?i)eval\\s*\\(");
        add("(?i)alert\\s*\\(");
        add("(?i)confirm\\s*\\(");
        add("(?i)prompt\\s*\\(");
        add("(?i)document\\s*\\.\\s*cookie");
        add("(?i)window\\s*\\.\\s*location");
        // 危险标签属性
        add("(?i)<[^>]+src\\s*=\\s*['\"]?[^>]*['\"]?[^>]*>");
        add("(?i)<[^>]+data\\s*=\\s*['\"]?[^>]*['\"]?[^>]*>");
        add("(?i)<[^>]+href\\s*=\\s*['\"]?\\s*javascript\\s*:");
        // 特殊标签
        add("(?i)<\\s*iframe[^>]*src\\s*=");
        add("(?i)<\\s*embed[^>]*src\\s*=");
        add("(?i)<\\s*object[^>]*data\\s*=");
        add("(?i)<\\s*svg[^>]*onload\\s*=");
        add("(?i)<\\s*img[^>]*onerror\\s*=");
        // CSS 表达式
        add("(?i)style\\s*=\\s*['\"][^'\"]*expression\\s*\\(");
        // 兜底：任何标签开始（谨慎使用，可根据需要注释）
        add("<\\s*[a-zA-Z][^>]*>");
    }

    private static void add(String regex) {
        PATTERNS.add(Pattern.compile(regex));
    }

    /**
     * 检测字节数组是否包含 XSS 攻击载荷
     * @param bytes   请求体字节
     * @param charset 字符集（通常从 request.getCharacterEncoding() 获取）
     * @return true 表示检测到攻击
     */
    public static boolean containsXss(byte[] bytes, Charset charset) {
        if (bytes.length == 0) return false;
        String content = new String(bytes, charset);
        String normalized = normalize(content);
        for (Pattern p : PATTERNS) {
            if (p.matcher(normalized).find()) {
                return true;
            }
        }
        return false;
    }

    // ---------- 多层解码（递归直至稳定） ----------
    private static String normalize(String input) {
        String prev, curr = input;
        do {
            prev = curr;
            curr = decodeHtmlEntities(curr);
            curr = decodeUrl(curr);
            curr = decodeUnicodeEscapes(curr);
        } while (!curr.equals(prev));
        return curr;
    }

    private static String decodeHtmlEntities(String s) {
        return StringEscapeUtils.unescapeHtml4(s);
    }

    private static String decodeUrl(String s) {
        try {
            String decoded = s;
            String previous;
            do {
                previous = decoded;
                decoded = URLDecoder.decode(decoded, StandardCharsets.UTF_8);
            } while (!decoded.equals(previous));
            return decoded;
        } catch (Exception e) {
            return s;
        }
    }

    private static String decodeUnicodeEscapes(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == '\\' && i + 5 < s.length() && s.charAt(i + 1) == 'u') {
                try {
                    int code = Integer.parseInt(s.substring(i + 2, i + 6), 16);
                    sb.append((char) code);
                    i += 6;
                    continue;
                } catch (NumberFormatException ignored) {}
            }
            sb.append(s.charAt(i));
            i++;
        }
        return sb.toString();
    }
}
