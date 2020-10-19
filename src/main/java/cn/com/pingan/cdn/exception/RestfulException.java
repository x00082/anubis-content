/**   
 * @Project: anubis-content
 * @File: RestfulException.java 
 * @Package cn.com.pingan.cdn.exception 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月15日 下午2:19:27 
 */
package cn.com.pingan.cdn.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** 
 * @ClassName: RestfulException 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月15日 下午2:19:27 
 *  
 */
@Setter
@Getter
@Builder
public class RestfulException extends RuntimeException {
    static final long serialVersionUID = 1L;

    public static final Integer SuccessCode = 0;
    public static final String SuccessMessage = "success";

    private int code;
    private String message;

    public RestfulException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
