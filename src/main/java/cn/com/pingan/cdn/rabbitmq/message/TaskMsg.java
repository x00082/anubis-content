package cn.com.pingan.cdn.rabbitmq.message;

import cn.com.pingan.cdn.common.RefreshType;
import cn.com.pingan.cdn.common.TaskOperationEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Classname TaskMsg
 * @Description TODO
 * @Date 2020/10/19 17:00
 * @Created by Luj
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskMsg {

    //主体字段
    private Long id;
    private String taskId;
    private Integer version;
    private TaskOperationEnum operation;

    //辅助字段
    private Integer hisVersion;
    private Boolean force = false;
    private String vendor;
    private Boolean isLimit = true;
    private Boolean isMerge = false;
    private RefreshType type;
    private Integer size;
    private Long expire = 0L;
    private Long delay = 0L;//ms
    private Integer retryNum = 0;
    private Integer roundRobinNum = 0;

}
