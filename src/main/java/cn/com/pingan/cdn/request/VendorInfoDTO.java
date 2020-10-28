package cn.com.pingan.cdn.request;

import lombok.Data;

/**
 * @Classname VendorInfoDTO
 * @Description TODO
 * @Date 2020/10/27 17:07
 * @Created by Luj
 */
@Data
public class VendorInfoDTO {
    private String vendor;

    private int refreshQps;

    private int refreshDirQps;

    private int prefetcQps;

    private int totalQps;

    private int mergeUrlCount;

    private int mergeDirCount;

    private int mergePrefetcCount;

    private String status;//up,down
}
