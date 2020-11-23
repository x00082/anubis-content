package cn.com.pingan.cdn.common;

import cn.com.pingan.cdn.model.mysql.VendorContentTask;
import lombok.Data;

import java.util.List;

/**
 * @Classname TaskItem
 * @Description TODO
 * @Date 2020/11/21 21:30
 * @Created by Luj
 */
@Data
public class TaskItem {
    private String vendor;
    private RefreshType type;
    private TaskOperationEnum opt;
    List<VendorContentTask> taskList;

}
