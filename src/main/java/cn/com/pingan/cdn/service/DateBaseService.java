package cn.com.pingan.cdn.service;

import cn.com.pingan.cdn.repository.mysql.*;
import cn.com.pingan.cdn.repository.pgsql.DomainRepository;

/**
 * @Classname DateBaseService
 * @Description TODO
 * @Date 2020/11/22 14:33
 * @Created by Luj
 */
public interface DateBaseService {

    public ContentHistoryRepository getContentHistoryRepository();

    public ContentItemRepository getContentItemRepository();

    public VendorTaskRepository getVendorTaskRepository();

    public MergeRecordRepository getMergeRecordRepository();

    public UserLimitRepository getUserLimitRepository();

    public DomainRepository getDomainRepository();

    public LineToVendorRepository getLineToVendorRepository();

    public VendorInfoRepository getVendorInfoRepository();

    public RobinRecordRepository getRobinRecordRepository();

    public HistoryRecordRepository getHistoryRecordRepository();

    public RequestRecordRepository getRequestRecordRepository();

    //public SplitHistoryRepository getSplitHistoryRepository();


}
