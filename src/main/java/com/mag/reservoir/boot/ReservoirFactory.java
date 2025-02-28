package com.mag.reservoir.boot;

import com.mag.reservoir.container.IContainer;
import com.mag.reservoir.release.AbstractRelease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.*;

/**
 * @author maruimin
 * @date 2025/2/7 15:37
 */
public class ReservoirFactory<T> implements ApplicationContextAware, InitializingBean, ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ReservoirFactory.class);
    private ApplicationContext applicationContext;

    private final IContainer<T> container;


    private final Class<? extends AbstractRelease<T>> processClass;

    private final ExecutorService executors;

    public ReservoirFactory(IContainer<T> container, Class<? extends AbstractRelease<T>> processClass) {
        this.container = container;
        this.processClass = processClass;
        int cores = Runtime.getRuntime().availableProcessors();
        // 根据CPU核心数动态配置线程池参数
        int corePoolSize = cores * 2;
        int maximumPoolSize = corePoolSize * 4;
        long keepAliveTime = 60L;
        TimeUnit unit = TimeUnit.MINUTES;
        int queueCapacity = 1000000;

        // 创建阻塞队列
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(queueCapacity);

        this.executors = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                r -> new Thread(r, "reservoir execute thread pool-" + r.hashCode()),
                (r, executor) -> logger.error(">>>>>>>>>>> reservoir execute too fast, Runnable=" + r.toString())
        );
    }

    public ReservoirFactory(IContainer<T> strategy, Class<? extends AbstractRelease<T>> processClass, ExecutorService executors) {
        this.container = strategy;
        this.processClass = processClass;
        this.executors = executors;
    }

    public void add(T element) {
        this.container.add(element);
    }

    public void close() {
        this.container.close();
    }

    @Override
    public void afterPropertiesSet() {
        this.container.init(this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    public Class<? extends AbstractRelease<T>> getProcessClass() {
        return processClass;
    }

    public ExecutorService getExecutors() {
        return executors;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        this.container.close();
    }
}


