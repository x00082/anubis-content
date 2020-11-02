/**   
 * @Project: anubis-content
 * @File: ContentException.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午2:48:37 
 */
package cn.com.pingan.cdn.exception;

/**
 * @ClassName: ContentException 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 下午2:48:37 
 *  
 */
public class ContentException extends BaseException {
    private static final long serialVersionUID = 1L;

    public ContentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ContentException(String code, String arg0) {
        super(code, arg0);
    }

    public ContentException(String code) {
        super(code);
    }
}
