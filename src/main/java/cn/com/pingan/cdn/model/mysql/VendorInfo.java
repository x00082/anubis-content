package cn.com.pingan.cdn.model.mysql;

import cn.com.pingan.cdn.request.VendorInfoDTO;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="vendor_info",schema="test", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"vendor"})
        },
        indexes = {
                @Index(columnList = "status")
        })
public class VendorInfo {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="vendor")
    private String vendor;

    @Column(name = "refresh_url_qps")
    private int refreshQps;

    @Column(name = "refresh_dir_qps")
    private int refreshDirQps;

    @Column(name = "prefetc_qps")
    private int prefetcQps;

    @Column(name = "total_qps")
    private int totalQps;


    @Column(name = "merge_url_count")
    private int mergeUrlCount;

    @Column(name = "merge_dir_count")
    private int mergeDirCount;

    @Column(name = "merge_prefetc_count")
    private int mergePrefetcCount;

    @Column(name = "status", length=16)
    private String status;//up,down

    public void setVendorInfo(VendorInfoDTO infoDTO){
        this.refreshQps = infoDTO.getRefreshQps();
        this.refreshDirQps = infoDTO.getRefreshDirQps();
        this.prefetcQps = infoDTO.getPrefetcQps();
        this.totalQps = infoDTO.getTotalQps();
        this.mergeUrlCount = infoDTO.getMergeUrlCount();
        this.mergeDirCount = infoDTO.getMergeDirCount();
        this.mergePrefetcCount = infoDTO.getPrefetcQps();
        this.status = infoDTO.getStatus();
    }

    public VendorInfoDTO getVendorInfo(){
        VendorInfoDTO infoDTO = new VendorInfoDTO();
        infoDTO.setVendor(vendor);
        infoDTO.setRefreshQps(refreshQps);
        infoDTO.setRefreshDirQps(refreshDirQps);
        infoDTO.setPrefetcQps(prefetcQps);
        infoDTO.setTotalQps(totalQps);
        infoDTO.setMergeUrlCount(mergeUrlCount);
        infoDTO.setMergeDirCount(mergeDirCount);
        infoDTO.setMergePrefetcCount(mergePrefetcCount);
        infoDTO.setStatus(status);
        return infoDTO;
    }

}
