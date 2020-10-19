/**   
 * @Project: anubis-content
 * @File: ContentVendorDTO.java 
 * @Package cn.com.pingan.cdn.request.content 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月14日 下午5:00:36 
 */
package cn.com.pingan.cdn.request.content;

import java.util.List;

import cn.com.pingan.cdn.request.content.ContentVendorDTO.UrlVendor;
import lombok.Data;

/** 
 * @ClassName: ContentVendorDTO 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月14日 下午5:00:36 
 *  
 */
@Data
public class ContentVendorDTO {
    private String taskId;
    
    private String type;
    
    private List<UrlVendor> urlVendors;
    
    @Data
    public static class UrlVendor {
        private List<String> urls;
        private List<String> vendors;
    }
}
