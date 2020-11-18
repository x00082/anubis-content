package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.common.VendorStatusEnum;
import cn.com.pingan.cdn.exception.ErrorCode;
import cn.com.pingan.cdn.model.mysql.VendorInfo;
import cn.com.pingan.cdn.rabbitmq.config.RabbitListenerConfig;
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

    @Autowired
    RabbitListenerConfig rabbitListenerConfig;

    @Override
    public ApiReceipt addVendorInfo(VendorInfoDTO infoDTO) {

        VendorInfo info = vendorInfoRepository.findByVendor(infoDTO.getVendor());
        if(info == null){
            info = new VendorInfo();
            info.setVendorInfo(infoDTO);
            vendorInfoRepository.save(info);
        }else{
            log.info("{}厂商信息已存在", infoDTO.getVendor());

            return ApiReceipt.error(ErrorCode.DUPLICATE);
        }
        return ApiReceipt.ok();
    }

    @Override
    public ApiReceipt modifyVendorInfo(VendorInfoDTO infoDTO) {

        VendorInfo info = vendorInfoRepository.findByVendor(infoDTO.getVendor());
        if(info == null){
            log.info("{}厂商信息不存在", infoDTO.getVendor());

            return ApiReceipt.error("0x004008", "厂商信息不存在");
        }else{
            info.setVendorInfo(infoDTO);
            vendorInfoRepository.save(info);
        }

        if (VendorStatusEnum.down.equals(infoDTO.getStatus())) {
            rabbitListenerConfig.stop("content_" + infoDTO.getVendor());
            rabbitListenerConfig.stop("content_" + infoDTO.getVendor() + "_url");
            rabbitListenerConfig.stop("content_" + infoDTO.getVendor() + "_dir");
            rabbitListenerConfig.stop("content_" + infoDTO.getVendor() + "_preheat");
            rabbitListenerConfig.stop("content_" + infoDTO.getVendor() + "_robin");

        }else if(VendorStatusEnum.up.equals(infoDTO.getStatus())){
            rabbitListenerConfig.start("content_" + infoDTO.getVendor());
            rabbitListenerConfig.start("content_" + infoDTO.getVendor() + "_url");
            rabbitListenerConfig.start("content_" + infoDTO.getVendor() + "_dir");
            rabbitListenerConfig.start("content_" + infoDTO.getVendor() + "_preheat");
            rabbitListenerConfig.start("content_" + infoDTO.getVendor() + "_robin");
        }

        return ApiReceipt.ok();
    }

    @Override
    public ApiReceipt delVendorInfo(String vendor) {
        VendorInfo info = vendorInfoRepository.findByVendor(vendor);
        if(info == null){
            log.info("{}厂商信息不存在", vendor);
        }else{
            vendorInfoRepository.deleteById(info.getId());
        }
        return ApiReceipt.ok();
    }

    @Override
    public ApiReceipt queryVendorInfo(String vendor){
        VendorInfo info = vendorInfoRepository.findByVendor(vendor);
        if(info == null){
            log.info("{}厂商信息不存在", vendor);
            return ApiReceipt.error("0x004008", "厂商信息不存在");
        }
        return ApiReceipt.ok().data(info.getVendorInfo());
    }

    @Override
    public ApiReceipt setVendorStatus(VendorInfoDTO infoDTO){
        VendorInfo info = vendorInfoRepository.findByVendor(infoDTO.getVendor());
        if(info == null){
            log.info("{}厂商信息不存在", infoDTO.getVendor());
            return ApiReceipt.error("0x004008", "厂商信息不存在");
        }

        info.setStatus(infoDTO.getStatus());
        vendorInfoRepository.save(info);

        if (VendorStatusEnum.down.equals(infoDTO.getStatus())) {
            rabbitListenerConfig.stop("content_" + infoDTO.getVendor());
            rabbitListenerConfig.stop("content_" + infoDTO.getVendor() + "_url");
            rabbitListenerConfig.stop("content_" + infoDTO.getVendor() + "_dir");
            rabbitListenerConfig.stop("content_" + infoDTO.getVendor() + "_preheat");
            rabbitListenerConfig.stop("content_" + infoDTO.getVendor() + "_robin");

        }else if(VendorStatusEnum.up.equals(infoDTO.getStatus())){
            rabbitListenerConfig.start("content_" + infoDTO.getVendor());
            rabbitListenerConfig.start("content_" + infoDTO.getVendor() + "_url");
            rabbitListenerConfig.start("content_" + infoDTO.getVendor() + "_dir");
            rabbitListenerConfig.start("content_" + infoDTO.getVendor() + "_preheat");
            rabbitListenerConfig.start("content_" + infoDTO.getVendor() + "_robin");
        }
        return ApiReceipt.ok();
    }
}
