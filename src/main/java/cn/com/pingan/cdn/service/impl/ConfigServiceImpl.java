package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.common.ErrorCode;
import cn.com.pingan.cdn.model.mysql.VendorInfo;
import cn.com.pingan.cdn.repository.mysql.VendorInfoRepository;
import cn.com.pingan.cdn.request.VendorInfoDTO;
import cn.com.pingan.cdn.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Classname ConfigServiceImpl
 * @Description TODO
 * @Date 2020/10/27 17:14
 * @Created by Luj
 */
@Service
@Slf4j
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    VendorInfoRepository vendorInfoRepository;

    @Override
    public ApiReceipt addVendorInfo(VendorInfoDTO infoDTO) throws Exception {

        VendorInfo info = vendorInfoRepository.findByVendor(infoDTO.getVendor());
        if(info == null){
            info = new VendorInfo();
            info.setVendorInfo(infoDTO);
            vendorInfoRepository.saveAndFlush(info);
        }else{
            log.info("{}厂商信息已存在", infoDTO.getVendor());

            return ApiReceipt.error(ErrorCode.DUPLICATE);
        }
        return ApiReceipt.ok();
    }

    @Override
    public ApiReceipt modifyVendorInfo(VendorInfoDTO infoDTO) throws Exception {

        VendorInfo info = vendorInfoRepository.findByVendor(infoDTO.getVendor());
        if(info == null){
            log.info("{}厂商信息不存在", infoDTO.getVendor());

            return ApiReceipt.error(ErrorCode.NULLDATA);
        }else{
            info.setVendorInfo(infoDTO);
            vendorInfoRepository.saveAndFlush(info);
        }

        return ApiReceipt.ok();
    }

    @Override
    public ApiReceipt delVendorInfo(String vendor) throws Exception {
        VendorInfo info = vendorInfoRepository.findByVendor(vendor);
        if(info == null){
            log.info("{}厂商信息不存在", vendor);
        }else{
            vendorInfoRepository.deleteById(info.getId());
        }
        return ApiReceipt.ok();
    }

    @Override
    public ApiReceipt queryVendorInfo(String vendor) throws Exception{
        VendorInfo info = vendorInfoRepository.findByVendor(vendor);
        if(info == null){
            log.info("{}厂商信息不存在", vendor);
            return ApiReceipt.error(ErrorCode.NULLDATA);
        }
        return ApiReceipt.ok().data(info.getVendorInfo());
    }

    @Override
    public Boolean setVendorStatus(VendorInfoDTO infoDTO) throws Exception{
        VendorInfo info = vendorInfoRepository.findByVendor(infoDTO.getVendor());
        if(info == null){
            log.info("{}厂商信息不存在", infoDTO.getVendor());
            return false;
        }

        info.setStatus(infoDTO.getStatus());
        vendorInfoRepository.saveAndFlush(info);
        return true;
    }
}
