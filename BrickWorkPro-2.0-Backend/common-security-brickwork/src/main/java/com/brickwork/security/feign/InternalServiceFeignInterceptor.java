package com.brickwork.security.feign;

import com.brickwork.security.filter.InternalServiceAuthFilter;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InternalServiceFeignInterceptor implements RequestInterceptor {

    @Value("${internal.service.key:change-me}")
    private String internalServiceKey;

    @Override
    public void apply(RequestTemplate template) {
        log.debug("Attaching internal service key to Feign request {}", template.url());
        template.header(InternalServiceAuthFilter.HEADER_NAME, internalServiceKey);
    }
}