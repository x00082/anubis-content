package cn.com.pingan.cdn.client;

import cn.com.pingan.cdn.common.AnubisNotifyExceptionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Classname AnubisNotifyService
 * @Description TODO
 * @Date 2020/10/21 18:57
 * @Created by Luj
 */
@FeignClient(name = "anubis-message")
public interface AnubisNotifyService {
    @Async
    @RequestMapping(value = "/message/email/notify/api/exception",method = RequestMethod.POST)
    void emailNotifyApiException(@RequestBody AnubisNotifyExceptionRequest request) ;
}
