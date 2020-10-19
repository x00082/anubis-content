/**   
 * @Project: anubis-content
 * @File: Constants.java 
 * @Package cn.com.pingan.cdn.rabbitmq.constants 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 上午10:34:14 
 */
package cn.com.pingan.cdn.rabbitmq.constants;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 
 * @ClassName: Constants 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 上午10:34:14 
 *  
 */
@Data
@NoArgsConstructor
public class Constants {
    
    //
    public static final String CONTENT_MESSAGE_EXCHANGE="anubisContentExchange";
    public static final String CONTENT_MESSAGE_QUEUE_NAE="anubisContentQueue";    
    public static final String CONTENT_MESSAGE_ROUTINE_KEY="anubisContentRoutineKey";
    
    
}
