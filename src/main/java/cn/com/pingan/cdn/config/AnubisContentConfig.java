package cn.com.pingan.cdn.config;

import cn.com.pingan.cdn.service.UserRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

/**
 * @Classname AnubisContentConfig
 * @Description TODO
 * @Date 2020/10/23 10:00
 * @Created by Luj
 */
@Configuration
public class AnubisContentConfig {


    private @Autowired
    Environment env;

    public @Bean
    HessianProxyFactoryBean userRpcService() {
        HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
        factory.setServiceUrl(env.getProperty("url.user.service"));
        factory.setServiceInterface(UserRpcService.class);
        return factory;
    }


    public @Bean
    ApplicationContextProvider l() { return new ApplicationContextProvider(); }
}
