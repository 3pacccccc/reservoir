# 《攒批处理组件-reservoir》

## 一、简介

### 1.1 概述

reservoir是一个攒批处理组件，其核心设计目标是高性能、接入简单、开箱即用、轻量级的组件，用户可以轻松接入就实现指定的攒批处理策略。

### 1.2 特性

- 1、简单：只需要简单配置，就可以实现攒批处理操作
- 2、支持多种规则，支持按个数、按时间、混合多种模式。

### 1.3 使用场景

​	   本组件适用与IO密集型操作场景，例如频繁对数据库的操作，网络文件上传，日志打印等等，使用本组件可降低IO消耗。



## 二、快速入门

### 2.1 引入pom依赖

```xml
        <dependency>
            <groupId>com.mag</groupId>
            <artifactId>reservoir</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```



### 2.2 引入配置

```java
/**
 * 按实际需要，只需要引入以下一个或者多个即可
 * @author maruimin
 * @date 2025/2/8 14:15
 */
@Configuration
public class ReservoirConfig {

    /**
     *
     * @return 按照个数攒批处理 BySizeContainer：执行容器，构造入参为达到指定个数执行释放事件。CarConditionProcess：执行释放后处理的类
     */
    @Bean(name = "bySizeFactory")
    public ReservoirFactory<Integer> bySizeFactory() {
        return new ReservoirFactory<>(new BySizeContainer<>(5), CarConditionProcess.class);
    }

    /**
     *
     * @return 按照个数攒批处理 BySizeContainer：执行容器，构造入参为达到指定个数执行释放事件。CarConditionProcess：执行释放后处理的类
     * 也可以自己自定义执行线程池，用于处理回调攒批释放后的回调任务处理。如果不指定，则使用组件自定义的线程池。
     */
    @Bean(name = "bySizeFactoryCustomThreadPool")
    public ReservoirFactory<Integer> bySizeFactoryCustomThreadPool() {
        return new ReservoirFactory<>(new BySizeContainer<>(5), CarConditionProcess.class, new ThreadPoolExecutor(
                10, 20, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024
        )));
    }

    /**
     *
     * @return 按照时间攒批处理 ByTimeContainer：执行容器，构造入参为达到指定时间执行释放事件。CarConditionProcess：执行释放后处理的类
     */
    @Bean(name = "byTimeFactory")
    public ReservoirFactory<Integer> byTimeFactory() {
        return new ReservoirFactory<>(new ByTimeContainer<>(50, TimeUnit.MILLISECONDS), CarConditionProcess.class);
    }

    /**
     *
     * @return 混合攒批处理（当达到指定的个数或者指定的时间，执行释放操作） MixContainer：执行容器，构造入参：执行释放的个数、执行释放的时间、时间单位。CarConditionProcess：执行释放后处理的类
     */
    @Bean(name = "mixFactory")
    public ReservoirFactory<Integer> mixFactory() {
        return new ReservoirFactory<>(new MixContainer<>(7, 100, TimeUnit.MILLISECONDS), CarConditionProcess.class);
    }

}

```





### 2.3 构建业务处理类

```java
import com.mag.reservoir.release.AbstractRelease;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** 释放数据处理类，在这个类里面，执行业务数据处理逻辑。需要继承AbstractRelease
 * @author maruimin
 * @date 2025/2/8 14:17
 */
@Component
public class CarConditionProcess extends AbstractRelease<Integer> {
    private final AtomicLong now = new AtomicLong(System.currentTimeMillis());

    public final CopyOnWriteArrayList<Integer> result = new CopyOnWriteArrayList<>();


    /**
     * 执行业务自己的处理逻辑
     * @param data 攒批释放后的数据
     */
    @Override
    public void release(List<Integer> data) {
        result.addAll(data);
        System.out.println(data + "---" + (System.currentTimeMillis() - now.get()));
    }
}

```

### 2.4 使用

```java

@RestController
public class TestController {

    @Resource(name = "bySizeFactory")
    private ReservoirFactory<Integer> bySizeFactory;

    /**
     * 直接往配置好的容器里面添加元素，当达到设置好的策略之后，无需做任何操作，本组件会自动释放掉容器里面的数据，并调用设置好的业务处理函数，执行对应的操作。
     */
    @GetMapping("/test")
    public String test() {
        bySizeFactory.add(1);
        return "success";
    }
}


```

