package cn.com.pingan.cdn.service.impl;

import cn.com.pingan.cdn.request.AlarmRequestDTO;
import cn.com.pingan.cdn.service.NotifyAlarmService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Classname NotifyAlarmServiceImpl
 * @Description TODO
 * @Date 2020/10/27 15:04
 * @Created by Luj
 */

@Service
@Slf4j
public class NotifyAlarmServiceImpl implements NotifyAlarmService {

    public static String VendorMetric = "刷新预热厂商状态";

    public static String VendorMetricStatus = "告警";


    @Override
    public int sendVendorAlarm(String msg){

        AlarmRequestDTO requestDTO = new AlarmRequestDTO();
        requestDTO.setMetric(VendorMetric);
        requestDTO.setStatus(VendorMetricStatus);
        requestDTO.setDesc(msg);
        requestDTO.setTimestamp(new Date().getTime());

        AlarmRequestDTO.NoticeInfo info =  new AlarmRequestDTO.NoticeInfo();
        List<String> ways = new ArrayList<>();
        ways.add("weixin");
        info.setWays(ways);


        AlarmRequestDTO.WxApps wxApps = new AlarmRequestDTO.WxApps();
        wxApps.setAgentid(1000002);
        wxApps.setCorpid("test");
        wxApps.setSecret("123456");
        info.setWx_apps(wxApps);

        requestDTO.setNotice_info(info);

        log.info("刷新预热厂商状态请求:{}", JSONObject.toJSONString(requestDTO));


        return 0;
    }
}
