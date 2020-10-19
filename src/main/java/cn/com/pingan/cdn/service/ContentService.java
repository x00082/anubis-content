/**   
 * @Project: anubis-content
 * @File: ContentService.java 
 * @Package cn.com.pingan.cdn.service 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午9:34:20 
 */
package cn.com.pingan.cdn.service;

import java.io.IOException;
import java.util.List;

import cn.com.pingan.cdn.common.ContentException;
import cn.com.pingan.cdn.common.RefreshType;
import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;;

/** 
 * @ClassName: ContentService 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午9:34:20 
 *  
 */
public interface ContentService {
    public ApiReceipt saveContent(GateWayHeaderDTO dto, List<String> data, RefreshType type) throws ContentException;
    public ApiReceipt saveContentM(GateWayHeaderDTO dto, List<String> data, RefreshType type) throws ContentException;

    //ApiReceipt setUserContentNumber(ContentLimitDTO command);

    ApiReceipt getUserContentNumber(String spCode) throws IOException;
    
    public ApiReceipt test() throws ContentException;
}
