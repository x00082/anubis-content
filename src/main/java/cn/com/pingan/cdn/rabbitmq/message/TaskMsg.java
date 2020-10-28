package cn.com.pingan.cdn.rabbitmq.message;

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

    private Long Id;
    private String taskId;
    private Integer version;
    private TaskOperationEnum operation;

    private Boolean isLimit = true;
    private Long expire = 0L;
    private Long delay = 0L;//ms
    private Integer retryNum = 0;
    private Integer roundRobinNum = 0;

}
