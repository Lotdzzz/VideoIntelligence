package com.framework.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author dotm
 * mp的自动装配类
 * 当 MybatisPlusInterceptor 类存在时，才会加载该配置类
 */
@AutoConfiguration
//onClass扫描的是本项目或依赖有没有这个类的类名 但是这个类可能不在spring容器内
@ConditionalOnClass({MybatisPlusInterceptor.class, PaginationInnerInterceptor.class})
public class MybatisPlusConfig {

    /**
     * 添加分页插件
     */
    @Bean
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
