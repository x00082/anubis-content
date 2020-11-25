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
import cn.com.pingan.cdn.rabbitmq.message.FanoutMsg;
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

    public ApiReceipt batchRedoContentTask(List<String> requestIds,  boolean flag) throws ContentException;

    public ApiReceipt getContentTaskDetails(String requestId) throws ContentException;

    public void saveVendorTask(TaskMsg taskMsg) throws ContentException;

    public int fflushDomainVendor(FanoutMsg taskMsg);

    //public void saveContentVendor(TaskMsg taskMsg) throws ContentException;

    //public void contentItemRobin(TaskMsg taskMsg) throws ContentException;

    public void contentHistoryRobin(TaskMsg taskMsg) throws ContentException;

    public void clearErrorTask(TaskMsg taskMsg) throws ContentException;


    public ApiReceipt setUserContentNumber(ContentLimitDTO command);

    public ApiReceipt getUserContentNumber(String spCode) throws IOException;

    public ContentHistory findHisttoryByRequestId(String id)throws ContentException;
    
    public ApiReceipt test() throws ContentException;

    public ApiReceipt setUserDefaultContentNumber(ContentDefaultNumDTO command);

    public ApiReceipt getUserDefaultContentNumber(String spCode) throws IOException;
}
