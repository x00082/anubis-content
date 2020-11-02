/**   
 * @Project: anubis-content
 * @File: ErrEnum.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月15日 下午2:22:19 
 */
package cn.com.pingan.cdn.exception;

import lombok.Getter;

/** 
 * @ClassName: ErrEnum 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月15日 下午2:22:19 
 *  
 */
@Getter
public enum ErrEnum {
    ErrInternal("internal err"),
    ErrDBInternal("db internal err"),
    ErrMQPushMsg("mq push msg failed"),
    ErrMQAckMsg("mq ack msg failed"),
    ErrMQRejectMsg("mq reject failed"),
    ErrRequestDNSFail("request dns fail"),
    ErrRequestLineFail("request line fail"),
    ;

    private int code;
    private String errMsg;

    ErrEnum(String errMsg) {
        this.errMsg = errMsg;
    }

    public int getCode() {
        return this.ordinal() + 500000;
    }
}
