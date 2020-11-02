package cn.com.pingan.cdn.exception;

/**
 * @Classname SslCertException
 * @Description TODO
 * @Date 2020/11/1 19:28
 * @Created by Luj
 */
public class SslCertException extends BaseException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param errorCode
     */
    public SslCertException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SslCertException(String code, String arg0) {
        super(code, arg0);
    }

    public SslCertException(String code) {
        super(code);
    }
}
