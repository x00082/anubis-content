package cn.com.pingan.cdn.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname TaskOperationEnum
 * @Description TODO
 * @Date 2020/10/19 16:00
 * @Created by Luj
 */
public enum TaskOperationEnum {

    content_item,//拆分

    //厂商任务
    content_qiniu,
    content_tencent,
    content_ksyun,
    content_venus,
    content_baishan,
    content_chinacache,
    content_net,
    content_jdcloud,
    content_aliyun,

    ;

    public static List<String> allTaskOperations() {
        List<String> taskOps = new ArrayList<String>();
        for (TaskOperationEnum tp : TaskOperationEnum.values()) {
            taskOps.add(tp.toString());
        }
        return taskOps;
    }

    public static TaskOperationEnum getVendorOperation(String value) {

        switch(value){
            case "qiniu":
                return TaskOperationEnum.content_qiniu;

            case "ksyun":
                return TaskOperationEnum.content_ksyun;

            case "venus":
                return TaskOperationEnum.content_venus;

            case "baishan":
                return TaskOperationEnum.content_baishan;

            case "tencent":
                return TaskOperationEnum.content_tencent;

            case "chinacache":
                return TaskOperationEnum.content_chinacache;

            case "net":
                return TaskOperationEnum.content_net;

            case "jdcloud":
                return TaskOperationEnum.content_jdcloud;

            case "aliyun":
                return TaskOperationEnum.content_aliyun;

            default:
                return TaskOperationEnum.content_venus;
        }

    }


    public static String getVendorString(TaskOperationEnum tp) {

        switch(tp){
            case content_qiniu:
                return "qiniu";

            case content_ksyun:
                return "ksyun";

            case content_venus:
                return "venus";

            case content_baishan:
                return "baishan";

            case content_tencent:
                return "tencent";

            case content_chinacache:
                return "chinacache";

            case content_net:
                return "net";

            case content_jdcloud:
                return "jdcloud";

            case content_aliyun:
                return "aliyun";

            default:
                return "venus";
        }
    }
}