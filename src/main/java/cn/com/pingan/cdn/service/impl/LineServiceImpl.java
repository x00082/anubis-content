/**   
 * @Project: anubis-content
 * @File: LineServiceImpl.java 
 * @Package cn.com.pingan.cdn.service.impl 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 下午2:05:16 
 */
package cn.com.pingan.cdn.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.pingan.cdn.exception.DomainException;
import cn.com.pingan.cdn.common.IdDTO;
import cn.com.pingan.cdn.common.LineResponse;
import cn.com.pingan.cdn.common.LineResponse.LineDetail;
import cn.com.pingan.cdn.service.LineService;
import cn.com.pingan.cdn.client.LineClientService;
import lombok.extern.slf4j.Slf4j;

/** 
 * @ClassName: LineServiceImpl 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 下午2:05:16 
 *  
 */
@Service
@Slf4j
public class LineServiceImpl implements LineService {

    @Autowired
    private LineClientService lineClientService;
    
    /**
     * @Method: getLineByIds
     * @Description: TODO()
     * @param lineIds
     * @param detail
     * @return
     * @throws DomainException 
     * @see cn.com.pingan.cdn.service.LineService#getLineByIds(java.util.List, boolean) 
     */
    @Override
    public List<LineDetail> getLineByIds(List<String> lineIds, boolean detail) throws DomainException {
        try {
            LineResponse apiResponse = this.lineClientService.getLineByIds(new IdDTO(String.join(",", lineIds), detail), "true");
            if (!apiResponse.isSuccessful()) {
                throw new DomainException("0x0002", apiResponse.getMessage());
            }
            return apiResponse.getData();
        } catch (Exception e) {
            log.info("getLineByIds error:{}", e.getMessage());
            e.printStackTrace();
            throw new DomainException("0x0002", e.getMessage());
        }
    }

}
