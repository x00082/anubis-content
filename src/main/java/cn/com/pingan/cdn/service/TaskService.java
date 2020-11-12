package cn.com.pingan.cdn.service;

import cn.com.pingan.cdn.exception.RestfulException;
import cn.com.pingan.cdn.model.mysql.VendorContentTask;
import cn.com.pingan.cdn.model.mysql.VendorInfo;
import cn.com.pingan.cdn.rabbitmq.message.TaskMsg;

/**
 * @Classname TaskService
 * @Description TODO
 * @Date 2020/10/20 15:25
 * @Created by Luj
 */
public interface TaskService {

    public void pushTaskMsg(TaskMsg msg) throws RestfulException;

    public int addTaskVersion(TaskMsg msg) throws RestfulException;

    public int handlerTask(TaskMsg msg) throws RestfulException;

    public int handlerMergeTask(TaskMsg msg) throws RestfulException;

    //public JSONObject queryRefreshPreloadTask(RefreshPreloadData data) throws RestfulException;

    //public JSONObject queryRefreshPreloadTaskStatus(RefreshPreloadTaskStatusDTO request) throws RestfulException;

    public Boolean handlerNewRequest(VendorContentTask task, TaskMsg msg) throws RestfulException;
    
    public Boolean handlerNewRequestUrl(TaskMsg msg) throws RestfulException;
    public Boolean handlerNewRequestDir(TaskMsg msg) throws RestfulException;
    public Boolean handlerNewRequestPreload(TaskMsg msg) throws RestfulException;
    public Boolean handlerRoundRobin(TaskMsg msg) throws RestfulException;

    public Boolean handlerRoundRobin(VendorContentTask task, TaskMsg msg) throws RestfulException;

    public Boolean handlerSuccess(VendorContentTask task, TaskMsg msg) throws RestfulException;

    public Boolean handlerFail(VendorContentTask task, TaskMsg msg) throws RestfulException;

    public VendorInfo findVendorInfo(String vendor) throws RestfulException;



}
