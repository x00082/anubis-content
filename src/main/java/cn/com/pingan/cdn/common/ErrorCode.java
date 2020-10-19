/**   
 * @Project: anubis-content
 * @File: ErrorCode.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午2:24:44 
 */
package cn.com.pingan.cdn.common;

/** 
 * @ClassName: ErrorCode 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 下午2:24:44 
 *  
 */
public enum ErrorCode {

    PARAMILLEGAL("0x0000", "请求参数异常"),

    DMORIGINJSON("0x001001", "源站参数错误"),
    DMWORKFLOW("0x001002", "请求workflow错误"),
    DMNAME("0x001003", "域名不能为空"),
    DMTYPE("0x001004", "域名类型不正确");

    private String code;
    private String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static String parse(String code) {
        for (ErrorCode ec : ErrorCode.values())
            if (ec.getCode().equals(code)) return ec.getDescription();
        return "";
    }
}
