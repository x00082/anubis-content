package cn.com.pingan.cdn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Classname AnubisContentConfig
 * @Description TODO
 * @Date 2020/10/23 10:00
 * @Created by Luj
 */
@Configuration
public class AnubisContentConfig {
    public @Bean
    ApplicationContextProvider l() { return new ApplicationContextProvider(); }
}
