package cn.com.pingan.cdn.service;

import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.request.VendorInfoDTO;

/**
 * @Classname ConfigService
 * @Description TODO
 * @Date 2020/10/27 16:59
 * @Created by Luj
 */
public interface ConfigService {

    public ApiReceipt addVendorInfo(VendorInfoDTO infoDTO) ;

    public ApiReceipt modifyVendorInfo(VendorInfoDTO infoDTO);

    public ApiReceipt delVendorInfo(String vendor);

    public ApiReceipt queryVendorInfo(String vendor);

    public ApiReceipt setVendorStatus(VendorInfoDTO infoDTO);
}
