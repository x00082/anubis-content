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
import cn.com.pingan.cdn.common.ContentLimitDTO;
import cn.com.pingan.cdn.exception.ContentException;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
import cn.com.pingan.cdn.request.QueryHisDTO;
import cn.com.pingan.cdn.request.VendorInfoDTO;
import cn.com.pingan.cdn.request.openapi.ContentDefaultNumDTO;
import cn.com.pingan.cdn.validator.content.FreshCommand;
import cn.com.pingan.cdn.validator.content.QueryHisCommand;

import java.io.IOException;
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


    public ApiReceipt queryDetails(String requestId)throws ContentException;

    public ApiReceipt redo(String id, boolean flag)throws ContentException;

    public ApiReceipt batchRedo(List<String> requestIds, boolean flag)throws ContentException;

    public QueryHisDTO queryHis(GateWayHeaderDTO dto, QueryHisCommand command);


    ApiReceipt openApiRefreshDir(GateWayHeaderDTO dto, List<String> data) throws ContentException;


    ApiReceipt openApiRefreshUrl(GateWayHeaderDTO dto, List<String> data) throws ContentException;

    ApiReceipt openApiPreload(GateWayHeaderDTO dto, List<String> data) throws ContentException;

    ApiReceipt setUserContentNumber(ContentLimitDTO command);

    ApiReceipt getUserContentNumber(String spCode) throws IOException;

    public ApiReceipt setUserLimitNumber(ContentDefaultNumDTO command);

    public ApiReceipt getUserLimitNumber(String spCode) throws IOException;

    public ApiReceipt addVendorInfo(VendorInfoDTO command);

    public ApiReceipt setVendorInfo(VendorInfoDTO command);

    public ApiReceipt getVendorInfo(String vendor);

    public ApiReceipt setVendorStatus(VendorInfoDTO command);

    ApiReceipt exportAndImport(GateWayHeaderDTO dto, QueryHisCommand command);

    
    ApiReceipt test(GateWayHeaderDTO dto, FreshCommand command) throws ContentException;
}
