/**   
 * @Project: anubis-content
 * @File: VendorClientService.java 
 * @Package cn.com.pingan.cdn.client.service 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月10日 下午3:47:07 
 */
package cn.com.pingan.cdn.client;

import cn.com.pingan.cdn.common.RefreshPreloadData;
import cn.com.pingan.cdn.common.RefreshPreloadTaskStatusDTO;
import cn.com.pingan.cdn.exception.AnubisContentException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/** 
 * @ClassName: VendorClientService 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月10日 下午3:47:07 
 *  
 */
public interface VendorClientService {

    @RequestMapping(value = "/adapter/refresh/url", method = RequestMethod.POST)
    public JSONObject refreshUrl(@RequestBody RefreshPreloadData refreshPreloadData) throws AnubisContentException;

    @RequestMapping(value = "/adapter/refresh/dir", method = RequestMethod.POST)
    public JSONObject refreshDir(@RequestBody RefreshPreloadData refreshPreloadData) throws AnubisContentException;

    @RequestMapping(value = "/adapter/preload/url", method = RequestMethod.POST)
    public JSONObject preloadUrl(@RequestBody RefreshPreloadData refreshPreloadData) throws AnubisContentException;

    @RequestMapping(value = "/adapter/refresh/and/preload/status", method = RequestMethod.POST)
    public JSONObject queryRefreshPreloadTask(@RequestBody RefreshPreloadTaskStatusDTO request) throws AnubisContentException;
}
