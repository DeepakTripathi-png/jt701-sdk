package com.jointech.sdk.jt701.model;

import com.jointech.sdk.jt701.base.BaseEnum;
import lombok.Getter;

/**
 * 定义事件枚举
 * @author HyoJung
 */
public enum EventTypeEnum implements BaseEnum<String> {
    LockEvent_0("关锁", "0"),
    LockEvent_1("蓝牙开锁", "1"),
    LockEvent_2("开后盖事件", "2"),
    LockEvent_3("Lora开锁", "3"),
    LockEvent_4("锁剪断", "4"),
    LockEvent_5("按键唤醒", "5"),
    LockEvent_6("定时上报", "6"),
    LockEvent_7("充电上报", "7"),
    LockEvent_8("拔出锁绳", "8"),
    LockEvent_9("RFID开锁", "9"),
    LockEvent_14("子锁信号丢失", "14"),
    LockEvent_15("阀门关闭事件", "15"),
    LockEvent_16("阀门打开事件", "16"),
    LockEvent_17("低电告警事件", "17"),
    LockEvent_18("防拆卸事件", "18"),
    LockEvent_19("拆后盖事件", "19"),
    LockEvent_20("锁绳拔出事件", "20"),
    LockEvent_21("锁绳插入事件", "21"),
    LockEvent_22("蓝牙连接唤醒事件", "22"),
    //GP系列事件
    GPEvent_0("信号监测", "10"),

    //JT系列（JT704H等）
    JTEvent_0("NFC触发", "30");

    @Getter
    private String desc;

    private String value;

    EventTypeEnum(String desc, String value) {
        this.desc = desc;
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    public static EventTypeEnum fromValue(Integer value) {
        String valueStr = String.valueOf(value);
        for (EventTypeEnum eventTypeEnum : values()) {
            if (eventTypeEnum.getValue().equals(valueStr)) {
                return eventTypeEnum;
            }
        }
        return null;
    }
}
