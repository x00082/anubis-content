/**   
 * @Project: anubis-content
 * @File: DomainException.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午2:52:47 
 */
package cn.com.pingan.cdn.exception;

/**
 * @ClassName: DomainException 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 下午2:52:47 
 *  
 */
public class DomainException extends BaseException {
    private static final long serialVersionUID = 1L;

    public DomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DomainException(String code, String arg0) {
        super(code, arg0);
    }

    public DomainException(String code) {
        super(code);
    }
}
