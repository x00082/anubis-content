package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.repository.mysql.*;
import cn.com.pingan.cdn.repository.pgsql.DomainRepository;
import cn.com.pingan.cdn.service.DateBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Classname DateBaseServiceImpl
 * @Description TODO
 * @Date 2020/11/22 14:44
 * @Created by Luj
 */
@Service
@Slf4j
public class DateBaseServiceImpl implements DateBaseService {

    @Autowired
    private  VendorTaskRepository vendorTaskRepository;

    @Autowired
    private  VendorInfoRepository vendorInfoRepository;

    @Autowired
    private  ContentHistoryRepository contentHistoryRepository;

    @Autowired
    private  UserLimitRepository userLimitRepository;

    @Autowired
    private  DomainRepository domainRepository;

    @Autowired
    private  LineToVendorRepository lineToVendorRepository;

    @Autowired
    private  MergeRecordRepository mergeRecordRepository;

    @Autowired
    private  RobinRecordRepository robinRecordRepository;

    @Autowired
    private HistoryRecordRepository historyRecordRepository;

    @Autowired
    private RequestRecordRepository requestRecordRepository;

    @Autowired
    private SplitHistoryRepository splitHistoryRepository;

    @Autowired
    private ContentItemRepository contentItemRepository;


    @Override
    public ContentHistoryRepository getContentHistoryRepository() {
        return contentHistoryRepository;
    }

    @Override
    public ContentItemRepository getContentItemRepository(){ return contentItemRepository; }

    @Override
    public VendorTaskRepository getVendorTaskRepository() {
        return vendorTaskRepository;
    }

    @Override
    public MergeRecordRepository getMergeRecordRepository() {
        return mergeRecordRepository;
    }

    @Override
    public UserLimitRepository getUserLimitRepository() {
        return userLimitRepository;
    }

    @Override
    public DomainRepository getDomainRepository() {
        return domainRepository;
    }

    @Override
    public LineToVendorRepository getLineToVendorRepository() {
        return lineToVendorRepository;
    }

    @Override
    public VendorInfoRepository getVendorInfoRepository() {
        return vendorInfoRepository;
    }

    @Override
    public RobinRecordRepository getRobinRecordRepository() { return robinRecordRepository;}

    @Override
    public HistoryRecordRepository getHistoryRecordRepository() { return historyRecordRepository; }

    @Override
    public RequestRecordRepository getRequestRecordRepository() { return requestRecordRepository; }

    @Override
    public SplitHistoryRepository getSplitHistoryRepository() { return splitHistoryRepository; }
}
