package cn.com.pingan.cdn.current;

import java.util.Collection;

/**
 * @Classname MergeHandler
 * @Description TODO
 * @Date 2020/10/29 20:27
 * @Created by Luj
 */
public interface MergeHandler<V> extends Handler {
    public void merge(Collection<V> col);
}
