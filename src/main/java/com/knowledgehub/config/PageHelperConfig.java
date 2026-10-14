package com.knowledgehub.config;

import com.github.pagehelper.PageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class PageHelperConfig {

    /**
     * 注册PageHelper分页插件，由现有MyBatis-Plus配置加载。
     *
     * @return 分页拦截器
     */
    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor interceptor = new PageInterceptor();

        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("reasonable", "false");

        interceptor.setProperties(properties);
        return interceptor;
    }
}