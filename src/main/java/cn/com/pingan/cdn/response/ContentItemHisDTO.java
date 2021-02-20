package cn.com.pingan.cdn.response;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @Classname ContentHisDTO
 * @Description TODO
 * @Date 2020/10/30 14:52
 * @Created by Luj
 */
@Data
public class ContentItemHisDTO {
    //private Long id;
    private Timestamp optTime;
    private String content;
    private String type;
    private String status;
    private String taskId;
    private String itemId;
    private String userId;
    private String account;
    private String channel;
}
