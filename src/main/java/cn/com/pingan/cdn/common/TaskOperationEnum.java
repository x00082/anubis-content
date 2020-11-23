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

    content_item,//拆分原始
    //content_item_robin,

    //content_vendor,//拆分厂商
    content_vendor_robin,
    content_vendor_success,
    content_vendor_fail,


    //厂商任务
    content_qiniu_common,
    content_qiniu_url,
    content_qiniu_dir,
    content_qiniu_preheat,
    content_qiniu_robin,

    content_tencent_common,
    content_tencent_url,
    content_tencent_dir,
    content_tencent_preheat,
    content_tencent_robin,

    content_ksyun_common,
    content_ksyun_url,
    content_ksyun_dir,
    content_ksyun_preheat,
    content_ksyun_robin,

    content_venus_common,
    content_venus_url,
    content_venus_dir,
    content_venus_preheat,
    content_venus_robin,

    content_baishan_common,
    content_baishan_url,
    content_baishan_dir,
    content_baishan_preheat,
    content_baishan_robin,

    content_chinacache_common,
    content_chinacache_url,
    content_chinacache_dir,
    content_chinacache_preheat,
    content_chinacache_robin,

    content_net_common,
    content_net_url,
    content_net_dir,
    content_net_preheat,
    content_net_robin,

    content_jdcloud_common,
    content_jdcloud_url,
    content_jdcloud_dir,
    content_jdcloud_preheat,
    content_jdcloud_robin,

    content_aliyun_common,
    content_aliyun_url,
    content_aliyun_dir,
    content_aliyun_preheat,
    content_aliyun_robin,

    default_error,

    ;

    public static List<String> allTaskOperations() {
        List<String> taskOps = new ArrayList<String>();
        for (TaskOperationEnum tp : TaskOperationEnum.values()) {
            taskOps.add(tp.toString());
        }
        return taskOps;
    }

    public static TaskOperationEnum of(String value) {
        for(TaskOperationEnum ut : TaskOperationEnum.values()) {
            if(ut.name().equals(value)) return ut;
        }
        return TaskOperationEnum.default_error;
    }

    public static TaskOperationEnum getVendorOperation(String value, RefreshType type) {


        String key = "content_" + value + "_" + type.name();
        return TaskOperationEnum.of(key);

    }

    public static TaskOperationEnum getVendorOperationCommon(String value) {


        String key = "content_" + value + "_common";
        return TaskOperationEnum.of(key);

    }

    public static TaskOperationEnum getVendorOperationRobin(String value) {


        String key = "content_" + value + "_robin";
        return TaskOperationEnum.of(key);

    }


    public static String getVendorString(TaskOperationEnum tp) {

        switch(tp){
            case content_qiniu_common:
            case content_qiniu_url:
            case content_qiniu_dir:
            case content_qiniu_preheat:
            case content_qiniu_robin:
                return "qiniu";

            case content_ksyun_common:
            case content_ksyun_url:
            case content_ksyun_dir:
            case content_ksyun_preheat:
            case content_ksyun_robin:
                return "ksyun";

            case content_venus_common:
            case content_venus_url:
            case content_venus_dir:
            case content_venus_preheat:
            case content_venus_robin:
                return "venus";

            case content_baishan_common:
            case content_baishan_url:
            case content_baishan_dir:
            case content_baishan_preheat:
            case content_baishan_robin:
                return "baishan";

            case content_tencent_common:
            case content_tencent_url:
            case content_tencent_dir:
            case content_tencent_preheat:
            case content_tencent_robin:
                return "tencent";

            case content_chinacache_common:
            case content_chinacache_url:
            case content_chinacache_dir:
            case content_chinacache_preheat:
            case content_chinacache_robin:
                return "chinacache";

            case content_net_common:
            case content_net_url:
            case content_net_dir:
            case content_net_preheat:
            case content_net_robin:
                return "net";

            case content_jdcloud_common:
            case content_jdcloud_url:
            case content_jdcloud_dir:
            case content_jdcloud_preheat:
            case content_jdcloud_robin:
                return "jdcloud";

            case content_aliyun_common:
            case content_aliyun_url:
            case content_aliyun_dir:
            case content_aliyun_preheat:
            case content_aliyun_robin:
                return "aliyun";

            default:
                return "venus";
        }
    }
}