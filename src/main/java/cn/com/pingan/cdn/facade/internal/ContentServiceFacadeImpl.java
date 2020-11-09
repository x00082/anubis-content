/**   
 * @Project: anubis-content
 * @File: ContentServiceFacadeImpl.java 
 * @Package cn.com.pingan.cdn.facade.internal 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年9月30日 下午4:53:01 
 */
package cn.com.pingan.cdn.facade.internal;

import cn.com.pingan.cdn.BaseUser;
import cn.com.pingan.cdn.common.ApiReceipt;
import cn.com.pingan.cdn.common.ContentLimitDTO;
import cn.com.pingan.cdn.common.RefreshType;
import cn.com.pingan.cdn.exception.ContentException;
import cn.com.pingan.cdn.facade.ContentServiceFacade;
import cn.com.pingan.cdn.gateWay.GateWayHeaderDTO;
import cn.com.pingan.cdn.model.mysql.ContentHistory;
import cn.com.pingan.cdn.repository.mysql.ContentHistoryRepository;
import cn.com.pingan.cdn.request.QueryHisDTO;
import cn.com.pingan.cdn.request.VendorInfoDTO;
import cn.com.pingan.cdn.request.openapi.ContentDefaultNumDTO;
import cn.com.pingan.cdn.response.ContentHisDTO;
import cn.com.pingan.cdn.response.openapi.OpenApiUserContentHisDTO;
import cn.com.pingan.cdn.service.ConfigService;
import cn.com.pingan.cdn.service.ContentService;
import cn.com.pingan.cdn.service.UserRpcService;
import cn.com.pingan.cdn.validator.content.FreshCommand;
import cn.com.pingan.cdn.validator.content.QueryHisCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @Autowired
    private ConfigService configService;

    @Autowired
    private ContentHistoryRepository contentHistoryRepository;

    private @Autowired
    UserRpcService userRpcService;

    public static final String BASE_USERT_TYPE_ACCOUNT= "Account";
    
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

    @Override
    public ApiReceipt reDO(String id, boolean flag) throws ContentException {
        return contentService.redoContentTask(id, flag);
    }

    @Override
    public QueryHisDTO queryHis(GateWayHeaderDTO dto, QueryHisCommand command) {
        QueryHisDTO queryHisDTO = new QueryHisDTO();
        int pageIndex = command.getPageIndex();
        int pageSize = command.getPageSize();
        pageIndex = pageIndex > 0 ? pageIndex - 1 : pageIndex;
        pageSize = (pageSize > 0 && pageSize <= 100) ? pageSize : 100;
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, sort);

        //管理员取command的账户数据中的用户UUID，commad是主账号则包含子账号
        //用户角色 主账号取SSOAUTH主账号和子账号的uuid
        //用户角色 子账号取SSOTUTH子账号的
        List<BaseUser> userList = userRpcService.queryAllAccount();//查所有用户的信息

        List<String> uuidList = new ArrayList<>();

        //拼装用户uuid查询条件
        queryUserUuid(dto, command, userList,uuidList);

        List<String> finalUuidList = uuidList;
        Page<ContentHistory> pager = this.contentHistoryRepository.findAll(new Specification<ContentHistory>() {
            private static final long serialVersionUID = 1L;

            public Predicate toPredicate(Root<ContentHistory> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> cond = new ArrayList<>();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Date startDate = null;
                Date endDate = null;
                try {
                    if (!StringUtils.isEmpty(command.getStartTime())) {
                        startDate = df.parse(command.getStartTime());
                    }else{
                        //默认查10天内的数据
                        startDate = preNDay(10);
                    }
                    if (!StringUtils.isEmpty(command.getEndTime())) {
                        endDate = df.parse(command.getEndTime());
                    }else{
                        endDate = new Date();
                    }
                } catch (ParseException e) {
                    log.error("", e);
                }

//                if (!StringUtils.isEmpty(dto.getUid()) && !"true".equals(dto.getIsAdmin())) {//非管理员加判断
//                    //非管理员角色  主账号查询包含子账号的刷新记录，子账号只能查询子账号刷新记录
//                    cond.add(criteriaBuilder.equal(root.get("userId"), dto.getUid()));
//                }
//                if (!StringUtils.isEmpty(command.getOperateAccount()) && "true".equals(dto.getIsAdmin()) && null != finalUuidList && finalUuidList.size() > 0) {//管理员新增用户过滤
//
//                    Path<Object> path = root.get("userId");//定义查询的字段
//
//                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(path);
//                    for (int i = 0; i < finalUuidList.size(); i++) {
//                        in.value(finalUuidList.get(i));//存入值
//                    }
//                    cond.add(criteriaBuilder.and(criteriaBuilder.and(in)));//存入结果集
//                }
                if(null != finalUuidList && finalUuidList.size()>0){
                    Path<Object> path = root.get("userId");//定义查询的字段

                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(path);
                    for (int i = 0; i < finalUuidList.size(); i++) {
                        in.value(finalUuidList.get(i));//存入值
                    }
                    cond.add(criteriaBuilder.and(criteriaBuilder.and(in)));//存入结果集
                }
                if (!StringUtils.isEmpty(command.getType().name())) {
                    cond.add(criteriaBuilder.equal(root.get("type"), command.getType()));
                }
                if (!StringUtils.isEmpty(command.getTaskId())) {
                    cond.add(criteriaBuilder.equal(root.get("requestId"), command.getTaskId()));
                }
                if (!StringUtils.isEmpty(command.getStatus().name())) {
                    cond.add(criteriaBuilder.equal(root.get("status"), command.getStatus()));
                }
                if (null != startDate) {//大于等于
                    cond.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startDate));
                }
                if (null != endDate) {//小于等于
                    cond.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime").as(Date.class), endDate));
                }
                return query.where(cond.toArray(new Predicate[0])).getRestriction();
            }

        }, pageRequest);


        //用户端屏蔽部分不必要信息
        if (!StringUtils.endsWithIgnoreCase("true", dto.getIsAdmin())) {
            ArrayList<OpenApiUserContentHisDTO> userData = new ArrayList<>();
            OpenApiUserContentHisDTO his = null;
            for (ContentHistory contentHistory : pager) {
                his = new OpenApiUserContentHisDTO();
                BeanUtils.copyProperties(contentHistory, his);
                his.setTaskId(contentHistory.getRequestId());
                his.setOptTime(contentHistory.getCreateTime());
                String dataStatus = contentHistory.getStatus() != null ? contentHistory.getStatus().toString() : "";
                if ("SUCCESS".equals(dataStatus)) {
                    his.setStatus("成功");
                } else if ("FAIL".equals(dataStatus)) {
                    his.setStatus("失败");
                } else {
                    his.setStatus("处理中");
                }
                userData.add(his);
            }
            queryHisDTO.setData(userData);

        } else {

            List<ContentHisDTO> data = new ArrayList<>();
            for (ContentHistory contentHistory : pager) {
                ContentHisDTO tmp = new ContentHisDTO();
                tmp.setId(contentHistory.getId());
                tmp.setOptTime(new Timestamp(contentHistory.getCreateTime() != null ? contentHistory.getCreateTime().getTime() : new Date().getTime()));
                tmp.setContent(contentHistory.getContent());
                tmp.setType(contentHistory.getType() != null ? contentHistory.getType().toString() : "");
                String status = "";
                String dataStatus = contentHistory.getStatus() != null ? contentHistory.getStatus().toString() : "";
                if ("SUCCESS".equals(dataStatus)) {
                    status = "成功";
                } else if ("FAIL".equals(dataStatus)) {
                    status = "失败";
                } else {
                    status = "处理中";
                }
                tmp.setStatus(status);
                tmp.setTaskId(contentHistory.getRequestId());
                tmp.setUserId(contentHistory.getUserId());
                String account = "";
                String channel = "";
                if (null != userList && !StringUtils.isEmpty(contentHistory.getUserId())) {
                    for (BaseUser baseUser : userList) {

                        if (!StringUtils.isEmpty(contentHistory.getUserId()) && !StringUtils.isEmpty(baseUser.getUuid()) &&
                                contentHistory.getUserId().equals(baseUser.getUuid())) {
                            account = baseUser.getAccount();
                            channel = baseUser.getChannel();
                            continue;
                        }
                    }
                }
                tmp.setAccount(account);
                tmp.setChannel(channel);
                data.add(tmp);
            }
            queryHisDTO.setData(data);
        }
        queryHisDTO.setPageIndex(pager.getNumber() + 1);
        queryHisDTO.setPageSize(pager.getNumberOfElements());
        queryHisDTO.setTotalRecords((int) pager.getTotalElements());

        return queryHisDTO;
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
        return contentService.saveContent(dto, data, RefreshType.dir);
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
        return contentService.saveContent(dto, data, RefreshType.url);
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
        return contentService.saveContent(dto, data, RefreshType.preheat);
    }

    @Override
    public ApiReceipt setUserContentNumber(ContentLimitDTO command) {
        return contentService.setUserContentNumber(command);
    }

    @Override
    public ApiReceipt getUserContentNumber(String spCode) throws IOException {
        return contentService.getUserContentNumber(spCode);
    }

    @Override
    public ApiReceipt setUserLimitNumber(ContentDefaultNumDTO command) {
        return contentService.setUserDefaultContentNumber(command);
    }

    @Override
    public ApiReceipt getUserLimitNumber(String spCode) throws IOException {
        return contentService.getUserDefaultContentNumber(spCode);
    }

    @Override
    public ApiReceipt addVendorInfo(VendorInfoDTO command) {
        return configService.addVendorInfo(command);
    }

    @Override
    public ApiReceipt setVendorInfo(VendorInfoDTO command) {
        return configService.modifyVendorInfo(command);
    }

    @Override
    public ApiReceipt getVendorInfo(String vendor) {
        return configService.queryVendorInfo(vendor);
    }

    @Override
    public ApiReceipt setVendorStatus(VendorInfoDTO command) {
        return configService.setVendorStatus(command);
    }


    @Override
    public ApiReceipt test() throws ContentException {
        contentService.test();
        return null;
    }

    private void queryUserUuid(GateWayHeaderDTO dto, QueryHisCommand command, List<BaseUser> userList,List<String> uuidList) {
        HashSet h = new HashSet();
        //查询的用户
        BaseUser account = new BaseUser();
        if (!StringUtils.isEmpty(command.getOperateAccount()) && "true".equals(dto.getIsAdmin())) {//管理员取接口入参的查询对象

            if (null != userList) {
                for (BaseUser baseUser : userList) {
                    if (command.getOperateAccount().equals(baseUser.getAccount())) {
                        account = baseUser;
                        continue;
                    }
                }
            }

        }else if (!"true".equals(dto.getIsAdmin())){
            //取header头中数据
            for (BaseUser baseUser : userList) {
                if (dto.getUid().equals(baseUser.getUuid())) {
                    account = baseUser;
                    continue;
                }
            }

        }

        if(!StringUtils.isEmpty(account.getUuid())&&!StringUtils.isEmpty(account.getUserCode())){

            h.add(account.getUuid());
            if(BASE_USERT_TYPE_ACCOUNT.equals(account.getType())){
                //放子账号的uuid
                for (BaseUser baseUser : userList) {
                    if (account.getUserCode().equals(baseUser.getUserCode())) { //子账号和主账户的spcode相等
                        account = baseUser;
                        h.add(baseUser.getUuid());
                        continue;
                    }
                }

            }
        }
        uuidList.addAll(h);
    }


    private Date preNDay(int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - i);
        return calendar.getTime();

    }

}
