package cn.com.pingan.cdn.controller;


import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.common.ContentLimitDTO;
import cn.com.pingan.cdn.common.StaticValue;
import cn.com.pingan.cdn.exception.ContentException;
import cn.com.pingan.cdn.exception.DomainException;
import cn.com.pingan.cdn.exception.ErrorCode;
import cn.com.pingan.cdn.facade.ContentServiceFacade;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
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
@RequestMapping("/content/api")
public class OpenApiContentController {


    private @Autowired
    HttpServletRequest request;

    private @Autowired
    ContentServiceFacade facade;

    @PostMapping("/refresh/url")
    public ApiReceipt refreshUrl(@RequestBody OpenApiFreshCommand command) throws ContentException, DomainException {
        log.info("api/fresh/url start command:{}", JSON.toJSONString(command));
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }

        //越权校验
        if(!"true".equals(dto.getIsAdmin())) {
            if (StringUtils.isEmpty(dto.getSpcode()) ) {
                return  ApiReceipt.error(ErrorCode.FORBIDOPT);
            }
        }

        if (null == command.getData() || command.getData().size() == 0) {

            return ApiReceipt.error(ErrorCode.PARAMILLEGAL);
        }

        if (command.getData().size() > StaticValue.SINGLE_DIR_REFRESH_LIMIT) {
            return ApiReceipt.error(ErrorCode.OUTLIMITSINGLE);
        }


        ApiReceipt result=this.facade.openApiRefreshUrl(dto,command.getData());
        log.info("api/fresh/url end result:{}", JSON.toJSONString(result));
        return result;
    }


    @PostMapping("/refresh/dir")
    public ApiReceipt refreshDir(@RequestBody OpenApiFreshCommand command) throws ContentException, DomainException {
        log.info("api/fresh/dir start command:{}", JSON.toJSONString(command));
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }

        //越权校验
        if(!"true".equals(dto.getIsAdmin())) {
            if (StringUtils.isEmpty(dto.getSpcode())) {
                return  ApiReceipt.error(ErrorCode.FORBIDOPT);
            }
        }

        if (null == command.getData() || command.getData().size() == 0) {

            return ApiReceipt.error(ErrorCode.PARAMILLEGAL);
        }

        if (command.getData().size() > StaticValue.SINGLE_URL_REFRESH_LIMIT) {
            return ApiReceipt.error(ErrorCode.OUTLIMITSINGLE);
        }

        ApiReceipt result=this.facade.openApiRefreshDir(dto,command.getData());
        log.info("content/fresh/dir end result:{}", JSON.toJSONString(result));
        return result;
    }

    @PostMapping("/preload")
    public ApiReceipt prefetch(@RequestBody OpenApiFreshCommand command) throws ContentException,DomainException{
        log.info("api/preheat start command:{}", JSON.toJSONString(command));

        GateWayHeaderDTO dto=this.getGateWayInfo(request);
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }

        //越权校验
        if(!"true".equals(dto.getIsAdmin())) {
            if (StringUtils.isEmpty(dto.getSpcode())) {
                return  ApiReceipt.error(ErrorCode.FORBIDOPT);
            }
        }

        if (null == command.getData() || command.getData().size() == 0) {

            return ApiReceipt.error(ErrorCode.PARAMILLEGAL);
        }

        if (command.getData().size() > StaticValue.SINGLE_URL_PRELOAD_LIMIT) {
            return ApiReceipt.error(ErrorCode.OUTLIMITSINGLE);
        }

        ApiReceipt result =this.facade.openApiPreload(dto,command.getData());
        log.info("api/preheat end result:{}", JSON.toJSONString(result));
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
        GateWayHeaderDTO dto=this.getGateWayInfo(request);
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }

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
        GateWayHeaderDTO dto=this.getGateWayInfo(request);
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }
        //越权校验
        if("true".equalsIgnoreCase(dto.getIsAdmin())){
            if(StringUtils.isBlank(spCode)) return ApiReceipt.error(ErrorCode.FORBIDOPT);
        }else {
            spCode = dto.getSpcode();
        }
        ApiReceipt result=this.facade.getUserContentNumber(spCode);
        log.info("查询user/content/number end result:{}", JSON.toJSONString(result));
        return result;
    }



    private GateWayHeaderDTO getGateWayInfo(HttpServletRequest request) {
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
