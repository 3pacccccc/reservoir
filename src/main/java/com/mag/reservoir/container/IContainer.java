package com.mag.reservoir.container;

import com.mag.reservoir.boot.ReservoirFactory;

/**
 * @author maruimin
 * @date 2025/2/7 15:50
 */
public interface IContainer<T> {

    /**
     * 添加元素
     *
     * @param t 元素类
     */
    void add(T t);

    /**
     * 初始化
     * @param factory 工厂容器
     */
    void init(ReservoirFactory<T> factory);

    /**
     * 关闭容器
     */
    void close();
}
