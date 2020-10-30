package cn.com.pingan.cdn.current;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Classname JxGaga
 * @Description TODO
 * @Date 2020/10/29 20:22
 * @Created by Luj
 */
public class JxGaga {
    private ListeningExecutorService service;
    private CountDownLatch latch;
    public JxGaga(ListeningExecutorService service,int workCount) {
        this.service = service;
        this.latch = new CountDownLatch(workCount);
    }
    public static JxGaga of(ExecutorService service, int workCount) {
        return new JxGaga(MoreExecutors.listeningDecorator(service),workCount);
    }

    public <T> JxGaga work(Callable<T> c, SuccessHandler<T> sh, FailureHandler fh) {
        Futures.addCallback(this.service.submit(c), new FutureCallback<T>() {
            public void onSuccess(T result) {
                try {
                    sh.onSuccess(result);
                }finally {
                    latch.countDown();
                }
            }

            public void onFailure(Throwable t) {
                try {
                    fh.onFailure(t);
                }finally {
                    latch.countDown();
                }
            }
        });
        return this;
    }




    public <V> JxGaga merge(MergeHandler<V> mh, Collection<V> col) {
        try {
            this.latch.await();
        } catch (InterruptedException e) {}
        mh.merge(col);
        return this;
    }

    public <V> JxGaga merge(MergeHandler<V> mh, Collection<V> col, long timeout, TimeUnit unit) {
        try {
            this.latch.await(timeout,unit);
        } catch (InterruptedException e) {}
        mh.merge(col);
        return this;
    }

    public void exit() {
        this.service.shutdown();
    }
}
