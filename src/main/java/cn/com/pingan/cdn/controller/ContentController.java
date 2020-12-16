/**   
 * @Project: anubis-content
 * @File: ContentController.java 
 * @Package cn.com.pingan.cdn.controller 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 上午10:09:09 
 */
package cn.com.pingan.cdn.controller;

import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.common.RedoDTO;
import cn.com.pingan.cdn.common.StaticValue;
import cn.com.pingan.cdn.exception.ContentException;
import cn.com.pingan.cdn.exception.DomainException;
import cn.com.pingan.cdn.exception.ErrorCode;
import cn.com.pingan.cdn.facade.ContentServiceFacade;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
import cn.com.pingan.cdn.request.VendorInfoDTO;
import cn.com.pingan.cdn.request.openapi.ContentDefaultNumDTO;
import cn.com.pingan.cdn.validator.content.FreshCommand;
import cn.com.pingan.cdn.validator.content.QueryHisCommand;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;


/**
 * @ClassName: ContentController 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年9月30日 上午10:09:09 
 *  
 */
@RestController
@RequestMapping("/content")
public class ContentController {

        private static final Logger log = org.slf4j.LoggerFactory.getLogger(ContentController.class);
        
        private @Autowired ContentServiceFacade facade;
        
        private @Autowired HttpServletRequest request;
      
        /**
         * 4.1 缓存刷新 目录刷新接口
         * @param command
         * @return
         * @throws ContentException 
         */
        @PutMapping("/refresh/dir")
        public ApiReceipt freshDir(@Valid @RequestBody FreshCommand command) throws ContentException, DomainException {
            log.info("content/fresh/dir start command:{}", JSON.toJSONString(command));
            GateWayHeaderDTO dto = this.getGateWayInfo(request);
            if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
                return ApiReceipt.error(ErrorCode.NOHEADER);
            }

            //越权校验
            if (!"true".equals(dto.getIsAdmin())) {
                if (StringUtils.isEmpty(command.getSpCode()) || !dto.getSpcode().equals(command.getSpCode())) {
                    return ApiReceipt.error(ErrorCode.FORBIDOPT);
                }
            }

            if (null == command.getData() || command.getData().size() == 0) {

                return ApiReceipt.error(ErrorCode.PARAMILLEGAL);
            }

            if (command.getData().size() > StaticValue.SINGLE_DIR_REFRESH_LIMIT) {
                return ApiReceipt.error(ErrorCode.OUTLIMITSINGLE);
            }


