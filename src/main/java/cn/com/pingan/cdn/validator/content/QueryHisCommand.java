package cn.com.pingan.cdn.validator.content;

import cn.com.pingan.cdn.common.HisStatus;
import cn.com.pingan.cdn.common.RefreshType;
import cn.com.pingan.cdn.validator.Command;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @Classname QueryHisCommand
 * @Description TODO
 * @Date 2020/10/30 11:28
 * @Created by Luj
 */
@Data
public class QueryHisCommand implements Command {
    private String startTime;
    private String endTime;
    @Enumerated(EnumType.STRING)
    private RefreshType type;
    private int pageIndex;
    private int pageSize;
    private String taskId;
    private String channel;
    private String account;
    private String subAccount;
    private String uid;
    @Enumerated(EnumType.STRING)
    private HisStatus status;

    private String spCode;
    private String operateAccount;//管理端按操作用户去查询


    public void setWithQueryHisCommandDTO(QueryHisCommandDTO dto){
        this.startTime = dto.getStartTime();
        this.endTime = dto.getEndTime();
        if(StringUtils.isNotEmpty(dto.getType())) {
            this.type = RefreshType.of(dto.getType());
        }
        this.pageIndex = dto.getPageIndex();
        this.pageSize = dto.getPageSize();
        this.taskId = dto.getTaskId();
        this.channel = dto.getChannel();
        this.account = dto.getAccount();
        this.subAccount = dto.getSubAccount();
        this.uid = dto.getUid();
        if(StringUtils.isNotEmpty(dto.getStatus())) {
            this.status = HisStatus.of(dto.getStatus());
        }
        this.spCode = dto.getSpCode();
        this.operateAccount = dto.getOperateAccount();
    }
}
