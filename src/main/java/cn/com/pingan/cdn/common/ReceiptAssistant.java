/**   
 * @Project: anubis-content
 * @File: ReceiptAssistant.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午2:21:27 
 */
package cn.com.pingan.cdn.common;

/** 
 * @ClassName: ReceiptAssistant 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 下午2:21:27 
 *  
 */
public enum ReceiptAssistant {
    SUCCESS{

        public ApiReceipt build() {
            return new ApiReceipt(ReceiptEnum.SUCCESS.code,null,ReceiptEnum.SUCCESS.message,null);
        }

        public ApiReceipt build(String message, Object data) {
            return new ApiReceipt(ReceiptEnum.SUCCESS.code,null,message,data);
        }

        public ApiReceipt build(Object data) {
            return new ApiReceipt(ReceiptEnum.SUCCESS.code,null,ReceiptEnum.SUCCESS.message,data);
        }
        
    },
    FAILURE{

        public ApiReceipt build() {
            return new ApiReceipt(ReceiptEnum.FAILURE.code,null,ReceiptEnum.FAILURE.message,null);
        }

        public ApiReceipt build(String message, Object data) {
            return new ApiReceipt(ReceiptEnum.FAILURE.code,null,message,data);
        }

        public ApiReceipt build(Object data) {
            return new ApiReceipt(ReceiptEnum.FAILURE.code,null,ReceiptEnum.FAILURE.message,data);
        }
        
    };
    
    public abstract ApiReceipt build();
    public abstract ApiReceipt build(Object data);
    public abstract ApiReceipt build(String message,Object data);
    
    
    public static enum ReceiptEnum{
        SUCCESS(0,"success"),
        FAILURE(1,"failure");
        
        private int code;
        private String message;
        
        ReceiptEnum(int code,String message){
            this.code = code;
            this.message = message;
        }
        
        public int getCode() {
            return code;
        }
        public void setCode(int code) {
            this.code = code;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        
    }
}
