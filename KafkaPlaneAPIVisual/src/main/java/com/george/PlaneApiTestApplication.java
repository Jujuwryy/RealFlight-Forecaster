package com.george;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import com.george.config.AviationStackConfig;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(AviationStackConfig.class)
public class PlaneApiTestApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
            .directory("./") 
            .ignoreIfMissing()  
            .load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(PlaneApiTestApplication.class, args);
    }
}
