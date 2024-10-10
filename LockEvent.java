package com.jointech.sdk.jt701.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>Description: 锁事件类</p>
 *
 * @author lenny
 * @version 1.0.1
 * @date 20210328
 */
@Data
public class LockEvent implements Serializable {
    /**
     * 事件产生时间
     */
    @JSONField(name = "DateTime")
    public String DateTime;
    /**
     * 纬度
     */
    @JSONField(name = "Latitude")
    public double Latitude;
    /**
     * 经度
     */
    @JSONField(name = "Longitude")
    public double Longitude;
    /**
     * 定位方式
     */
    @JSONField(name = "LocationType")
    public int LocationType;
    /**
     * 速度
     */
    @JSONField(name = "Speed")
    public int Speed;
    /**
     * 方向
     */
    @JSONField(name = "Direction")
    public int Direction;
    /**
     * 事件类型
     */
    @JSONField(name = "Event")
    public int Event;
    /**
     * 开锁验证
     */
    @JSONField(name = "Status")
    public int Status;
    /**
     * 事件关联的围栏
     */
    @JSONField(name = "UnlockFenceID")
    public int UnlockFenceID = -1;
    /**
     * RFID卡
     */
    @JSONField(name = "RFIDNo")
    public String RFIDNo;
    /**
     * 开锁密码错误次数
     */
    @JSONField(name = "PsdErrorTimes")
    public int PsdErrorTimes;
    /**
     * 数据流水号
     */
    @JSONField(name = "Index")
    public int Index;
    /**
     * 里程值
     */
    @JSONField(name = "Mileage")
    public long Mileage;
}
