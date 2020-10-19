/**   
 * @Project: anubis-content
 * @File: HistoryMessage.java 
 * @Package cn.com.pingan.cdn.rabbitmq.message 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 上午10:44:01 
 */
package cn.com.pingan.cdn.rabbitmq.message;

import java.util.List;

import cn.com.pingan.cdn.common.RefreshType;
import lombok.Data;

/** 
 * @ClassName: HistoryMessage 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 上午10:44:01 
 *  
 */
@Data
public class HistoryMessage {
    
    private String taskId;
    
    private RefreshType type;
    
    private List<String> urls;
}
