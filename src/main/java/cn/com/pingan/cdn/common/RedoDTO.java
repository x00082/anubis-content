package cn.com.pingan.cdn.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Classname RedocDTO
 * @Description TODO
 * @Date 2020/11/4 17:42
 * @Created by Luj
 */
@Data
@NoArgsConstructor
public class RedoDTO {

    private String taskId;//单个

    private List<String> taskIds;//批量重试

    private Boolean force;
}
