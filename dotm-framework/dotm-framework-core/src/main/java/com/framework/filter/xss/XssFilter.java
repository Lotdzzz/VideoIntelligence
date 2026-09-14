package com.framework.filter.xss;

import com.framework.constants.FilterOrderConstants;
import com.framework.properties.XssFilterExcludes;
import com.framework.utils.StringUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dotm
 * 防止XSS攻击的过滤器
 * 本过滤器配置了白名单 主要针对富文本功能 风险对冲
 */
@Component
@Order(FilterOrderConstants.XSS_FILTER_ORDER) // 设置过滤器的优先级，值越小优先级越高
@RequiredArgsConstructor
public class XssFilter implements Filter {

    /**
     * 排除链接
     */
    public List<String> excludes = new ArrayList<>();

    /**
     * xss白名单
     */
    private final XssFilterExcludes xssFilterExcludes;

    /**
     * 白名单过滤
     * 主要针对富文本功能 风险对冲
     *
     * @param filterConfig The configuration information associated with the filter instance being initialised
     * @throws ServletException if an error occurs during the initialization of the filter
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String tempExcludes = String.join(",", xssFilterExcludes.getUrls());
        if (StringUtils.isNotEmpty(tempExcludes)) {
            String[] urls = tempExcludes.split(",");
            excludes.addAll(Arrays.asList(urls));
        }
    }

    /**
     * 请求url豁免 如果是get delete直接豁免
     * 如果不豁免的请求直接用XssHttpServletRequestWrapper包装
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //进行http封装
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        //进行白名单判断并且判断Get Delete 同时进行文件上传豁免
        if (handleExcludeURL(httpRequest, httpResponse) ||
                request.getContentType() != null && request.getContentType().toLowerCase().startsWith("multipart/")) {
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(new XssHttpServletRequestWrapper(httpRequest), response);
    }

    /**
     * 白名单过滤方法
     */
    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getServletPath();
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
            return true;
        }
        return StringUtils.matches(url, excludes);
    }
}
