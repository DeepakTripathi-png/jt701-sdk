package com.jointech.sdk.jt701.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.io.Serializable;

/**
 * <p>Description: 定位实体类</p>
 *
 * @author lenny
 * @version 1.0.1
 * @date 20210328
 */
@Data
public class LocationData implements Serializable {
    /**
     * 协议版本号
     */
    @JSONField(name = "ProtocolType")
    public int ProtocolType;
    /**
     * 终端类型号
     */
    @JSONField(name = "DeviceType")
    public int DeviceType;
    /**
     * 数据类型号
     */
    @JSONField(name = "DataType")
    public int DataType;
    /**
     * 数据长度
     */
    @JSONField(name = "DataLength")
    public int DataLength;
    /**
     * 定位时间
     */
    @JSONField(name = "GpsTime")
    public String GpsTime;
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
     * 里程
     */
    @JSONField(name = "Mileage")
    public long Mileage;
    /**
     * GPS信号值
     */
    @JSONField(name = "GpsSignal")
    public int GpsSignal;
    /**
     * GSM信号质量
     */
    @JSONField(name = "GSMSignal")
    public int GSMSignal;
    /**
     * 报警类型
     */
    @JSONField(name = "Alarm")
    public int Alarm = -1;
    /**
     * 区域报警ID
     */
    @JSONField(name = "AlarmArea")
    public int AlarmArea;
    /**
     * 电量值
     */
    @JSONField(name = "Battery")
    public int Battery;
    /**
     * 电机状态
     */
    @JSONField(name = "LockStatus")
    public int LockStatus;
    /**
     * 锁绳状态
     */
    @JSONField(name = "LockRope")
    public int LockRope;
    /**
     * 后盖状态
     */
    @JSONField(name = "BackCover")
    public int BackCover;
    /**
     * MCC
     */
    @JSONField(name = "MCC")
    public int MCC;
    /**
     * MNC
     */
    @JSONField(name = "MNC")
    public int MNC;
    /**
     * LAC
     */
    @JSONField(name = "LAC")
    public int LAC;
    /**
     * CELLID
     */
    @JSONField(name = "CELLID")
    public long CELLID;
    /**
     * IMEI
     */
    @JSONField(name = "IMEI")
    public String IMEI;
    /**
     * Awaken
     */
    @JSONField(name = "Awaken")
    public int Awaken;
    /**
     * 流水号
     */
    @JSONField(name = "Index")
    public int Index;
}
