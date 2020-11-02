/**   
 * @Project: anubis-content
 * @File: LineService.java 
 * @Package cn.com.pingan.cdn.service 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 上午10:58:05 
 */
package cn.com.pingan.cdn.service;

import java.util.List;

import cn.com.pingan.cdn.exception.DomainException;
import cn.com.pingan.cdn.common.LineResponse;

/** 
 * @ClassName: LineService 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 上午10:58:05 
 *  
 */
public interface LineService {
    /**
     * 批量获取线路信息
     *
     * @param lineIds
     * @param detail
     * @return
     * @throws DomainException
     */
    public List<LineResponse.LineDetail> getLineByIds(List<String> lineIds, boolean detail) throws DomainException;
}
