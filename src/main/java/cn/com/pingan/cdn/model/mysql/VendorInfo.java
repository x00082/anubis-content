package cn.com.pingan.cdn.model.mysql;

import cn.com.pingan.cdn.common.VendorStatusEnum;
import cn.com.pingan.cdn.request.VendorInfoDTO;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity

@Table(name="vendor_info", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"vendor"})
        },
        indexes = {
                @Index(columnList = "status")
        })

/*
@Table(name="vendor_info",
        indexes = {
                @Index(columnList = "vendor"),
                @Index(columnList = "status")
        })
*/
public class VendorInfo {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="vendor", nullable = false)
    private String vendor;

    @Column(name = "total_qps", nullable = false)
    private int totalQps;

    @Column(name = "total_size")
    private int totalSize;

    @Column(name = "merge_url_count")
    private int mergeUrlCount;

    @Column(name = "merge_dir_count")
    private int mergeDirCount;

    @Column(name = "merge_preheat_count")
    private int mergePreheatCount;

    @Column(name = "status", length=16, nullable = false)
    @Enumerated(EnumType.STRING)
    private VendorStatusEnum status;//up,down

    public void setVendorInfo(VendorInfoDTO infoDTO){

        this.vendor = infoDTO.getVendor();
        this.totalQps = infoDTO.getTotalQps();
        this.totalSize = infoDTO.getTotalSize();
        this.mergeUrlCount = infoDTO.getMergeUrlCount();
        this.mergeDirCount = infoDTO.getMergeDirCount();
        this.mergePreheatCount = infoDTO.getMergePreheatCount();
        this.status = infoDTO.getStatus();
    }

    public VendorInfoDTO getVendorInfo(){
        VendorInfoDTO infoDTO = new VendorInfoDTO();
        infoDTO.setVendor(vendor);
        infoDTO.setTotalQps(totalQps);
        infoDTO.setTotalSize(totalSize);
        infoDTO.setMergeUrlCount(mergeUrlCount);
        infoDTO.setMergeDirCount(mergeDirCount);
        infoDTO.setMergePreheatCount(mergePreheatCount);
        infoDTO.setStatus(status);
        return infoDTO;
    }

}
