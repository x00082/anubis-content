/**   
 * @Project: anubis-content
 * @File: ContentLimitJsonConfig.java 
 * @Package cn.com.pingan.cdn.config 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 上午9:58:29 
 */
package cn.com.pingan.cdn.config;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

/** 
 * @ClassName: ContentLimitJsonConfig 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 上午9:58:29 
 *  
 */
@Slf4j
@Component
public class ContentLimitJsonConfig {
    @Value("classpath:contentLimit.json")
    private Resource resource;

    @PostConstruct
    public JSONObject getDefaultContentLimit(){
        JSONObject jo = null;
        try {
            String json = (String) IOUtils.toString(resource.getInputStream(), "UTF-8");
            jo  = JSONObject.parseObject(json);
        }catch (Exception e) {
          return null;
        }

        return jo;

    }
}
