package com.mag.reservoir.container;

/**
 * 按个数攒批容器，当容器内的元素达到指定的个数的时候，触发释放时间
 *
 * @author maruimin
 * @date 2025/2/7 17:06
 */
public class BySizeContainer<T> extends AbstractContainer<T> implements IContainer<T> {

    private final int maxSize;

    public BySizeContainer(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be greater than 0");
        }
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }


    @Override
    public void add(T t) {
        if (queue.size() >= maxSize) {
            super.release(maxSize);
        }
        super.add(t);
    }
}
