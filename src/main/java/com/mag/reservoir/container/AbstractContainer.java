package com.mag.reservoir.container;

import com.mag.reservoir.boot.ReservoirFactory;
import com.mag.reservoir.release.AbstractRelease;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author maruimin
 * @date 2025/2/7 17:16
 */
public class AbstractContainer<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractContainer.class);

    protected Queue<T> queue = new MpscUnboundedArrayQueue<>(10_000);
    private ApplicationContext applicationContext;

    private Class<? extends AbstractRelease<T>> processClass;

    private ExecutorService executors;

    private final Lock lock = new ReentrantLock();

    private volatile boolean running = true;

    protected final AtomicBoolean flushInProgress = new AtomicBoolean(false);

    public void init(ReservoirFactory<T> factory) {
        this.processClass = factory.getProcessClass();
        this.applicationContext = factory.getApplicationContext();
        this.executors = factory.getExecutors();
    }

    public void add(T data) {
        if (running) {
            queue.add(data);
        }
    }

    public void close() {
        running = false;
        this.release();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void release() {
        try {
            int size = queue.size();
            int count = 0;
            List<T> dataList = new ArrayList<>();
            while (count < size) {
                T element = queue.poll();
                dataList.add(element);
                count++;
            }
            releaseExecute(dataList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            flushInProgress.set(false);
        }
    }

    public void release(int size) {
        lock.lock();
        try {
            List<T> dataList = new ArrayList<>();
            if (queue.size() >= size) {
                while (!queue.isEmpty()) {
                    dataList.add(queue.poll());
                    if (dataList.size() >= size) {
                        releaseExecute(dataList);
                        if (queue.size() < size) {
                            break;
                        } else {
                            dataList = new ArrayList<>();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("release error", e);
        } finally {
            lock.unlock();
        }
    }

    private void releaseExecute(List<T> dataList) {
        if (dataList.isEmpty()) {
            return;
        }
        String className = this.processClass.getSimpleName();
        String beanName = className.substring(0, 1).toLowerCase() + className.substring(1);
        AbstractRelease<T> bean;
        try {
            bean = applicationContext.getBean(beanName, AbstractRelease.class);
        } catch (Exception e) {
            logger.error("can not find process bean ");
            throw new IllegalArgumentException("process bean not found");
        }
        executors.execute(() -> {
            try {
                bean.release(dataList);
            } catch (Exception e) {
                logger.error("release execution error", e);
            }
        });
    }

}
