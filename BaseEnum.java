package com.jointech.sdk.jt701.base;

import java.io.Serializable;

/**
 * 基础枚举
 * @author HyoJung
 */
public interface BaseEnum <T> extends Serializable {
    T getValue();
}
