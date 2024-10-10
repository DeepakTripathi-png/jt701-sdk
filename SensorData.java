package com.jointech.sdk.jt701.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 从机数据
 * @author HyoJung
 * @date 20210526
 */
@Data
public class SensorData implements Serializable {
    @JSONField(name = "GpsTime")
    public String GpsTime;
    @JSONField(name = "Latitude")
    public double Latitude;
    @JSONField(name = "Longitude")
    public double Longitude;
    @JSONField(name = "LocationType")
    public int LocationType;
    @JSONField(name = "Speed")
    public int Speed;
    @JSONField(name = "Direction")
    public int Direction;
    @JSONField(name = "SensorID")
    public String SensorID;
    @JSONField(name = "LockStatus")
    public int LockStatus;
    @JSONField(name = "LockRope")
    public int LockRope;
    @JSONField(name = "LockTimes")
    public int LockTimes;
    @JSONField(name = "Index")
    public int Index;
    @JSONField(name = "Voltage")
    public String Voltage;
    @JSONField(name = "Power")
    public int Power;
    @JSONField(name = "RSSI")
    public int RSSI;
    @JSONField(name = "DateTime")
    public String DateTime;
    @JSONField(name = "SensorType")
    public int SensorType;
    @JSONField(name = "Temperature")
    public double Temperature;
    @JSONField(name = "Humidity")
    public int Humidity;
    @JSONField(name = "Event")
    public int Event = -1;
}
