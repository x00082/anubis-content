/**   
 * @Project: anubis-content
 * @File: ApiResponse.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 下午2:00:49 
 */
package cn.com.pingan.cdn.common;

/** 
 * @ClassName: ApiResponse 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 下午2:00:49 
 *  
 */
public class ApiResponse {
    private int code;
    private String message;
    private Object data;

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isSuccessful() {
        return this.code == 0;
    }

    public void reportApiError() {
        throw new RuntimeException(this.message);
    }
}
