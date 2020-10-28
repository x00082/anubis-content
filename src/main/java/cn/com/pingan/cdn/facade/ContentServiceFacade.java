/**   
 * @Project: anubis-content
 * @File: ContentServiceFacade.java 
 * @Package cn.com.pingan.cdn.facade 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午4:47:16 
 */
package cn.com.pingan.cdn.facade;

import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.common.ContentException;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
import cn.com.pingan.cdn.validator.content.FreshCommand;

import java.util.List;

/** 
 * @ClassName: ContentServiceFacade 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 下午4:47:16 
 *  
 */
public interface ContentServiceFacade {
    public ApiReceipt refreshDir(GateWayHeaderDTO dto, FreshCommand command) throws ContentException;

    public ApiReceipt refreshUrl(GateWayHeaderDTO dto, FreshCommand command)throws ContentException;

    public ApiReceipt prefetch(GateWayHeaderDTO dto, FreshCommand command)throws ContentException;

    //public QueryHisDTO queryHis(GateWayHeaderDTO dto, QueryHisCommand command);


    ApiReceipt openApiRefreshDir(GateWayHeaderDTO dto, List<String> data) throws ContentException;


    ApiReceipt openApiRefreshUrl(GateWayHeaderDTO dto, List<String> data) throws ContentException;

    ApiReceipt openApiPreload(GateWayHeaderDTO dto, List<String> data) throws ContentException;
    
    ApiReceipt test() throws ContentException;
}
