package cn.com.pingan.cdn.request;

import cn.com.pingan.cdn.common.VendorStatusEnum;
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

    private int totalQps;

    private int totalSize;

    private int robinQps;

    private int mergeUrlCount;

    private int mergeDirCount;

    private int mergePreheatCount;

    private VendorStatusEnum status;//up,down
}
