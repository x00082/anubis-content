package cn.com.pingan.cdn.common;

import lombok.Data;

/**
 * @Classname RobinStatus
 * @Description TODO
 * @Date 2020/11/20 18:50
 * @Created by Luj
 */
@Data
public class RobinStatus {
    private String jobId;
    private TaskStatus taskStatus;
}
