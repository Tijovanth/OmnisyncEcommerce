package com.omnisynce_ecommerce.order_service.utils;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignTokenInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if(requestAttributes == null){
            return;
        }
        HttpServletRequest request = requestAttributes.getRequest();
        String authorizationToken = request.getHeader("Authorization");
        if (authorizationToken != null) {
            requestTemplate.header(AUTHORIZATION_HEADER, authorizationToken);
        }
    }
}
