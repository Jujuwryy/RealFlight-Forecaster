package com.george.config;

import com.george.model.Flight;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "aviationstack.api")
public class AviationStackConfig {

    private String url;
    private String key;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Bean
    public WebClient aviationStackWebClient() {
        return WebClient.builder()
                .baseUrl("https://" + url)
                .defaultUriVariables(Map.of("access_key", key))
                .build();
    }
}