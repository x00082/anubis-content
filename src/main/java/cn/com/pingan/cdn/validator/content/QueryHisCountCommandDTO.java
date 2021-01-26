package cn.com.pingan.cdn.validator.content;

import cn.com.pingan.cdn.validator.Command;
import lombok.Data;

import java.util.List;

/**
 * @Classname QueryHisCommand
 * @Description TODO
 * @Date 2020/10/30 11:28
 * @Created by Luj
 */
@Data
public class QueryHisCountCommandDTO implements Command {
    private String startTime;
    private String endTime;
    private String type;
    private List<String> domains;
    private List<String> urls;
    private String status;
    private List<String> spCodes;
    private String taskId;
}
