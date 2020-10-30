package cn.com.pingan.cdn.current;

/**
 * @Classname SuccessHandler
 * @Description TODO
 * @Date 2020/10/29 20:23
 * @Created by Luj
 */
@FunctionalInterface
public interface SuccessHandler<T> extends Handler{
    public void onSuccess(T t);
}
