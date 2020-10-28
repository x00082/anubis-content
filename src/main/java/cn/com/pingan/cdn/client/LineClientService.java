/**   
 * @Project: anubis-content
 * @File: LineClient.java 
 * @Package cn.com.pingan.cdn.service.internal 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 下午2:09:39 
 */
package cn.com.pingan.cdn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import cn.com.pingan.cdn.common.IdDTO;
import cn.com.pingan.cdn.common.LineResponse;



/** 
 * @ClassName: LineClientService
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 下午2:09:39 
 *  
 */
@FeignClient("ANUBIS-LINE")
public interface LineClientService {
    
    @PostMapping(value = "/queryids")
    public LineResponse getLineByIds(@RequestBody IdDTO idDTO, @RequestHeader(name = "isAdmin") String isAdmin);
    
}
