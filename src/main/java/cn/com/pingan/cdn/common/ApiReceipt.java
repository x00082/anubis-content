/**   
 * @Project: anubis-content
 * @File: ApiReceipt.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午1:51:50 
 */
package cn.com.pingan.cdn.common;

import brave.Tracer;
import cn.com.pingan.cdn.config.ApplicationContextProvider;
import cn.com.pingan.cdn.common.ReceiptAssistant.ReceiptEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** 
 * @ClassName: ApiReceipt 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 下午1:51:50 
 *  
 */
@Getter @Setter
@AllArgsConstructor
public class ApiReceipt {
    
    private int code;
    private String msgCode;
    private String message;
    private Object data;
    private String requestId;
    
    public ApiReceipt(int code, String msgCode, String message, Object data) {
        this();
        this.code = code;
        this.msgCode = msgCode;
        this.message = message;
        this.data = data;
    }
 
    public ApiReceipt() {
        Tracer tracer = ApplicationContextProvider.l(Tracer.class);
        this.requestId = Long.toHexString(tracer.currentSpan().context().traceId());
    }
    
    public static ApiReceipt build(ReceiptAssistant assistant) {
        return assistant.build();
    }
    
    public ApiReceipt swap() {
        this.code = this.code == ReceiptEnum.SUCCESS.getCode() 
                ? ReceiptEnum.FAILURE.getCode() : ReceiptEnum.SUCCESS.getCode();
        return this;
    }
    
    public ApiReceipt of(int code) {
        this.code = code;
        return this;
    }
    
    public ApiReceipt with(String msgCode,String message) {
        this.msgCode = msgCode;
        this.message = message;
        return this;
    }
    
    public ApiReceipt data(Object data) {
        this.data = data;
        return this;
    }
    
    public static ApiReceipt ok() {
        return ApiReceipt.build(ReceiptAssistant.SUCCESS);
    }
    
    public static ApiReceipt ok(Object data) {
        return ApiReceipt.build(ReceiptAssistant.SUCCESS).data(data);
    }
    
    public static ApiReceipt error() {
        return ApiReceipt.build(ReceiptAssistant.FAILURE);
    }
    
    public static ApiReceipt error(ErrorCode errorCode) {
        return ApiReceipt.error(errorCode.getCode(),errorCode.getDescription());
    }
    
    public static ApiReceipt error(String msgCode,String message) {
        return ApiReceipt.build(ReceiptAssistant.FAILURE).with(msgCode,message);
    }
}
