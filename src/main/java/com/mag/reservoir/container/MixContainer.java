package com.mag.reservoir.container;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 混合容器，当达到指定个数或者指定的时间的时候，触发释放操作
 *
 * @author maruimin
 * @date 2025/2/7 17:55
 */
public class MixContainer<T> extends AbstractContainer<T> implements IContainer<T> {

    // 达到触发的个数
    private final int maxSize;

    // 达到触发的时间
    private final int maxTime;

    // 出发时间单位
    private final TimeUnit timeUnit;

    // 执行业务释放的线程池
    ScheduledExecutorService scheduledExecutorService;


    public MixContainer(int maxSize, int maxTime, TimeUnit timeUnit) {
        if (maxSize <= 0 || maxTime <= 0 || timeUnit == null) {
            throw new IllegalArgumentException("maxSize and maxTime must be greater than 0 and timeUnit can not be null");
        }
        this.maxTime = maxTime;
        this.timeUnit = timeUnit;
        this.maxSize = maxSize;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduledExecutorService.scheduleAtFixedRate(super::release, this.maxTime, this.maxTime, this.timeUnit);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public void add(T t) {
        if (queue.size() >= this.maxSize) {
            super.release(this.maxSize);
        }
        super.add(t);
    }
}
