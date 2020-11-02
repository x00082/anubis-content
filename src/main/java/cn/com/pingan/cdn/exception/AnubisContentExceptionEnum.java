package cn.com.pingan.cdn.exception;

/**
 * @Classname AnubisCdnExceptionEnum
 * @Description TODO
 * @Date 2020/10/21 19:16
 * @Created by Luj
 */
public enum AnubisContentExceptionEnum {
    Content_Internal_Error(50000, "internal error."),
    Vendor_List_Empty(50002,"vendor list is empty"),
    Task_Not_Exist(50003,"task not exist"),
    ;
    private int code;

    private String message;

    AnubisContentExceptionEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
