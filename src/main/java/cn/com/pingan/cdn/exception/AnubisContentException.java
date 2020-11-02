package cn.com.pingan.cdn.exception;

import lombok.Data;

/**
 * @Classname AnubisContentException
 * @Description TODO
 * @Date 2020/10/21 19:15
 * @Created by Luj
 */
@Data
public class AnubisContentException extends Exception {
    private int code;

    private String message;

    public AnubisContentException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public AnubisContentException(AnubisContentExceptionEnum exception) {

        this.code = exception.getCode();
        this.message= exception.getMessage();
    }
}
