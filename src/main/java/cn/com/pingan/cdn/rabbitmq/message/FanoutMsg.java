package cn.com.pingan.cdn.rabbitmq.message;

import cn.com.pingan.cdn.common.FanoutType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @Classname FanoutMsg
 * @Description TODO
 * @Date 2020/11/19 15:43
 * @Created by Luj
 */
@Data
@Builder
@AllArgsConstructor
public class FanoutMsg {
    private Long id;
    private String key;
    private FanoutType operation;

    public FanoutMsg(String key, FanoutType operation){
        this.key = key;
        this.operation = operation;
    }
    public FanoutMsg(){}

}
