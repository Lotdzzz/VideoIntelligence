package com.dotm.utils;

import com.framework.exception.login.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;

import java.net.ConnectException;
import java.util.function.Supplier;

/**
 * 这是一个用于发送请求返回响应体的工具类，主要用于封装请求和响应的相关信息，方便在系统中进行统一处理。
 *
 * @author dotm
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestResponseUtil {

    private final RestTemplate restTemplate;

    /**
     * 发送请求并获取响应体 无需强转
     *
     * @param request      请求体
     * @param url          请求地址
     * @param responseType 响应体类型
     * @param <T>          响应体类型
     * @return 响应体
     */
    public <T> T post(HttpEntity<MultiValueMap<String, String>> request, String url, Class<T> responseType) {
        return execute(() -> restTemplate.postForEntity(url, request, responseType));
    }

    /**
     * get请求
     *
     * @param request      请求体
     * @param url          请求地址
     * @param responseType 响应体类型
     * @param <T>          响应体类型
     * @return 响应体
     */
    public <T> T get(HttpEntity<MultiValueMap<String, String>> request, String url, Class<T> responseType) {
        return execute(() -> restTemplate.exchange(url, HttpMethod.GET, request, responseType));
    }

    /**
     * 兼容不同 HTTP client 的超时判断
     */
    private boolean isTimeout(Throwable cause) {
        return cause instanceof ConnectException
                || cause instanceof java.io.InterruptedIOException
                || (cause != null
                && cause.getClass().getSimpleName().contains("ConnectTimeoutException"));
    }

    /**
     * 执行请求并处理异常
     * 这个方法主要是解决get post请求的异常冗余代码
     *
     * @param caller 这个参数就是获取函数式接口传入的执行方法的返回值
     * @param <T>    响应体类型
     * @return 响应体
     */
    private <T> T execute(Supplier<ResponseEntity<T>> caller) {
        try {
            ResponseEntity<T> resp = caller.get();
            return resp.getBody();
        } catch (RestClientException e) {
            throw translate(e);
        }
    }

    /**
     * 判断异常进行转换
     *
     * @param e 异常
     * @return 转换后的异常
     */
    private RuntimeException translate(RestClientException e) {
        if (e instanceof ResourceAccessException) {
            // 网络层：超时 / 连不上 / DNS 失败
            Throwable cause = e.getCause();
            if (isTimeout(cause)) {
                return new LoginTimeOutException(e.getMessage());
            }
            return new LoginNetError(e.getMessage());
        }

        if (e instanceof HttpClientErrorException) {
            // 4xx：参数错误、code 失效、client 配置错误
            return new LoginAuthRequestError(e.getMessage());
        }

        if (e instanceof HttpServerErrorException) {
            // 5xx：对方服务端故障
            return new LoginAuthServiceUnavailableError(e.getMessage());
        }

        // 兜底：序列化失败、未知异常
        return new LoginAuthError(e.getMessage());
    }
}
