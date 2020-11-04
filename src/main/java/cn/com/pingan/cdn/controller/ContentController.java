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
import cn.com.pingan.cdn.exception.ContentException;
import cn.com.pingan.cdn.exception.DomainException;
import cn.com.pingan.cdn.exception.ErrorCode;
import cn.com.pingan.cdn.facade.ContentServiceFacade;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
import cn.com.pingan.cdn.validator.content.FreshCommand;
import cn.com.pingan.cdn.validator.content.QueryHisCommand;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;



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

        public static int SINGLE_URL_REFRESH_LIMIT = 60; //单次url刷新上限

        public static int SINGLE_DIR_REFRESH_LIMIT = 10;  //单次目录刷新上限

        public static int SINGLE_URL_PRELOAD_LIMIT = 60;  //单次url预热上限

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

            if (null == command.getData() || command.getData().size() == 0) {

                return ApiReceipt.error(ErrorCode.PARAMILLEGAL);
            }

            if (command.getData().size() > SINGLE_DIR_REFRESH_LIMIT) {
                return ApiReceipt.error(ErrorCode.OUTLIMITSINGLE);
            }

            //越权校验
            if (!"true".equals(dto.getIsAdmin())) {
                if (StringUtils.isEmpty(command.getSpCode()) || !dto.getSpcode().equals(command.getSpCode())) {
                    return ApiReceipt.error(ErrorCode.FORBIDOPT);
                }
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

            if (null == command.getData() || command.getData().size() == 0) {

                return ApiReceipt.error(ErrorCode.PARAMILLEGAL);
            }

            if (command.getData().size() > SINGLE_URL_REFRESH_LIMIT) {
                return ApiReceipt.error(ErrorCode.OUTLIMITSINGLE);
            }

            //越权校验
            if (!"true".equals(dto.getIsAdmin())) {
                if (StringUtils.isEmpty(command.getSpCode()) || !dto.getSpcode().equals(command.getSpCode())) {
                    return ApiReceipt.error(ErrorCode.FORBIDOPT);
                }
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

            if (null == command.getData() || command.getData().size() == 0) {

                return ApiReceipt.error(ErrorCode.PARAMILLEGAL);
            }

            if (command.getData().size() > SINGLE_URL_PRELOAD_LIMIT) {
                return ApiReceipt.error(ErrorCode.OUTLIMITSINGLE);
            }

            //越权校验
            if (!"true".equals(dto.getIsAdmin())) {
                if (StringUtils.isEmpty(command.getSpCode()) || !dto.getSpcode().equals(command.getSpCode())) {
                    return ApiReceipt.error(ErrorCode.FORBIDOPT);
                }
            }

            ApiReceipt result =this.facade.prefetch(dto,command);
            log.info("content/preheat end result:{}", JSON.toJSONString(result));
            return result;
        }
        
        @GetMapping("/test")
        public ApiReceipt testurl(String s) throws ContentException, DomainException {
            ApiReceipt result = new ApiReceipt();
            return  ApiReceipt.error(ErrorCode.PARAMILLEGAL);
            
            //this.facade.test();
            //return result;
        }
        
        @PostMapping("/test/refreshurl")
        public ApiReceipt test(@Valid @RequestBody FreshCommand command) throws ContentException, DomainException {
            GateWayHeaderDTO dto = new GateWayHeaderDTO();
            dto.setIsAdmin("true");
            dto.setSpcode("1563872277077");
            dto.setUid("e72c4493b8da6b489e7f8d8a641b1481196e026c42815942380dfec61cdce93c");
            ApiReceipt result = this.facade.refreshUrl(dto,command);
            log.info("content/fresh/url end result:{}", JSON.toJSONString(result));
            return result;
        }

        @PostMapping("/test/queryHis")
        public ApiReceipt testQueryHis(@Valid @RequestBody QueryHisCommand command) throws ContentException, DomainException {
            GateWayHeaderDTO dto = new GateWayHeaderDTO();
            dto.setIsAdmin("true");
            dto.setSpcode("1563872277077");
            dto.setUid("e72c4493b8da6b489e7f8d8a641b1481196e026c42815942380dfec61cdce93c");
            ApiReceipt result =ApiReceipt.ok().data(this.facade.queryHis(dto,command));
            log.info("content/fresh/url end result:{}", JSONObject.toJSONString(result));
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
                if (StringUtils.isEmpty(command.getSpCode()) || !dto.getSpcode().equals(command.getSpCode())) {
                    return  ApiReceipt.error(ErrorCode.FORBIDOPT);
                }
            }

            ApiReceipt result =ApiReceipt.ok().data(this.facade.queryHis(dto,command));
            log.info("content/queryHis end result:{}", JSON.toJSONString(result));
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
        
}
