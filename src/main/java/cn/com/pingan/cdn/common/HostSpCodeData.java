package cn.com.pingan.cdn.common;

import lombok.Data;

/**
 * @Classname TaskItem
 * @Description TODO
 * @Date 2020/11/21 21:30
 * @Created by Luj
 */
@Data
public class HostSpCodeData {
    private String host;
    private String userCode;

    public HostSpCodeData(String host, String userCode){
        this.host = host;
        this.userCode = userCode;
    }
}
