package cn.com.pingan.cdn.service;

import cn.com.pingan.cdn.common.MergeType;
import cn.com.pingan.cdn.exception.RestfulException;
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

    public int handlerRequestTask(TaskMsg msg) throws RestfulException;

    public int handlerRobinTask(TaskMsg msg) throws RestfulException;

    public int handlerCommonTask(TaskMsg msg) throws RestfulException;

    public int handlerMergeTask(TaskMsg msg, MergeType type) throws RestfulException;

    public int fflushVendorInfoMap(String vendor);

    //public JSONObject queryRefreshPreloadTask(RefreshPreloadData data) throws RestfulException;

    //public JSONObject queryRefreshPreloadTaskStatus(RefreshPreloadTaskStatusDTO request) throws RestfulException;
    
    public Boolean handlerNewRequestUrl(TaskMsg msg) throws RestfulException;
    public Boolean handlerNewRequestDir(TaskMsg msg) throws RestfulException;
    public Boolean handlerNewRequestPreload(TaskMsg msg) throws RestfulException;
    public Boolean handlerRoundRobin(TaskMsg msg) throws RestfulException;

    public Boolean handlerFail(TaskMsg msg) throws RestfulException;
    public Boolean handlerSuccess(TaskMsg msg) throws RestfulException;



    public VendorInfo findVendorInfo(String vendor) throws RestfulException;



}
