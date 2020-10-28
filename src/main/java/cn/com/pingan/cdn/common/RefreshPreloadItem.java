package cn.com.pingan.cdn.common;

import lombok.Data;

/**
 * @Classname RefreshPreloadItem
 * @Description TODO
 * @Date 2020/10/21 19:20
 * @Created by Luj
 */
@Data
public class RefreshPreloadItem {

    private String jobType; //refresh / preload

    private String jobId;  //刷新，预热任务id

    public RefreshPreloadItem(){

    }
    public RefreshPreloadItem(String jobType, String jobId) {
        this.jobType = jobType;
        this.jobId = jobId;
    }
}
