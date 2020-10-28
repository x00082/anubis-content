/**   
 * @Project: anubis-content
 * @File: HistType.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午9:40:18 
 */
package cn.com.pingan.cdn.common;

/** 
 * @ClassName: HistType 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午9:40:18 
 *  
 */
public enum RefreshType {
    /**
     * url刷新
     */
    url("url"),
    /**
     * 目录刷新
     */
    dir("dir"),
    /**
     * 预热
     */
    preheat("preheat");
    
    private String code;

    RefreshType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    
    public static RefreshType of(String value) {
        for(RefreshType ut : RefreshType.values()) {
            if(ut.name().equals(value)) return ut;
        }
        return RefreshType.url;
    }
}
