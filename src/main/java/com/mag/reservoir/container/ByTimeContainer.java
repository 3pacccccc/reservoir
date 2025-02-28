package com.mag.reservoir.container;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 按时间攒批容器，当达到指定的时间的时候，触发释放时间
 *
 * @author maruimin
 * @date 2025/2/7 17:53
 */
public class ByTimeContainer<T> extends AbstractContainer<T> implements IContainer<T> {
    ScheduledExecutorService scheduledExecutorService;

    private final int maxTime;

    public ByTimeContainer(int maxTime, TimeUnit timeUnit) {
        if (maxTime < 0) {
            throw new IllegalArgumentException("maxTime must be greater than 0");
        }
        this.maxTime = maxTime;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduledExecutorService.scheduleAtFixedRate(super::release, 0, this.maxTime, timeUnit);
    }

    public int getMaxTime() {
        return maxTime;
    }

    @Override
    public void add(T t) {
        super.add(t);
    }
}
