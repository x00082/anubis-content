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

    public static final String CONTENT_DELAY_EXCHANGE="anubisContentDelayExchange";//延时
    public static final String DELAYED_EXCHANGE_TYPE="x-delayed-message";
    public static final String CONTENT_DELAY_QUEUE="anubisContentDelayQueue";
    public static final String CONTENT_DELAY_ROUTINE_KEY="anubisContentDelayRoutineKey";


    public static final String CONTENT_MESSAGE_EXCHANGE="anubisContentExchange";//正常

    public static final String CONTENT_MESSAGE_ITEM="content_item";
    public static final String CONTENT_MESSAGE_ITEM_ROBIN="content_item_robin";

    public static final String CONTENT_MESSAGE_VENDOR="content_vendor";
    public static final String CONTENT_MESSAGE_VENDOR_ROBIN="content_vendor_robin";

    //厂商队列
    public static final String CONTENT_VENDOR_QINIU = "content_qiniu";
    public static final String CONTENT_VENDOR_QINIU_ROBIN = "content_qiniu_robin";


    public static final String CONTENT_VENDOR_TENCENT = "content_tencent";
    public static final String CONTENT_VENDOR_TENCENT_ROBIN = "content_tencent_robin";


    public static final String CONTENT_VENDOR_KSYUN = "content_ksyun";
    public static final String CONTENT_VENDOR_KSYUN_ROBIN = "content_ksyun_robin";


    public static final String CONTENT_VENDOR_VENUS = "content_venus";
    public static final String CONTENT_VENDOR_VENUS_ROBIN = "content_venus_robin";


    public static final String CONTENT_VENDOR_BAISHAN = "content_baishan";
    public static final String CONTENT_VENDOR_BAISHAN_ROBIN = "content_baishan_robin";


    public static final String CONTENT_VENDOR_CHINACHE = "content_chinacache";
    public static final String CONTENT_VENDOR_CHINACHE_ROBIN = "content_chinacache_robin";


    public static final String CONTENT_VENDOR_NET = "content_net";
    public static final String CONTENT_VENDOR_NET_ROBIN = "content_net_robin";


    public static final String CONTENT_VENDOR_JDCLOUD = "content_jdcloud";
    public static final String CONTENT_VENDOR_JDCLOUD_ROBIN = "content_jdcloud_robin";


    public static final String CONTENT_VENDOR_ALIYUN = "content_aliyun";
    public static final String CONTENT_VENDOR_ALIYUN_ROBIN = "content_aliyun_robin";

    public static final String DEFAULT_ERROR = "default_error";



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

}
