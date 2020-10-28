package cn.com.pingan.cdn.common;

import lombok.Data;

import java.util.List;

/**
 * @Classname RefreshPreloadTaskStatusDTO
 * @Description TODO
 * @Date 2020/10/21 19:20
 * @Created by Luj
 */
@Data
public class RefreshPreloadTaskStatusDTO {
    private List<RefreshPreloadItem> taskList;

    public RefreshPreloadTaskStatusDTO() {
    }

    public RefreshPreloadTaskStatusDTO(List<RefreshPreloadItem> taskList) {
        this.taskList = taskList;
    }
}

