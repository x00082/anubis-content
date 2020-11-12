package cn.com.pingan.cdn.controller;


import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.common.ContentLimitDTO;
import cn.com.pingan.cdn.exception.ContentException;
import cn.com.pingan.cdn.exception.DomainException;
import cn.com.pingan.cdn.exception.ErrorCode;
import cn.com.pingan.cdn.facade.ContentServiceFacade;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
import cn.com.pingan.cdn.request.VendorInfoDTO;
import cn.com.pingan.cdn.request.openapi.ContentDefaultNumDTO;
import cn.com.pingan.cdn.request.openapi.OpenApiFreshCommand;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @Classname OpenApiContentController
 * @Description TODO
 * @Date 2020/10/30 13:51
 * @Created by Lu J
 */
@Slf4j
@RestController
@RequestMapping("/base/api")
public class OpenApiContentController {


    private @Autowired
    HttpServletRequest request;

    private @Autowired
    ContentServiceFacade facade;

    @PostMapping("/refresh/url")
    public ApiReceipt refreshUrl(@RequestBody OpenApiFreshCommand command) throws ContentException, DomainException {
        log.info("content/fresh/url start command:{}", JSON.toJSONString(command));
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (!requestHeaderCheck(dto)) return  ApiReceipt.error(ErrorCode.NOHEADER);

        //越权校验
        if(!"true".equals(dto.getIsAdmin())) {
            if (StringUtils.isEmpty(dto.getSpcode()) ) {
                return  ApiReceipt.error(ErrorCode.FORBIDOPT);
            }
        }

        ApiReceipt result=this.facade.openApiRefreshUrl(dto,command.getData());
        log.info("content/fresh/url end result:{}", JSON.toJSONString(result));
        return result;
    }


    @PostMapping("/refresh/dir")
    public ApiReceipt refreshDir(@RequestBody OpenApiFreshCommand command) throws ContentException, DomainException {
        log.info("content/fresh/dir start command:{}", JSON.toJSONString(command));
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (!requestHeaderCheck(dto)) return  ApiReceipt.error(ErrorCode.NOHEADER);

        //越权校验
        if(!"true".equals(dto.getIsAdmin())) {
            if (StringUtils.isEmpty(dto.getSpcode())) {
                return  ApiReceipt.error(ErrorCode.FORBIDOPT);
            }
        }

        ApiReceipt result=this.facade.openApiRefreshDir(dto,command.getData());
        log.info("content/fresh/dir end result:{}", JSON.toJSONString(result));
        return result;
    }

    @PostMapping("/preload")
    public ApiReceipt prefetch(@RequestBody OpenApiFreshCommand command) throws ContentException,DomainException{
        log.info("content/preheat start command:{}", JSON.toJSONString(command));

        GateWayHeaderDTO dto=this.getGateWayInfo(request);
        if (!requestHeaderCheck(dto)) return  ApiReceipt.error(ErrorCode.NOHEADER);

        //越权校验
        if(!"true".equals(dto.getIsAdmin())) {
            if (StringUtils.isEmpty(dto.getSpcode())) {
                return  ApiReceipt.error(ErrorCode.FORBIDOPT);
            }
        }

        ApiReceipt result =this.facade.openApiPreload(dto,command.getData());
        log.info("content/preheat end result:{}", JSON.toJSONString(result));
        return result;
    }


