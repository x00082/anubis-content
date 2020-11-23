/**   
 * @Project: anubis-content
 * @File: Constants.java 
 * @Package cn.com.pingan.cdn.rabbitmq.constants 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 上午10:34:14 
 */
package cn.com.pingan.cdn.rabbitmq.constants;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @ClassName: Constants 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 上午10:34:14 
 *  
 */
@Data
@NoArgsConstructor
public class Constants {

    public static final int SUCCESS = 0;
    public static final int FAILED = 1;
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_WAIT = "WAIT";
    public static final String STATUS_FAIL = "FAIL";

    public static final String QPS_LIMIT = "QPSLIMITED";
    
    //

    public static final String CONTENT_FANOUT_EXCHANGE = "anubisContentFanoutExchange";



    public static final String CONTENT_DELAY_EXCHANGE="anubisContentDelayExchange";//延时
    public static final String DELAYED_EXCHANGE_TYPE="x-delayed-message";
    public static final String CONTENT_DELAY_QUEUE="anubisContentDelayQueue";
    public static final String CONTENT_DELAY_ROUTINE_KEY="anubisContentDelayRoutineKey";


    public static final String CONTENT_MESSAGE_EXCHANGE="anubisContentExchange";//正常

    public static final String CONTENT_MESSAGE_ITEM="content_item";
    //public static final String CONTENT_MESSAGE_ITEM_ROBIN="content_item_robin";

    //public static final String CONTENT_MESSAGE_VENDOR="content_vendor";
    public static final String CONTENT_MESSAGE_VENDOR_ROBIN="content_vendor_robin";
    public static final String CONTENT_MESSAGE_VENDOR_SUCCESS="content_vendor_success";
    public static final String CONTENT_MESSAGE_VENDOR_FAIL="content_vendor_fail";


    //厂商队列
    public static final String CONTENT_VENDOR_QINIU_COMMON = "content_qiniu_common";
    public static final String CONTENT_VENDOR_QINIU_URL = "content_qiniu_url";
    public static final String CONTENT_VENDOR_QINIU_DIR = "content_qiniu_dir";
    public static final String CONTENT_VENDOR_QINIU_PREHEAT = "content_qiniu_preheat";
    public static final String CONTENT_VENDOR_QINIU_ROBIN = "content_qiniu_robin";


    public static final String CONTENT_VENDOR_TENCENT_COMMON = "content_tencent_common";
    public static final String CONTENT_VENDOR_TENCENT_URL = "content_tencent_url";
    public static final String CONTENT_VENDOR_TENCENT_DIR = "content_tencent_dir";
    public static final String CONTENT_VENDOR_TENCENT_PREHEAT = "content_tencent_preheat";
    public static final String CONTENT_VENDOR_TENCENT_ROBIN = "content_tencent_robin";


    public static final String CONTENT_VENDOR_KSYUN_COMMON = "content_ksyun_common";
    public static final String CONTENT_VENDOR_KSYUN_URL = "content_ksyun_url";
    public static final String CONTENT_VENDOR_KSYUN_DIR = "content_ksyun_dir";
    public static final String CONTENT_VENDOR_KSYUN_PREHEAT = "content_ksyun_preheat";
    public static final String CONTENT_VENDOR_KSYUN_ROBIN = "content_ksyun_robin";


    public static final String CONTENT_VENDOR_VENUS_COMMON = "content_venus_common";
    public static final String CONTENT_VENDOR_VENUS_URL = "content_venus_url";
    public static final String CONTENT_VENDOR_VENUS_DIR = "content_venus_dir";
    public static final String CONTENT_VENDOR_VENUS_PREHEAT = "content_venus_preheat";
    public static final String CONTENT_VENDOR_VENUS_ROBIN = "content_venus_robin";


    public static final String CONTENT_VENDOR_BAISHAN_COMMON = "content_baishan_common";
    public static final String CONTENT_VENDOR_BAISHAN_URL = "content_baishan_url";
    public static final String CONTENT_VENDOR_BAISHAN_DIR = "content_baishan_dir";
    public static final String CONTENT_VENDOR_BAISHAN_PREHEAT = "content_baishan_preheat";
    public static final String CONTENT_VENDOR_BAISHAN_ROBIN = "content_baishan_robin";


    public static final String CONTENT_VENDOR_CHINACHE_COMMON = "content_chinacache_common";
    public static final String CONTENT_VENDOR_CHINACHE_URL = "content_chinacache_url";
    public static final String CONTENT_VENDOR_CHINACHE_DIR = "content_chinacache_dir";
    public static final String CONTENT_VENDOR_CHINACHE_PREHEAT = "content_chinacache_preheat";
    public static final String CONTENT_VENDOR_CHINACHE_ROBIN = "content_chinacache_robin";


    public static final String CONTENT_VENDOR_NET_COMMON = "content_net_common";
    public static final String CONTENT_VENDOR_NET_URL = "content_net_url";
    public static final String CONTENT_VENDOR_NET_DIR = "content_net_dir";
    public static final String CONTENT_VENDOR_NET_PREHEAT = "content_net_preheat";
    public static final String CONTENT_VENDOR_NET_ROBIN = "content_net_robin";


    public static final String CONTENT_VENDOR_JDCLOUD_COMMON = "content_jdcloud_common";
    public static final String CONTENT_VENDOR_JDCLOUD_URL = "content_jdcloud_url";
    public static final String CONTENT_VENDOR_JDCLOUD_DIR = "content_jdcloud_dir";
    public static final String CONTENT_VENDOR_JDCLOUD_PREHEAT = "content_jdcloud_preheat";
    public static final String CONTENT_VENDOR_JDCLOUD_ROBIN = "content_jdcloud_robin";


    public static final String CONTENT_VENDOR_ALIYUN_COMMON = "content_aliyun_common";
    public static final String CONTENT_VENDOR_ALIYUN_URL = "content_aliyun_url";
    public static final String CONTENT_VENDOR_ALIYUN_DIR = "content_aliyun_dir";
    public static final String CONTENT_VENDOR_ALIYUN_PREHEAT = "content_aliyun_preheat";
    public static final String CONTENT_VENDOR_ALIYUN_ROBIN = "content_aliyun_robin";

    public static final String DEFAULT_ERROR = "default_error";


/*
    public static final String[] VENDOR_QUEUE = {
                                                CONTENT_VENDOR_QINIU,
                                                CONTENT_VENDOR_TENCENT,
                                                CONTENT_VENDOR_KSYUN,
                                                CONTENT_VENDOR_VENUS,
                                                CONTENT_VENDOR_BAISHAN,
                                                CONTENT_VENDOR_CHINACHE,
                                                CONTENT_VENDOR_NET,
                                                CONTENT_VENDOR_JDCLOUD,
                                                CONTENT_VENDOR_ALIYUN
                                                };
*/
}
