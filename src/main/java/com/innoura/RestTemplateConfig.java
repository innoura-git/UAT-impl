package com.innoura;




import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    private final OutboundFilter outboundFilter;

    public RestTemplateConfig(OutboundFilter outboundFilter) {
        this.outboundFilter = outboundFilter;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.interceptors(List.of(outboundFilter)).build();
    }
}

