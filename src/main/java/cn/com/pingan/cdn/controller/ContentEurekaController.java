package cn.com.pingan.cdn.controller;

import cn.com.pingan.cdn.common.ApiReceipt;
import com.netflix.discovery.DiscoveryManager;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname ContentEurekaController
 * @Description TODO
 * @Date 2020/12/24 14:32
 * @Created by Luj
 */

@RestController
@RequestMapping("/content/eureka")
public class ContentEurekaController {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ContentEurekaController.class);


    @RequestMapping(value = "/offline", method = RequestMethod.GET)
    public ApiReceipt offLine(){
        DiscoveryManager.getInstance().shutdownComponent();
        return ApiReceipt.ok();
    }

}
