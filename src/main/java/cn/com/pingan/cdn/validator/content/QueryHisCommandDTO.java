package cn.com.pingan.cdn.validator.content;

import cn.com.pingan.cdn.validator.Command;
import lombok.Data;

/**
 * @Classname QueryHisCommand
 * @Description TODO
 * @Date 2020/10/30 11:28
 * @Created by Luj
 */
@Data
public class QueryHisCommandDTO implements Command {
    private String startTime;
    private String endTime;
    private String type;
    private String url;
    private int pageIndex;
    private int pageSize;
    private String taskId;
    private String channel;
    private String account;
    private String subAccount;
    private String uid;
    private String status;

    private String spCode;
    private String operateAccount;//管理端按操作用户去查询
}
