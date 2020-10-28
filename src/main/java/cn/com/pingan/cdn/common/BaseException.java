/**   
 * @Project: anubis-content
 * @File: BaseException.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午2:47:27 
 */
package cn.com.pingan.cdn.common;

import lombok.Getter;

/** 
 * @ClassName: BaseException 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 下午2:47:27 
 *  
 */
@Getter
public class BaseException extends Exception {
    private static final long serialVersionUID = 1L;
    private String code;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.code = errorCode.getCode();
    }

    public BaseException(String code) {
        super(ErrorCode.parse(code));
        this.code = code;
    }
    
    public BaseException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public BaseException(String code,String arg0) {
        super(arg0);
        this.code = code;
    }
}
