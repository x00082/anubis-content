/**   
 * @Project: anubis-content
 * @File: GateWayHeaderDTO.java 
 * @Package cn.com.pingan.cdn.gateWay 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 上午10:49:21 
 */
package cn.com.pingan.cdn.gateWay;

import lombok.Data;

/** 
 * @ClassName: GateWayHeaderDTO 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 上午10:49:21 
 *  
 */
@Data
public class GateWayHeaderDTO {
    private String uid;
    private String isAdmin;
    private String channel;
    private String username;
    private String subAccount;
    private String spcode;
    private String token;
}
