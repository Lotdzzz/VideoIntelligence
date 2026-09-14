package com.framework.filter.xss;

import com.framework.constants.ExceptionConstants;
import com.framework.exception.XssAttackException;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author dotm
 * 请求参数的xss攻击拦截工具
 * 这个类继承了HttpServletRequestWrapper
 * 在SpringMVC中 接口通过@Parameter注解获取请求参数时 会调用getParameterValues方法
 * 在@RestBody 注解中获取请求参数时 会调用getInputStream方法
 * 所以我们需要重写这两个方法
 *
 * @注：本项目目前不进行xss清洗 直接拒绝
 */
@Slf4j
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private byte[] cachedBody;
    private boolean bodyRead = false;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * 检测请求体中的参数
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!bodyRead) {
            // 1. 读取原始请求体并缓存
            ServletInputStream original = super.getInputStream();
            ByteArrayOutputStream baoS = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = original.read(buffer)) != -1) {
                baoS.write(buffer, 0, len);
            }
            cachedBody = baoS.toByteArray();

            // 2. 获取字符集（若 request 未指定则默认 UTF-8）
            String encoding = getRequest().getCharacterEncoding();
            Charset charset = (encoding != null) ? Charset.forName(encoding) : StandardCharsets.UTF_8;

            // 3. 执行 XSS 检测
            boolean xssDetected = XssBodyDetector.containsXss(cachedBody, charset);

            if (xssDetected) {
                throw new XssAttackException(ExceptionConstants.XSS_ATTACK);
            }

            bodyRead = true;
        }
        // 返回可重复读取的缓存流
        return new CachedBodyServletInputStream(cachedBody);
    }

    // ---------- 内部类：可重复读的 ServletInputStream ----------
    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public CachedBodyServletInputStream(byte[] body) {
            this.inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }

    /**
     * 进行路径参数的检测
     */
    @Override
    public String[] getParameterValues(String name) {
        //获取参数列表
        String[] values = super.getParameterValues(name);
        if (values != null) {
            for (String value : values) {
                check(value, name);
            }
        }
        return super.getParameterValues(name);
    }

    /**
     * 检测单个参数
     */
    private void check(String value, String parameterName) {
        XssDetectionResult result = XssDetector.detect(value);
        if (!result.risky()) {
            return;
        }
        throw new XssAttackException(
                ExceptionConstants.XSS_ATTACK
                        + parameterName
                        + "，规则："
                        + result.rule()
        );
    }
}
