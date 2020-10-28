package cn.com.pingan.cdn.common;

/**
 * @Classname TaskStatus
 * @Description TODO
 * @Date 2020/10/20 11:21
 * @Created by Luj
 */
public enum TaskStatus {

    WAIT,

    PROCESSING,

    ROUND_ROBIN,

    SUCCESS,

    FAIL;


    public static TaskStatus of(String value) {
        for(TaskStatus ut : TaskStatus.values()) {
            if(ut.name().equals(value)) return ut;
        }
        return TaskStatus.SUCCESS;
    }
}
