package cn.com.pingan.cdn.response.openapi;

import cn.com.pingan.cdn.common.RefreshType;
import lombok.Data;

import java.util.Date;

/**
 * @Classname OpenApiUserContentHisDTO
 * @Description TODO
 * @Date 2020/10/30 14:48
 * @Created by Luj
 */
@Data
public class OpenApiUserContentHisDTO {
    private Date optTime;
    private String content;
    private RefreshType type;
    private String status;
    private String taskId;
}
