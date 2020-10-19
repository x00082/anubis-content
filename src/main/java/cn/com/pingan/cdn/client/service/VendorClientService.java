/**   
 * @Project: anubis-content
 * @File: VendorClientService.java 
 * @Package cn.com.pingan.cdn.client.service 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月10日 下午3:47:07 
 */
package cn.com.pingan.cdn.client.service;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;


/** 
 * @ClassName: VendorClientService 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月10日 下午3:47:07 
 *  
 */
public interface VendorClientService {
    
    public JSONObject refreshUrl(JSONObject refreshPreloadData) throws Exception;

    public JSONObject refreshDir(JSONObject refreshPreloadData) throws Exception;

    public JSONObject preloadUrl(JSONObject refreshPreloadData) throws Exception;
    
    public JSONObject taskStatusQuery(JSONObject request) throws Exception;

    public JSONObject queryRefreshAndPreloadTaskStatus(JSONObject request) throws Exception;
    
    
    public boolean tryRefreshUrlAcquire();
    
    public boolean tryRefreshDirAcquire();
    
    public boolean tryPreloadUrlAcquire();
    
    public void run();
}
