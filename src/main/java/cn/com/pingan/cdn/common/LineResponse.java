/**   
 * @Project: anubis-content
 * @File: LineResponse.java 
 * @Package cn.com.pingan.cdn.common 
 * @Description: TODO() 
 * @author lujun  
 * @date 2020年10月16日 下午2:01:28 
 */
package cn.com.pingan.cdn.common;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** 
 * @ClassName: LineResponse 
 * @Description: TODO() 
 * @author lujun
 * @date 2020年10月16日 下午2:01:28 
 *  
 */
@Data
public class LineResponse extends ApiResponse {
    private List<LineDetail> data;

    @Data
    public static class LineDetail extends LineInfo {
        private Map<String, LineInfo> baseLines;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineInfo {
        private String id;
        private String vendor;
        private String platform;
        private Boolean isDefault;
        private String name;// 线路名称
        private String type; // 线路类型，基础或融合
        private String cname;// 线路CNAME
        private String[] baseLineIds; // 融合线路包含的基础线路
        private String vendorId;// 线路所属的厂商id;
        private String platformId; // 线路支持的业务平台id
        private String[] featureIds; // 线路包含的特征id
        private Map<String, Feature> features; // 线路特征详情
        private int fingerprint;// 线路fingerprint
        private String status;
        private String zone;
        private boolean unifiedCname;// 是否有统一CNAME
        private String comment;// 线路备注
        private int grade;// 基础线路等级
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private String name;
    }
    
    public enum LineStatus {
        active,
        ;
    }

    public enum LineType {
        base,
        mix,
        ;
    }
}
