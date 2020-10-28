package cn.com.pingan.cdn.service.internal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import cn.com.pingan.cdn.utils.HttpClient;

import java.util.HashMap;

/**
 * @Classname AlarmClientService
 * @Description TODO
 * @Date 2020/10/27 11:08
 * @Created by Luj
 */
public class AlarmClientService {

    @Value("${alarm.host}")
    private String alarmUrl;//地址

    //报警接收接口
    private String alarmPath = "/v1/pcm/alarm";

    //创建 查询接收人
    private String alarmReceiversPath = "/v1/pcm/alarm/receivers";
    //创建策略
    private String alarmStrategiesPath = "/v1/pcm/strategies";



    //创建接收人
//    @Async
    public void alarmReceivers(HashMap param) throws Exception {
        String res = new HttpClient().callWithPostAndRetryTimes(alarmUrl + alarmReceiversPath, JSON.toJSONString(param), 3);
    }

    //查询接收人
//    @Async
    public JSONObject getAlarmReceivers(String  param) throws Exception {
        String res = new HttpClient().callWithGet(alarmUrl + alarmReceiversPath+"?"+param);
        return JSONObject.parseObject(res);
    }

    //查询策略
//    @Async
    public JSONObject getAlarmStrategiesPath(String  param) throws Exception {
        String res = new HttpClient().callWithGet(alarmUrl + alarmStrategiesPath+"?"+param);
        return JSONObject.parseObject(res);

    }

    //删除策略
    public void deleteAlarmStrategies(String id) throws Exception{
        new HttpClient().callWithDelete(alarmUrl + alarmStrategiesPath+"/"+id);
    }

    //创建策略
//    @Async
    public void alarmStrategiesPath(HashMap param) throws Exception {
        String res = new HttpClient().callWithPostAndRetryTimes(alarmUrl + alarmStrategiesPath, JSON.toJSONString(param), 3);
    }

    //请求发送通知
//    @Async
    public JSONObject alarm(HashMap param) throws Exception {
        String res = new HttpClient().callWithPostAndRetryTimes(alarmUrl + alarmPath, JSON.toJSONString(param), 3);
        return JSONObject.parseObject(res);
    }
}