    /**
     * 设置用户刷新预热每日上限
     * @param command
     * @return
     * @throws ContentException
     */
    @PostMapping("/user/content/number")
    public ApiReceipt setUserContentNumber(@RequestBody ContentLimitDTO command) throws ContentException {
        log.info("user/content/number start command:{}", JSON.toJSONString(command));
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (!requestHeaderCheck(dto)) return  ApiReceipt.error(ErrorCode.NOHEADER);

        //越权校验
        if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
            return  ApiReceipt.error(ErrorCode.FORBIDOPT);
        }
        ApiReceipt result=this.facade.setUserContentNumber(command);
        log.info("user/content/number end result:{}", JSON.toJSONString(result));
        return result;
    }

    /**
     * 查询用户刷新预热每日剩余用量
     * @param spCode
     * @return
     * @throws ContentException
     * @throws IOException
     */
    @GetMapping("/user/content/number")
    public ApiReceipt getUserContentNumber(@RequestParam(required = false) String spCode) throws ContentException, IOException {
        log.info("查询user/content/number start command:{}", spCode);
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (!requestHeaderCheck(dto)) return  ApiReceipt.error(ErrorCode.NOHEADER);
        //越权校验
        if("true".equalsIgnoreCase(dto.getIsAdmin())){
           if(StringUtils.isBlank(spCode)) return  ApiReceipt.error(ErrorCode.FORBIDOPT);
        }else {
            spCode = dto.getSpcode();
        }
        ApiReceipt result=this.facade.getUserContentNumber(spCode);
        log.info("查询user/content/number end result:{}", JSON.toJSONString(result));
        return result;
    }

    /**
     * 设置用户默认用量上限
     * @param command
     * @return
     * @throws ContentException
     */
    @PostMapping("/user/content/limit")
    public ApiReceipt setUserLimitNumber(@RequestBody ContentDefaultNumDTO command) throws ContentException {
        log.info("user/content/limit start command:{}", JSON.toJSONString(command));
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (!requestHeaderCheck(dto)) return  ApiReceipt.error(ErrorCode.NOHEADER);

        //越权校验
        if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
            return  ApiReceipt.error(ErrorCode.FORBIDOPT);
        }
        ApiReceipt result=this.facade.setUserLimitNumber(command);
        log.info("user/content/limit end result:{}", JSON.toJSONString(result));
        return result;
    }

    /**
     * 查询用户默认用量上限
     * @param spCode
     * @return
     * @throws ContentException
     * @throws IOException
     */
    @GetMapping("/user/content/limit")
    public ApiReceipt getUserLimitNumber(@RequestParam(required = false) String spCode) throws ContentException, IOException {
        log.info("查询user/content/limit start command:{}", spCode);
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (!requestHeaderCheck(dto)) return  ApiReceipt.error(ErrorCode.NOHEADER);
        //越权校验
        if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
            return  ApiReceipt.error(ErrorCode.FORBIDOPT);
        }
        ApiReceipt result=this.facade.getUserLimitNumber(spCode);
        log.info("查询user/content/limit end result:{}", JSON.toJSONString(result));
        return result;
    }


    /////
    /**
     * 添加厂商信息
     * @param command
     * @return
     * @throws ContentException
     */
    @PostMapping("/vendor/content/info/add")
    public ApiReceipt addVendorInfo(@RequestBody VendorInfoDTO command) throws ContentException {
        log.info("vendor/content/info/add start command:{}", JSON.toJSONString(command));
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (!requestHeaderCheck(dto)) return  ApiReceipt.error(ErrorCode.NOHEADER);

        //越权校验
        if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
            return  ApiReceipt.error(ErrorCode.FORBIDOPT);
        }
        ApiReceipt result=this.facade.addVendorInfo(command);
        log.info("vendor/content/info/add end result:{}", JSON.toJSONString(result));
        return result;
    }

    /**
     * 查询厂商信息
     * @param vendor
     * @return
     * @throws ContentException
     * @throws IOException
     */
    @GetMapping("/vendor/content/info/query")
    public ApiReceipt getVendorInfo(@RequestParam String vendor) throws ContentException, IOException {
        log.info("查询vendor/content/info start command:{}", vendor);
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (!requestHeaderCheck(dto)) return  ApiReceipt.error(ErrorCode.NOHEADER);
        //越权校验
        if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
            return  ApiReceipt.error(ErrorCode.FORBIDOPT);
        }
        ApiReceipt result=this.facade.getVendorInfo(vendor);
        log.info("查询vendor/content/info end result:{}", JSON.toJSONString(result));
        return result;
    }

    /**
     * 设置厂商信息
     * @param command
     * @return
     * @throws ContentException
     * @throws IOException
     */
    @PostMapping("/vendor/content/info/set")
    public ApiReceipt setVendorInfo(@RequestBody VendorInfoDTO command) throws ContentException, IOException {
        log.info("vendor/content/info/set start command:{}", command);
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (!requestHeaderCheck(dto)) return  ApiReceipt.error(ErrorCode.NOHEADER);
        //越权校验
        if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
            return  ApiReceipt.error(ErrorCode.FORBIDOPT);
        }
        ApiReceipt result=this.facade.setVendorInfo(command);
        log.info("vendor/content/info/set end result:{}", JSON.toJSONString(result));
        return result;
    }

    /**
     * 设置厂商信息
     * @param command
     * @return
     * @throws ContentException
     * @throws IOException
     */
    @PostMapping("/vendor/content/info/status")
    public ApiReceipt setVendorInfoStatus(@RequestBody VendorInfoDTO command) throws ContentException, IOException {
        log.info("vendor/content/info/set start command:{}", command);
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (!requestHeaderCheck(dto)) return  ApiReceipt.error(ErrorCode.NOHEADER);
        //越权校验
        if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
            return  ApiReceipt.error(ErrorCode.FORBIDOPT);
        }
        ApiReceipt result=this.facade.setVendorStatus(command);
        log.info("vendor/content/info/status end result:{}", JSON.toJSONString(result));
        return result;
    }


    public Boolean requestHeaderCheck(GateWayHeaderDTO dto) throws ContentException {
        if(StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid())||StringUtils.isEmpty(dto.getSpcode())) {
            return false;
        }
        return true;
    }

    private GateWayHeaderDTO getGateWayInfo(HttpServletRequest request) {
//          Enumeration<String> names=request.getHeaderNames();
//          while (names.hasMoreElements()){
//              String name=(String)names.nextElement();
//              log.info(name+":"+request.getHeader(name));
//          }
        GateWayHeaderDTO dto = new GateWayHeaderDTO();
        dto.setUid(request.getHeader("uid") != null ? String.valueOf(request.getHeader("uid")) : null);
        dto.setIsAdmin(request.getHeader("isAdmin") != null ? String.valueOf(request.getHeader("isAdmin")) : null);
        dto.setChannel(request.getHeader("channel") != null ? String.valueOf(request.getHeader("channel")) : null);
        dto.setUsername(request.getHeader("username") != null ? String.valueOf(request.getHeader("username")) : null);
        dto.setSubAccount(request.getHeader("subAccount") != null ? String.valueOf(request.getHeader("subAccount")) : null);
        dto.setSpcode(request.getHeader("spcode") != null ? String.valueOf(request.getHeader("spcode")) : null);
        dto.setToken(request.getHeader("token") != null ? String.valueOf(request.getHeader("token")) : null);
        return dto;
    }

}
