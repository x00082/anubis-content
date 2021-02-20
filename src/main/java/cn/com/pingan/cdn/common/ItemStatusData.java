package cn.com.pingan.cdn.common;

import lombok.Data;

import java.util.Map;

/**
 * @Classname ItemStatusData
 * @Description TODO
 * @Date 2021/2/18 16:48
 * @Created by Luj
 */
@Data
public class ItemStatusData {
    private TaskStatus status;
    private Map<String, String> itemMap;
}
