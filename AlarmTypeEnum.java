package com.jointech.sdk.jt701.model;

import com.jointech.sdk.jt701.base.BaseEnum;
import lombok.Getter;

/**
 * 终端报警类型
 * @author HyoJung
 */
public enum AlarmTypeEnum implements BaseEnum<String> {
    LOCK_ALARM_1("锁绳剪断", "1"),
    LOCK_ALARM_2("震动", "2"),
    LOCK_ALARM_3("长时间开锁", "3"),
    LOCK_ALARM_4("开锁密码连续5次错误", "4"),
    LOCK_ALARM_5("刷非法卡", "5"),
    LOCK_ALARM_6("低电量", "6"),
    LOCK_ALARM_7("开后盖", "7"),
    LOCK_ALARM_8("卡锁", "8"),
    LOCK_ALARM_9("进区域报警", "9"),
    LOCK_ALARM_10("出区域报警", "10");

    @Getter
    private String desc;

    private String value;

    AlarmTypeEnum(String desc, String value) {
        this.desc = desc;
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    public static AlarmTypeEnum fromValue(Integer value) {
        String valueStr = String.valueOf(value);
        for (AlarmTypeEnum alarmTypeEnum : values()) {
            if (alarmTypeEnum.getValue().equals(valueStr)) {
                return alarmTypeEnum;
            }
        }
        return null;
    }
}
