package com.framework.filter.xss;

/**
 * @author dotm
 * XSS 检测结果
 */
public record XssDetectionResult(
        boolean risky,
        String rule,
        String normalizedValue
) {

    public static XssDetectionResult safe(String value) {
        return new XssDetectionResult(false, null, value);
    }

    public static XssDetectionResult risky(
            String rule,
            String value) {

        return new XssDetectionResult(true, rule, value);
    }
}
