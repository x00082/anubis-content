package cn.com.pingan.cdn.common;

/**
 * @Classname VendorEnum
 * @Description TODO
 * @Date 2020/10/20 14:40
 * @Created by Luj
 */
public enum VendorEnum {
    qiniu("qiniu"), //七牛
    tencent("tencent"),//腾讯
    ksyun("ksyun"),//金山云
    venus("venus"),//自建CDN
    baishan("baishan"), //白山CDN
    ;

    private String code;

    VendorEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static VendorEnum getByCode(String code) {
        for (VendorEnum vendor : VendorEnum.values()) {
            if (code.equals(vendor.getCode()))
                return vendor;
        }
        return null;
    }
}
