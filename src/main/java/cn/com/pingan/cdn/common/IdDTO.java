/**   
 * @Project: anubis-content
 * @File: IdDTO.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 下午2:13:44 
 */
package cn.com.pingan.cdn.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 
 * @ClassName: IdDTO 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 下午2:13:44 
 *  
 */
@Data
@AllArgsConstructor
public class IdDTO {
    private String id;
    private Boolean detail;
}
