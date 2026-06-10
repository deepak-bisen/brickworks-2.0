package com.brickwork.security.feign;

import com.brickwork.security.filter.InternalServiceAuthFilter;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InternalServiceFeignInterceptor implements RequestInterceptor {

    @Value("${internal.service.key:change-me}")
    private String internalServiceKey;

    @Override
    public void apply(RequestTemplate template) {
        template.header(InternalServiceAuthFilter.HEADER_NAME, internalServiceKey);
    }
}