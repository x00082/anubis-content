/**   
 * @Project: anubis-content
 * @File: ContentService.java 
 * @Package cn.com.pingan.cdn.service 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月9日 上午9:34:20 
 */
package cn.com.pingan.cdn.service;

import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.common.ContentLimitDTO;
import cn.com.pingan.cdn.common.RefreshType;
import cn.com.pingan.cdn.exception.ContentException;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
import cn.com.pingan.cdn.model.mysql.ContentHistory;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;
import cn.com.pingan.cdn.request.openapi.ContentDefaultNumDTO;

import java.io.IOException;
import java.util.List;

;

/** 
 * @ClassName: ContentService 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月9日 上午9:34:20 
 *  
 */
public interface ContentService {
    public ApiReceipt saveContent(GateWayHeaderDTO dto, List<String> data, RefreshType type) throws ContentException;

    public ApiReceipt redoContentTask(String requestId, boolean flag) throws ContentException;

    public void saveContentItem(TaskMsg taskMsg) throws ContentException;


    ApiReceipt setUserContentNumber(ContentLimitDTO command);

    ApiReceipt getUserContentNumber(String spCode) throws IOException;

    public ContentHistory findHisttoryByRequestId(String id)throws ContentException;
    
    public ApiReceipt test() throws ContentException;

    ApiReceipt setUserDefaultContentNumber(ContentDefaultNumDTO command);

    ApiReceipt getUserDefaultContentNumber(String spCode) throws IOException;
}
