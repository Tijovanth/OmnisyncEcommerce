package com.omnisynce_ecommerce.order_service.configs;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductClientConfig {

    @Bean
    public ErrorDecoder customErrorDecoderForProductClient() {
        return new CustomErrorDecoderForProductClient();
    }
}
