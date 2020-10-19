/**   
 * @Project: anubis-content
 * @File: ContentServiceFacadeImpl.java 
 * @Package cn.com.pingan.cdn.facade.internal 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午4:53:01 
 */
package cn.com.pingan.cdn.facade.internal;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.common.ContentException;
import cn.com.pingan.cdn.facade.ContentServiceFacade;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
import cn.com.pingan.cdn.service.ContentService;
import cn.com.pingan.cdn.common.RefreshType;
import cn.com.pingan.cdn.validator.content.FreshCommand;
import lombok.extern.slf4j.Slf4j;

/** 
 * @ClassName: ContentServiceFacadeImpl 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 下午4:53:01 
 *  
 */
@Service
@Slf4j
public class ContentServiceFacadeImpl implements ContentServiceFacade {

    @Autowired
    private ContentService contentService;
    
    /**
     * @Method: refreshDir
     * @Description: TODO()
     * @param dto
     * @param command
     * @return
     * @throws ContentException 
     */
    @Override
    public ApiReceipt refreshDir(GateWayHeaderDTO dto, FreshCommand command) throws ContentException {
        // TODO 自动生成的方法存根
        
        return contentService.saveContent(dto, command.getData(), RefreshType.dir);
    }

    /**
     * @Method: refreshUrl
     * @Description: TODO()
     * @param dto
     * @param command
     * @return
     * @throws ContentException 
     */
    @Override
    public ApiReceipt refreshUrl(GateWayHeaderDTO dto, FreshCommand command) throws ContentException {
        // TODO 自动生成的方法存根
        
        
        return contentService.saveContent(dto, command.getData(), RefreshType.url);
    }

    /**
     * @Method: prefetch
     * @Description: TODO()
     * @param dto
     * @param command
     * @return
     * @throws ContentException 
     */
    @Override
    public ApiReceipt prefetch(GateWayHeaderDTO dto, FreshCommand command) throws ContentException {
        // TODO 自动生成的方法存根
        
        
        return contentService.saveContent(dto, command.getData(), RefreshType.preheat);
    }

    /**
     * @Method: openApiRefreshDir
     * @Description: TODO()
     * @param dto
     * @param data
     * @return
     * @throws ContentException 
     */
    @Override
    public ApiReceipt openApiRefreshDir(GateWayHeaderDTO dto, List<String> data) throws ContentException {
        // TODO 自动生成的方法存根
        return null;
    }

    /**
     * @Method: openApiRefreshUrl
     * @Description: TODO()
     * @param dto
     * @param data
     * @return
     * @throws ContentException 
     */
    @Override
    public ApiReceipt openApiRefreshUrl(GateWayHeaderDTO dto, List<String> data) throws ContentException {
        // TODO 自动生成的方法存根
        return null;
    }

    /**
     * @Method: openApiPreload
     * @Description: TODO()
     * @param dto
     * @param data
     * @return
     * @throws ContentException 
     */
    @Override
    public ApiReceipt openApiPreload(GateWayHeaderDTO dto, List<String> data) throws ContentException {
        // TODO 自动生成的方法存根
        return null;
    }

    
    @Override
    public ApiReceipt test() throws ContentException {
        contentService.test();
        return null;
    }
}
