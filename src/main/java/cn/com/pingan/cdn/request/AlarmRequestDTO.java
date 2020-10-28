package cn.com.pingan.cdn.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Classname AlarmRequestDTO
 * @Description TODO
 * @Date 2020/10/27 11:41
 * @Created by Luj
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmRequestDTO {

    private String metric;//监控指标

    private Float value;//指标值

    private String status;//告警 - 恢复

    private String desc;//描述，没有模板时，作为告警信息

    private Long timestamp;//时间戳

    private Map<String, String> tags;

    private Map<String, String> annotations;//模板注释

    private NoticeInfo notice_info;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeInfo {

        private String template;

        private String subject;

        private List<String> ways;//[weixin, pa_email, email, pa_sms]

        private List<String> phones;

        private List<String> emails;

        private WxApps wx_apps;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WxApps{

        private String corpid;

        private Integer agentid;

        private String secret;

    }

}


