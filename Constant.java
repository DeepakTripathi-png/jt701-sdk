package com.jointech.sdk.jt701.constants;

import java.util.Arrays;
import java.util.List;

/**
 * 常量定义
 * @author HyoJung
 * @date 20210526
 */
public class Constant {
    private Constant(){}
    /**
     * 二进制消息包头
     */
    public static final byte BINARY_MSG_HEADER = '$';

    /**
     * 文本消息包头
     */
    public static final byte TEXT_MSG_HEADER = '(';

    /**
     * 文本消息包尾
     */
    public static final byte TEXT_MSG_TAIL = ')';

    /**
     * 文本消息分隔符
     */
    public static final byte TEXT_MSG_SPLITER = ',';

    /**
     * 透传二进制数据的指令
     */
    public static final List<String> WLNET_TYPE_LIST = Arrays.asList("5", "7");
}
