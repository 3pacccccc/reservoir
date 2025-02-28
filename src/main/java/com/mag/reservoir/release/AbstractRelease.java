package com.mag.reservoir.release;

import java.util.List;

/**
 * @author maruimin
 * @date 2025/2/6 10:18
 */
public abstract class AbstractRelease<T> {
    public abstract void release(List<T> data);

}
