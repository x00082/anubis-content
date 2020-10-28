package cn.com.pingan.cdn.client;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Classname AliyunClientService
 * @Description TODO
 * @Date 2020/10/21 18:51
 * @Created by Luj
 */
@FeignClient(name = "anubis-adapter-venus")
public interface VenusClientService extends VendorClientService {
}
