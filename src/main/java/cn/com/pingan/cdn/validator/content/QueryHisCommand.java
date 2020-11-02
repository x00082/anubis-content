package cn.com.pingan.cdn.validator.content;

import cn.com.pingan.cdn.common.HisStatus;
import cn.com.pingan.cdn.common.RefreshType;
import cn.com.pingan.cdn.validator.Command;
import lombok.Data;

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
}
