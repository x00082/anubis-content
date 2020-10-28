/**   
 * @Project: anubis-content
 * @File: HisStatus.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午10:16:23 
 */
package cn.com.pingan.cdn.common;

/** 
 * @ClassName: HisStatus 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午10:16:23 
 *  
 */
public enum HisStatus {
    /**
     * 成功
     */
    SUCCESS,
    /**
     * 失败
     */
    FAIL,
    /**
     * 操作中
     */
    WAIT;
    
    public static HisStatus of(String value) {
        for(HisStatus ut : HisStatus.values()) {
            if(ut.name().equals(value)) return ut;
        }
        return HisStatus.SUCCESS;
    }
}
