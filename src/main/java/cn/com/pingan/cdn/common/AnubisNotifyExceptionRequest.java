package cn.com.pingan.cdn.common;

import lombok.Data;

/**
 * @Classname AnubisNotifyExceptionRequest
 * @Description TODO
 * @Date 2020/10/21 18:58
 * @Created by Luj
 */
@Data
public class AnubisNotifyExceptionRequest {
    private String serviceName;

    private String vendor;

    private String method;

    private String domainName;

    private String requestId;

    private String message;

    private Exception exception;

    public AnubisNotifyExceptionRequest() {
    }

    public AnubisNotifyExceptionRequest(String serviceName, String vendor, String method, String domainName, String requestId, String message, Exception exception) {
        this.serviceName = serviceName;
        this.vendor = vendor;
        this.method = method;
        this.domainName = domainName;
        this.requestId = requestId;
        this.message = message;
        this.exception = exception;
    }


    public AnubisNotifyExceptionRequest(String serviceName, String method, String domainName, String requestId, String message) {
        this.serviceName = serviceName;
        this.method = method;
        this.domainName = domainName;
        this.requestId = requestId;
        this.message = message;
    }
}