            ApiReceipt result = this.facade.refreshDir(dto, command);
            log.info("content/fresh/dir end result:{}", JSON.toJSONString(result));
            return result;

        }

        /***
        * @Description:  缓存刷新 RL刷新接口
        * @Param:
        * @return:
        */
        @PutMapping("/refresh/url")
        public ApiReceipt freshUrl(@Valid @RequestBody FreshCommand command) throws ContentException, DomainException {
            log.info("content/fresh/url start command:{}", JSON.toJSONString(command));
            GateWayHeaderDTO dto = this.getGateWayInfo(request);
            if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
                return ApiReceipt.error(ErrorCode.NOHEADER);
            }

            //越权校验
            if (!"true".equals(dto.getIsAdmin())) {
                if (StringUtils.isEmpty(command.getSpCode()) || !dto.getSpcode().equals(command.getSpCode())) {
                    return ApiReceipt.error(ErrorCode.FORBIDOPT);
                }
            }

            if (null == command.getData() || command.getData().size() == 0) {

                return ApiReceipt.error(ErrorCode.PARAMILLEGAL);
            }

            if (command.getData().size() > StaticValue.SINGLE_URL_REFRESH_LIMIT) {
                return ApiReceipt.error(ErrorCode.OUTLIMITSINGLE);
            }


            ApiReceipt result=this.facade.refreshUrl(dto,command);
            log.info("content/fresh/url end result:{}", JSON.toJSONString(result));
            return result;
        }

        
        /**
         * 4.2 缓存预取
         * @param command
         * @return
         */
        @PutMapping("/preheat")
        public ApiReceipt prefetch(@Valid @RequestBody FreshCommand command) throws ContentException, DomainException {
            log.info("content/preheat start command:{}", JSON.toJSONString(command));
            // TODO
            GateWayHeaderDTO dto = this.getGateWayInfo(request);
            if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
                return ApiReceipt.error(ErrorCode.NOHEADER);
            }

            //越权校验
            if (!"true".equals(dto.getIsAdmin())) {
                if (StringUtils.isEmpty(command.getSpCode()) || !dto.getSpcode().equals(command.getSpCode())) {
                    return ApiReceipt.error(ErrorCode.FORBIDOPT);
                }
            }

            if (null == command.getData() || command.getData().size() == 0) {

                return ApiReceipt.error(ErrorCode.PARAMILLEGAL);
            }

            if (command.getData().size() > StaticValue.SINGLE_URL_PRELOAD_LIMIT) {
                return ApiReceipt.error(ErrorCode.OUTLIMITSINGLE);
            }

            ApiReceipt result =this.facade.prefetch(dto,command);
            log.info("content/preheat end result:{}", JSON.toJSONString(result));
            return result;
        }

        @PostMapping("/redo")
        public ApiReceipt reDo(@Valid @RequestBody RedoDTO command) throws ContentException, DomainException {
            log.info("content/preheat start command:{}", JSON.toJSONString(command));
            // TODO
            GateWayHeaderDTO dto = this.getGateWayInfo(request);
            if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
                return ApiReceipt.error(ErrorCode.NOHEADER);
            }


            //越权校验
            if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
                return  ApiReceipt.error(ErrorCode.FORBIDOPT);
            }
            boolean force = false;
            if(command.getForce() == null){
                force = false;
            }else{
                force = command.getForce();
            }
            ApiReceipt result =this.facade.redo(command.getTaskId(), force);
            log.info("content/preheat end result:{}", JSON.toJSONString(result));
            return result;
        }

        @PostMapping("/redo/batch")
        public ApiReceipt batchRedo(@Valid @RequestBody RedoDTO command) throws ContentException, DomainException {
            log.info("content/preheat start command:{}", JSON.toJSONString(command));
            // TODO
            GateWayHeaderDTO dto = this.getGateWayInfo(request);
            if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
                return ApiReceipt.error(ErrorCode.NOHEADER);
            }


            //越权校验
            if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
                return  ApiReceipt.error(ErrorCode.FORBIDOPT);
            }
            boolean force = false;
            if(command.getForce() == null){
                force = false;
            }else{
                force = command.getForce();
            }
            ApiReceipt result =this.facade.batchRedo(command.getTaskIds(), force);
            log.info("content/preheat end result:{}", JSON.toJSONString(result));
            return result;
        }

        /***
        * @Description:  操作历史查询接口
        * @Param:
        * @return:
        */
        //TODOs
        @PostMapping("/queryHis")
        public ApiReceipt queryHis(@Valid @RequestBody QueryHisCommand command) throws ContentException, DomainException {

            log.info("content/queryHis start command:{}", JSON.toJSONString(command));
            // TODO
            GateWayHeaderDTO dto = this.getGateWayInfo(request);
            if(StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid())) {
                return  ApiReceipt.error(ErrorCode.NOHEADER);
            }
            //越权校验
            if(!"true".equals(dto.getIsAdmin())) {
                if (StringUtils.isEmpty(dto.getSpcode())) {
                    return  ApiReceipt.error(ErrorCode.FORBIDOPT);
                }
            }

            ApiReceipt result =ApiReceipt.ok().data(this.facade.queryHis(dto,command));
            log.info("content/queryHis end result:{}", JSON.toJSONString(result));
            return result;
        }

        @GetMapping("/queryDetails")
        public ApiReceipt queryDetails(@RequestParam String requestId) throws ContentException, DomainException {

            log.info("content/queryDetails start command:{}", requestId);
            // TODO
            GateWayHeaderDTO dto = this.getGateWayInfo(request);
            if(StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid())) {
                return  ApiReceipt.error(ErrorCode.NOHEADER);
            }
            //越权校验
            if(!"true".equals(dto.getIsAdmin())) {
                if (StringUtils.isEmpty(dto.getSpcode())) {
                    return  ApiReceipt.error(ErrorCode.FORBIDOPT);
                }
            }

            ApiReceipt result = this.facade.queryDetails(requestId);
            log.info("content/queryDetails end result:{}", JSON.toJSONString(result));
            return result;
        }



        @PostMapping("/importHis")
        public ApiReceipt importHis(@Valid @RequestBody QueryHisCommand command) throws ContentException, DomainException {

            log.info("content/importHis start command:{}", JSON.toJSONString(command));
            // TODO
            GateWayHeaderDTO dto = this.getGateWayInfo(request);
            if(StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid())) {
                return  ApiReceipt.error(ErrorCode.NOHEADER);
            }
            //越权校验
            if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
                return  ApiReceipt.error(ErrorCode.FORBIDOPT);
            }

            ApiReceipt result =this.facade.exportAndImport(dto,command);
            log.info("content/importHis end result:{}", JSON.toJSONString(result));
            return result;
        }

        @GetMapping("/queryImportTask")
        public ApiReceipt queryImportTask(@RequestParam String exportId) throws ContentException, DomainException {

            log.info("content/queryImportTask start command:{}", exportId);
            // TODO
            GateWayHeaderDTO dto = this.getGateWayInfo(request);
            if(StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid())) {
                return  ApiReceipt.error(ErrorCode.NOHEADER);
            }
            //越权校验
            if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
                return  ApiReceipt.error(ErrorCode.FORBIDOPT);
            }

            ApiReceipt result = this.facade.queryImportTask(exportId);
            log.info("content/queryImportTask end result:{}", JSON.toJSONString(result));
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
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }

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
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }
        //越权校验
        if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
            return  ApiReceipt.error(ErrorCode.FORBIDOPT);
        }
        ApiReceipt result=this.facade.getUserLimitNumber(spCode);
        log.info("查询user/content/limit end result:{}", JSON.toJSONString(result));
        return result;
    }


    /**
     * 添加厂商信息
     * @param command
     * @return
     * @throws ContentException
     */
    @PostMapping("/vendor/info/add")
    public ApiReceipt addVendorInfo(@RequestBody VendorInfoDTO command) throws ContentException {
        log.info("vendor/content/info/add start command:{}", JSON.toJSONString(command));
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }

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
    @GetMapping("/vendor/info/query")
    public ApiReceipt getVendorInfo(@RequestParam String vendor) throws ContentException, IOException {
        log.info("查询vendor/content/info start command:{}", vendor);
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }
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
    @PostMapping("/vendor/info/set")
    public ApiReceipt setVendorInfo(@RequestBody VendorInfoDTO command) throws ContentException, IOException {
        log.info("vendor/content/info/set start command:{}", command);
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }
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
    @PostMapping("/vendor/info/status")
    public ApiReceipt setVendorInfoStatus(@RequestBody VendorInfoDTO command) throws ContentException, IOException {
        log.info("vendor/content/info/set start command:{}", command);
        GateWayHeaderDTO dto= this.getGateWayInfo(request);
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }
        //越权校验
        if(!"true".equalsIgnoreCase(dto.getIsAdmin())){
            return  ApiReceipt.error(ErrorCode.FORBIDOPT);
        }
        ApiReceipt result=this.facade.setVendorStatus(command);
        log.info("vendor/content/info/status end result:{}", JSON.toJSONString(result));
        return result;
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


    @PutMapping("/refresh/test")
    public ApiReceipt test(@Valid @RequestBody FreshCommand command) throws ContentException, DomainException {
        log.info("content/fresh/dir start command:{}", JSON.toJSONString(command));
        GateWayHeaderDTO dto = this.getGateWayInfo(request);
        if (StringUtils.isEmpty(dto.getUsername()) || StringUtils.isEmpty(dto.getUid()) || StringUtils.isEmpty(dto.getSpcode())) {
            return ApiReceipt.error(ErrorCode.NOHEADER);
        }

        //越权校验
        if (!"true".equals(dto.getIsAdmin())) {
            if (StringUtils.isEmpty(command.getSpCode()) || !dto.getSpcode().equals(command.getSpCode())) {
                return ApiReceipt.error(ErrorCode.FORBIDOPT);
            }
        }

        if (null == command.getData() || command.getData().size() == 0) {

            return ApiReceipt.error(ErrorCode.PARAMILLEGAL);
        }

        if (command.getData().size() > StaticValue.SINGLE_DIR_REFRESH_LIMIT) {
            return ApiReceipt.error(ErrorCode.OUTLIMITSINGLE);
        }


        ApiReceipt result = this.facade.test(dto, command);
        log.info("content/fresh/dir end result:{}", JSON.toJSONString(result));
        return result;
    }

}
