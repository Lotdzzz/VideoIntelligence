package com.framework.filter.xss;

import org.apache.commons.text.StringEscapeUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author dotm
 * XSS 检测工具
 * <p>
 * 功能：
 * 1. URL 多次解码
 * 2. HTML Entity 解码
 * 3. Unicode 标准化
 * 4. 控制字符处理
 * 5. XSS 特征检测
 */
public final class XssDetector {

    private XssDetector() {
    }

    /**
     * 最大 URL Decode 次数
     * <p>
     * 防止：
     * %253Cscript%253E
     * <p>
     * 多次编码绕过
     */
    private static final int MAX_DECODE_TIMES = 3;


    /**
     * XSS 检测规则
     * <p>
     * LinkedHashMap 可以保证检测顺序
     */
    private static final Map<String, Pattern> XSS_PATTERNS = new LinkedHashMap<>();


    static {

        /*
         * 1. script 标签
         *
         * <script>
         * <script src=...>
         * < script >
         */
        XSS_PATTERNS.put(
                "SCRIPT_TAG",
                Pattern.compile(
                        "<\\s*/?\\s*script\\b",
                        Pattern.CASE_INSENSITIVE
                )
        );

        /*
         * 2. 高风险标签
         */
        XSS_PATTERNS.put(
                "DANGEROUS_TAG",
                Pattern.compile(
                        "<\\s*/?\\s*"
                                + "(iframe|object|embed|svg|math|applet|base)"
                                + "\\b",
                        Pattern.CASE_INSENSITIVE
                )
        );

        /*
         * 3. HTML 事件属性
         *
         * onclick=
         * onerror=
         * onload=
         * onmouseover=
         */
        XSS_PATTERNS.put(
                "EVENT_HANDLER",
                Pattern.compile(
                        "\\bon[a-zA-Z0-9_-]+\\s*=",
                        Pattern.CASE_INSENSITIVE
                )
        );

        /*
         * 4. javascript: 协议
         *
         * javascript:
         * java script:
         */
        XSS_PATTERNS.put(
                "JAVASCRIPT_PROTOCOL",
                Pattern.compile(
                        "java\\s*script\\s*:",
                        Pattern.CASE_INSENSITIVE
                )
        );

        /*
         * 5. vbscript 协议
         */
        XSS_PATTERNS.put(
                "VBSCRIPT_PROTOCOL",
                Pattern.compile(
                        "vb\\s*script\\s*:",
                        Pattern.CASE_INSENSITIVE
                )
        );

        /*
         * 6. data URI
         *
         * data:text/html
         *
         * 注意：
         * data:image/png 通常是正常的
         *
         * 这里只拦截危险类型
         */
        XSS_PATTERNS.put(
                "DANGEROUS_DATA_URI",
                Pattern.compile(
                        "data\\s*:\\s*"
                                + "(text/html|application/xhtml\\+xml|"
                                + "text/javascript|application/javascript)",
                        Pattern.CASE_INSENSITIVE
                )
        );

        /*
         * 7. CSS expression
         *
         * 老版本 IE XSS
         */
        XSS_PATTERNS.put(
                "CSS_EXPRESSION",
                Pattern.compile(
                        "expression\\s*\\(",
                        Pattern.CASE_INSENSITIVE
                )
        );

        /*
         * 8. CSS javascript URL
         */
        XSS_PATTERNS.put(
                "CSS_JAVASCRIPT",
                Pattern.compile(
                        "url\\s*\\(\\s*['\"]?\\s*"
                                + "java\\s*script\\s*:",
                        Pattern.CASE_INSENSITIVE
                )
        );
    }


    /**
     * 是否存在 XSS 风险
     */
    public static boolean hasXss(String value) {
        return detect(value).risky();
    }


    /**
     * XSS 检测
     */
    public static XssDetectionResult detect(String value) {
        if (value == null || value.isBlank()) {
            return XssDetectionResult.safe(value);
        }

        /*
         * 第一步：
         * 输入规范化
         */
        String normalized = normalize(value);

        /*
         * 第二步：
         * 逐条规则检测
         */
        for (Map.Entry<String, Pattern> entry : XSS_PATTERNS.entrySet()) {
            if (entry.getValue()
                    .matcher(normalized)
                    .find()) {

                return XssDetectionResult.risky(
                        entry.getKey(),
                        normalized
                );
            }
        }

        return XssDetectionResult.safe(normalized);
    }


    /**
     * 输入规范化
     */
    public static String normalize(String value) {

        String result = value;

        /*
         * 1. 多次 URL Decode
         */
        result = urlDecode(result);

        /*
         * 2. HTML Entity Decode
         *
         * &lt;script&gt;
         * ↓
         * <script>
         */
        result = StringEscapeUtils.unescapeHtml4(result);

        /*
         * 3. Unicode Normalization
         *
         * 统一 Unicode 表达形式
         */
        result = Normalizer.normalize(
                result,
                Normalizer.Form.NFKC
        );

        /*
         * 4. 删除危险控制字符
         *
         * 保留：
         * \n
         * \r
         * \t
         */
        result = removeControlCharacters(result);

        /*
         * 5. 标准化特殊空白字符
         */
        result = normalizeWhitespace(result);

        return result;
    }


    /**
     * 多次 URL Decode
     */
    private static String urlDecode(String value) {

        String current = value;

        for (int i = 0;
             i < MAX_DECODE_TIMES;
             i++) {

            try {

                String decoded =
                        URLDecoder.decode(
                                current,
                                StandardCharsets.UTF_8
                        );

                /*
                 * 解码后没有变化
                 * 说明不需要继续
                 */
                if (decoded.equals(current)) {
                    break;
                }

                current = decoded;

            } catch (IllegalArgumentException e) {

                /*
                 * 非法 URL 编码
                 *
                 * 不继续 Decode
                 */
                break;
            }
        }

        return current;
    }


    /**
     * 删除控制字符
     */
    private static String removeControlCharacters(
            String value) {

        StringBuilder builder =
                new StringBuilder(value.length());

        for (int i = 0;
             i < value.length();
             i++) {

            char c = value.charAt(i);

            /*
             * 保留常用空白字符
             */
            if (c == '\n'
                    || c == '\r'
                    || c == '\t') {

                builder.append(c);
                continue;
            }

            /*
             * 删除 ASCII 控制字符
             */
            if (Character.isISOControl(c)) {
                continue;
            }

            builder.append(c);
        }

        return builder.toString();
    }


    /**
     * 空白字符标准化
     */
    private static String normalizeWhitespace(String value) {
        return value
                .replace('\u00A0', ' ')
                .replace('\u200B', ' ')
                .replace('\u200C', ' ')
                .replace('\u200D', ' ');
    }
}