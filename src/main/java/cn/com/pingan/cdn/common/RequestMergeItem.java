package cn.com.pingan.cdn.common;

import cn.com.pingan.cdn.model.mysql.VendorContentTask;
import lombok.Data;
import java.util.List;

/**
 * @Classname RequestMergeItem
 * @Description TODO
 * @Date 2020/11/27 18:21
 * @Created by Luj
 */
@Data
public class RequestMergeItem {
    private String requestId;

    List<VendorContentTask> vendorContentTaskList;
}
