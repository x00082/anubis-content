package cn.com.pingan.cdn.response;

import cn.com.pingan.cdn.common.FlowEmun;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Classname TaskDetailsResponse
 * @Description TODO
 * @Date 2020/11/13 15:02
 * @Created by Luj
 */
@Data
public class TaskDetailsResponse {

    private String requestId;

    private FlowEmun contentStatus;

    Map<String, UrlStatus> taskDetails;

    @Data
    public static class UrlStatus{
        private List<String> urls;
        private String status;
        private String message;
    }

}
