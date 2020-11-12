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

    private int totalQps;

    private int totalSize;

    private int mergeUrlCount;

    private int mergeDirCount;

    private int mergePreheatCount;

    private String status;//up,down
}
