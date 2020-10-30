package cn.com.pingan.cdn.current;

/**
 * @Classname FailureHandler
 * @Description TODO
 * @Date 2020/10/29 20:27
 * @Created by Luj
 */
@FunctionalInterface
public interface FailureHandler extends Handler {
    public void onFailure(Throwable t);
}
