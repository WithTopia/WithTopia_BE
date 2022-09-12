package com.four.withtopia.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000","https://d2eyce60w9qn52.cloudfront.net")
                .allowedHeaders("*")
                .exposedHeaders("")
                .allowCredentials(true)
                .maxAge(3000);
    }

}
