package cn.com.pingan.cdn.request;

import cn.com.pingan.cdn.common.HisStatus;
import cn.com.pingan.cdn.common.RefreshType;
import lombok.Data;

import java.util.List;

/**
 * @Classname QueryHisDTO
 * @Description TODO
 * @Date 2021/01/25 11:37
 * @Created by Luj
 */
@Data
public class QueryHisCountDTO {
    private List<HisCount> detailsCount;

    @Data
    public static class HisCount{
        private RefreshType type;
        private long successCount;
        private long failCount;
        private long waitCount;

        public HisCount(RefreshType type){
            this.type = type;
            this.successCount = 0;
            this.failCount = 0;
            this.waitCount = 0;
        }

    }

    @Data
    public static class HisCountResult{
        private RefreshType type;
        private HisStatus status;
        private long count;
    }


}
