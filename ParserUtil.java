package com.jointech.sdk.jt701.utils;

import com.jointech.sdk.jt701.constants.Constant;
import com.jointech.sdk.jt701.model.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>Description: 解析方法工具类</p>
 * @author HyoJung
 * @date 20210526
 */
public class ParserUtil {
    private ParserUtil()
    {}

    /**
     * 解析指令数据
     * @param in 原始数据
     * @return
     */
    public static Result decodeTextMessage(ByteBuf in)
    {
        //定义定位数据实体类
        Result model = new Result();
        //包头(
        in.readByte();
        //字段列表
        List<String> itemList = new ArrayList<String>();
        //透传的二进制数据
        ByteBuf msgBody = null;
        while (in.readableBytes() > 0) {
            //无线网关数据上传(WLNET5、WLNET7)第7个字段以及后面为二进制数据
            if (itemList.size() >= 6 && Objects.equals("WLNET", itemList.get(3)) && Constant.WLNET_TYPE_LIST.contains(itemList.get(4))) {
                //到结尾")"前的长度
                int lastItemLen = in.readableBytes() - 1;
                //反转义
                msgBody = Unpooled.buffer(lastItemLen);
                CommonUtil.unescape(in, msgBody, lastItemLen);
                in.readByte();
            } else {
                //查询逗号的下标截取数据
                int index = in.bytesBefore(Constant.TEXT_MSG_SPLITER);
                int itemLen = index > 0 ? index : in.readableBytes() - 1;
                byte[] byteArr = new byte[itemLen];
                in.readBytes(byteArr);
                in.readByte();
                itemList.add(new String(byteArr));
            }
        }
        //WLNET消息类型为组合
        String msgType = itemList.get(1);
        if (itemList.size() >= 5 && (Objects.equals("WLNET", itemList.get(3))||Objects.equals("OTA", itemList.get(3)))) {
            msgType = itemList.get(3) + itemList.get(4);
        }
        Object dataBody=null;
        if(msgType.equals("WLNET5")) {
            SensorData sensorData=parseWlnet5(msgBody);
            dataBody=sensorData;
            model.setReplyMsg(replyMessage(msgType,sensorData.getIndex()));
        }else if(msgType.equals("P45")) {
            dataBody=parseP45(itemList);
            model.setReplyMsg(replyMessage(msgType,itemList));
        }else {
            if(itemList.size()>0)
            {
                dataBody="(";
                for(String item :itemList) {
                    dataBody+=item+",";
                }
                dataBody=CommonUtil.trimEnd(dataBody.toString(),",");
                dataBody += ")";
            }
        }
        model.setDeviceID(itemList.get(0));
        model.setMsgType(msgType);
        model.setDataBody(dataBody);
        return model;
    }

    /**
     * 解析从机数据
     * @param byteBuf
     * @return
     */
    private static SensorData parseWlnet5(ByteBuf byteBuf) {
        SensorData sensorData = new SensorData();
        //定位时间
        byte[] timeArr = new byte[6];
        byteBuf.readBytes(timeArr);
        String bcdTimeStr = ByteBufUtil.hexDump(timeArr);
        ZonedDateTime gpsZonedDateTime = parseBcdTime(bcdTimeStr);
        //纬度
        byte[] latArr = new byte[4];
        byteBuf.readBytes(latArr);
        String latHexStr = ByteBufUtil.hexDump(latArr);
        BigDecimal latFloat = new BigDecimal(latHexStr.substring(2, 4) + "." + latHexStr.substring(4)).divide(new BigDecimal("60"), 6, RoundingMode.HALF_UP);
        double lat = new BigDecimal(latHexStr.substring(0, 2)).add(latFloat).doubleValue();
        //经度
        byte[] lngArr = new byte[5];
        byteBuf.readBytes(lngArr);
        String lngHexStr = ByteBufUtil.hexDump(lngArr);
        BigDecimal lngFloat = new BigDecimal(lngHexStr.substring(3, 5) + "." + lngHexStr.substring(5, 9)).divide(new BigDecimal("60"), 6, RoundingMode.HALF_UP);
        double lng = new BigDecimal(lngHexStr.substring(0, 3)).add(lngFloat).doubleValue();
        //位指示
        int bitFlag = Byte.parseByte(lngHexStr.substring(9, 10), 16);
        //定位状态
        int locationType = (bitFlag & 0x01) > 0 ? 1 : 0;
        //北纬、南纬
        if ((bitFlag & 0b0010) == 0) {
            lat = -lat;
        }
        //东经、西经
        if ((bitFlag & 0b0100) == 0) {
            lng = -lng;
        }
        //速度
        int speed = (int) (byteBuf.readUnsignedByte() * 1.85);
        //方向
        int direction = byteBuf.readUnsignedByte() * 2;

        //从机时间
        byte[] slaveMachineTimeArr = new byte[6];
        byteBuf.readBytes(slaveMachineTimeArr);
        String slaveMachineBcdTimeStr = ByteBufUtil.hexDump(slaveMachineTimeArr);
        ZonedDateTime slaveMachineZonedDateTime = parseBcdTime(slaveMachineBcdTimeStr);
        //从机ID
        byte[] slaveMachineIdArr = new byte[5];
        byteBuf.readBytes(slaveMachineIdArr);
        String slaveMachineId = ByteBufUtil.hexDump(slaveMachineIdArr).toUpperCase();
        //从机数据流水号
        int flowId = byteBuf.readUnsignedByte();
        //从机电压
        String voltage = new BigDecimal(byteBuf.readUnsignedShort()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP).toString();
        //从机电量
        int power = byteBuf.readUnsignedByte();
        //RSSI
        int rssi = byteBuf.readUnsignedByte();
        //传感器类型
        int sensorType = byteBuf.readUnsignedByte();
        //温度值
        double temperature = -1000.0;
        //湿度值
        int humidity = 0;
        //事件类型
        int eventType = -1;
        //设备状态
        int terminalStatus = -1;
        //开关锁次数
        int lockTimes = -1;
        if (sensorType == 1) {
            //温度
            temperature = parseTemperature(byteBuf.readShort());
            //湿度
            humidity = byteBuf.readUnsignedByte();
            //网关保存数据条数
            int itemCount = byteBuf.readUnsignedShort();
            //网关状态
            int gatewayStatus = byteBuf.readUnsignedByte();
        } else if (sensorType == 4) {
            //事件
            int event = byteBuf.readUnsignedShort();
            //判断事件
            if (NumberUtil.getBitValue(event, 0) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_0.getValue());
            } else if (NumberUtil.getBitValue(event, 1) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_1.getValue());
            } else if (NumberUtil.getBitValue(event, 2) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_2.getValue());
            } else if (NumberUtil.getBitValue(event, 3) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_3.getValue());
            } else if (NumberUtil.getBitValue(event, 4) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_4.getValue());
            } else if (NumberUtil.getBitValue(event, 5) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_5.getValue());
            } else if (NumberUtil.getBitValue(event, 6) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_6.getValue());
            } else if (NumberUtil.getBitValue(event, 7) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_7.getValue());
            } else if (NumberUtil.getBitValue(event, 8) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_8.getValue());
            } else if (NumberUtil.getBitValue(event, 9) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_9.getValue());
            }else if (NumberUtil.getBitValue(event, 14) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_14.getValue());
            }
            //设备状态
            terminalStatus = byteBuf.readUnsignedShort();
            //开关锁次数
            lockTimes = byteBuf.readUnsignedShort();
            //网关状态
            int gatewayStatus = byteBuf.readUnsignedByte();
        }else if (sensorType == 5||sensorType == 6)
        {
            //事件
            int event = byteBuf.readUnsignedShort();
            //判断事件
            if (NumberUtil.getBitValue(event, 0) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_15.getValue());
            } else if (NumberUtil.getBitValue(event, 1) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_16.getValue());
            } else if (NumberUtil.getBitValue(event, 2) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_17.getValue());
            } else if (NumberUtil.getBitValue(event, 3) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_6.getValue());
            } else if (NumberUtil.getBitValue(event, 4) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_18.getValue());
            } else if (NumberUtil.getBitValue(event, 5) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_19.getValue());
            } else if (NumberUtil.getBitValue(event, 6) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_3.getValue());
            } else if (NumberUtil.getBitValue(event, 7) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_0.getValue());
            } else if (NumberUtil.getBitValue(event, 8) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_1.getValue());
            } else if (NumberUtil.getBitValue(event, 9) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_20.getValue());
            }else if (NumberUtil.getBitValue(event, 10) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_21.getValue());
            }else if (NumberUtil.getBitValue(event, 11) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_22.getValue());
            }else if (NumberUtil.getBitValue(event, 12) == 1) {
                eventType = Integer.parseInt(EventTypeEnum.LockEvent_5.getValue());
            }
            //设备状态
            terminalStatus = byteBuf.readUnsignedShort();
            //开关锁次数
            lockTimes = byteBuf.readUnsignedShort();
            //网关状态
            int gatewayStatus = byteBuf.readUnsignedByte();
        }
        sensorData.setGpsTime(gpsZonedDateTime.toString());
        sensorData.setLatitude(lat);
        sensorData.setLongitude(lng);
        sensorData.setLocationType(locationType);
        sensorData.setSpeed(speed);
        sensorData.setDirection(direction);
        sensorData.setSensorID(slaveMachineId);
        sensorData.setLockStatus(NumberUtil.getBitValue(terminalStatus, 0));
        sensorData.setLockRope(NumberUtil.getBitValue(terminalStatus, 0));
        sensorData.setLockTimes(lockTimes);
        sensorData.setIndex(flowId);
        sensorData.setVoltage(voltage);
        sensorData.setPower(power);
        sensorData.setRSSI(rssi);
        sensorData.setDateTime(slaveMachineZonedDateTime.toString());
        sensorData.setSensorType(sensorType);
        sensorData.setTemperature(temperature);
        sensorData.setHumidity(humidity);
        sensorData.setEvent(eventType);
        return sensorData;
    }

    /**
     * 解析P45
     * @param itemList
     * @return
     */
    private static LockEvent parseP45(List<String> itemList)
    {
        LockEvent model = new LockEvent();
        model.DateTime= parseBcdTime(itemList.get(2) + itemList.get(3)).toString();
        model.Latitude = Double.valueOf(itemList.get(4));
        if (itemList.get(5).equals("S"))
        {
            model.Latitude = -model.Latitude;
        }
        model.Longitude = Double.valueOf(itemList.get(6));
        if (itemList.get(5).equals("W"))
        {
            model.Longitude = -model.Longitude;
        }
        model.LocationType= itemList.get(8).equals("V") ? 0 : 1;
        model.Speed= Double.valueOf(itemList.get(9)).intValue();
        model.Direction = Integer.valueOf(itemList.get(10));
        model.Event = Integer.valueOf(itemList.get(11));
        //开锁验证
        int status= Integer.valueOf(itemList.get(12));
        model.RFIDNo = itemList.get(13);
        //动态密码开锁
        if (model.Event == 6)
        {
            if (status == 0)
            {
                //开锁密码不正确
                model.Status = 0;
            }
            else if (status > 0 && status <= 10)
            {
                //正常开锁
                model.Status = 1;
                //围栏内开锁时候的围栏ID
                model.UnlockFenceID = status;
            }
            else if (status == 98)
            {
                //正常开锁
                model.Status = 1;
            }
            else if (status == 99)
            {
                //设备开启了围栏内开锁，且当前开锁并未在围栏内，拒绝开锁
                model.Status = 3;
            }
        }
        else if (model.Event == 4)
        {
            if (Integer.valueOf(itemList.get(14)) == 0)
            {
                //开锁密码不正确
                model.Status = 0;
            }
            else
            {
                //正常开锁
                model.Status = 1;
            }
        }
        model.PsdErrorTimes = Integer.valueOf(itemList.get(15));
        model.Index = Integer.valueOf(itemList.get(16));
        if (itemList.size() > 17)
        {
            model.Mileage = Integer.valueOf(itemList.get(16));
        }
        return model;
    }

    /**
     * 解析从机数据温度
     *
     * @param temperatureInt
     * @return
     */
    private static double parseTemperature(int temperatureInt) {
        if (temperatureInt == 0xFFFF) {
            return 9999.9;
        }
        double temperature = ((short) (temperatureInt << 4) >> 4) * 0.1;
        if ((temperatureInt >> 12) > 0) {
            temperature = -temperature;
        }
        return temperature;
    }

    /**
     * 转换GPS时间
     *
     * @param bcdTimeStr
     * @return
     */
    public static ZonedDateTime parseBcdTime(String bcdTimeStr) {
        if(bcdTimeStr.equals("000000000000"))
        {
            //默认给出时间为2000年1月1日 00时00分00
            bcdTimeStr="010100000000";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
        LocalDateTime localDateTime = LocalDateTime.parse(bcdTimeStr, formatter);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
        return zonedDateTime;
    }

    /**
     * 解析定位数据
     * @param in
     * @return
     */
    public static Result decodeBinaryMessage(ByteBuf in)
    {
        //协议头
        in.readByte();
        //终端号码
        byte[] terminalNumArr = new byte[5];
        in.readBytes(terminalNumArr);
        String terminalNum = ByteBufUtil.hexDump(terminalNumArr);
        //协议版本号
        int version = in.readUnsignedByte();
        short tempByte = in.readUnsignedByte();
        //终端类型号
        int terminalType = tempByte >> 4;
        //数据类型号
        int dataType = tempByte & 0b00001111;
        //数据长度
        int dataLen = in.readUnsignedShort();
        //定位时间
        byte[] timeArr = new byte[6];
        in.readBytes(timeArr);
        String bcdTimeStr = ByteBufUtil.hexDump(timeArr);
        ZonedDateTime gpsZonedDateTime = parseBcdTime(bcdTimeStr);
        //纬度
        byte[] latArr = new byte[4];
        in.readBytes(latArr);
        String latHexStr = ByteBufUtil.hexDump(latArr);
        double lat = 0.0;
        BigDecimal latFloat = new BigDecimal(latHexStr.substring(2, 4) + "." + latHexStr.substring(4)).divide(new BigDecimal("60"), 6, RoundingMode.HALF_UP);
        lat = new BigDecimal(latHexStr.substring(0, 2)).add(latFloat).doubleValue();
        //经度
        byte[] lngArr = new byte[5];
        in.readBytes(lngArr);
        String lngHexStr = ByteBufUtil.hexDump(lngArr);
        double lng=0.0;
        BigDecimal lngFloat = new BigDecimal(lngHexStr.substring(3, 5) + "." + lngHexStr.substring(5, 9)).divide(new BigDecimal("60"), 6, RoundingMode.HALF_UP);
        lng = new BigDecimal(lngHexStr.substring(0, 3)).add(lngFloat).doubleValue();
        //位指示
        int bitFlag = Byte.parseByte(lngHexStr.substring(9, 10), 16);
        //定位状态
        int locationType = (bitFlag & 0x01) > 0 ? 1 : 0;
        //北纬、南纬
        if ((bitFlag & 0b0010) == 0) {
            lat = -lat;
        }
        //东经、西经
        if ((bitFlag & 0b0100) == 0) {
            lng = -lng;
        }
        //速度
        int speed = (int) (in.readUnsignedByte() * 1.85);
        //方向
        int direction = in.readUnsignedByte() * 2;
        //里程
        long mileage = in.readUnsignedInt();
        //GPS卫星个数
        int gpsSignal = in.readByte();
        //绑定车辆ID
        long vehicleId = in.readUnsignedInt();
        //终端状态
        int terminalStatus = in.readUnsignedShort();
        //是否基站定位
        if (NumberUtil.getBitValue(terminalStatus, 0) == 1) {
            locationType = 2;
        }
        //电量指示
        int batteryPercent = in.readUnsignedByte();
        //2G CELL ID
        int cellId2G = in.readUnsignedShort();
        //LAC
        int lac = in.readUnsignedShort();
        //GSM信号质量
        int cellSignal = in.readUnsignedByte();
        //区域报警ID
        int regionAlarmId = in.readUnsignedByte();
        //设备状态3
        int terminalStatus3 = in.readUnsignedByte();
        //唤醒源
        int fWakeSource=(terminalStatus3 & 0b0000_1111);
        //预留
        in.readShort();
        //IMEI号
        byte[] imeiArr = new byte[8];
        in.readBytes(imeiArr);
        String imei = ByteBufUtil.hexDump(imeiArr);
        //3G CELL ID 高16位
        int cellId3G = in.readUnsignedShort();
        int cellId=0;
        if(cellId3G>0){
            cellId=(cellId3G<<16)+cellId2G;
        }else{
            cellId=cellId2G;
        }
        //MCC
        int mcc = in.readUnsignedShort();
        //MNC
        int mnc = in.readUnsignedByte();
        //流水号
        int flowId = in.readUnsignedByte();
        //解析报警
        int fAlarm=parseLocationAlarm(terminalStatus);

        LocationData location=new LocationData();
        location.setProtocolType(version);
        location.setDeviceType(terminalType);
        location.setDataType(dataType);
        location.setDataLength(dataLen);
        location.setGpsTime(gpsZonedDateTime.toString());
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setLocationType(locationType);
        location.setSpeed(speed);
        location.setDirection(direction);
        location.setMileage(mileage);
        location.setGpsSignal(gpsSignal);
        location.setGSMSignal(cellSignal);
        location.setAlarm(fAlarm);
        location.setAlarmArea(regionAlarmId);
        location.setBattery(batteryPercent);
        location.setLockStatus(NumberUtil.getBitValue(terminalStatus, 7) == 1 ? 0 : 1);
        location.setLockRope(NumberUtil.getBitValue(terminalStatus, 6) == 1 ? 0 : 1);
        location.setBackCover(NumberUtil.getBitValue(terminalStatus, 13));
        location.setMCC(mcc);
        location.setMNC(mnc);
        location.setLAC(lac);
        location.setCELLID(cellId);
        location.setIMEI(imei);
        location.setAlarm(fWakeSource);
        location.setIndex(flowId);
        //定义定位数据实体类
        Result model = new Result();
        model.setDeviceID(terminalNum);
        model.setMsgType("Location");
        model.setDataBody(location);
        if (version < 0x19) {
            model.setReplyMsg("(P35)");
        } else {
            model.setReplyMsg(String.format("(P69,0,%s)",flowId));
        }
        return model;
    }

    /**
     * 解析定位报警
     * @param terminalStatus
     * @return
     */
    private static int parseLocationAlarm(int terminalStatus)
    {
        //是否报警
        int fAlarm = -1;
        //是否应答确认
        if (NumberUtil.getBitValue(terminalStatus, 5) == 1) {
            //判断报警
            if (NumberUtil.getBitValue(terminalStatus, 1) == 1) {
                fAlarm = Integer.parseInt(AlarmTypeEnum.LOCK_ALARM_9.getValue());
            } else if (NumberUtil.getBitValue(terminalStatus, 2) == 1) {
                fAlarm = Integer.parseInt(AlarmTypeEnum.LOCK_ALARM_10.getValue());
            } else if (NumberUtil.getBitValue(terminalStatus, 3) == 1) {
                fAlarm = Integer.parseInt(AlarmTypeEnum.LOCK_ALARM_1.getValue());
            } else if (NumberUtil.getBitValue(terminalStatus, 4) == 1) {
                fAlarm = Integer.parseInt(AlarmTypeEnum.LOCK_ALARM_2.getValue());
            } else if (NumberUtil.getBitValue(terminalStatus, 8) == 1) {
                fAlarm = Integer.parseInt(AlarmTypeEnum.LOCK_ALARM_3.getValue());
            } else if (NumberUtil.getBitValue(terminalStatus, 9) == 1) {
                fAlarm = Integer.parseInt(AlarmTypeEnum.LOCK_ALARM_4.getValue());
            } else if (NumberUtil.getBitValue(terminalStatus, 10) == 1) {
                fAlarm = Integer.parseInt(AlarmTypeEnum.LOCK_ALARM_5.getValue());
            } else if (NumberUtil.getBitValue(terminalStatus, 11) == 1) {
                fAlarm = Integer.parseInt(AlarmTypeEnum.LOCK_ALARM_6.getValue());
            } else if (NumberUtil.getBitValue(terminalStatus, 12) == 1) {
                fAlarm = Integer.parseInt(AlarmTypeEnum.LOCK_ALARM_7.getValue());
            } else if (NumberUtil.getBitValue(terminalStatus, 14) == 1) {
                fAlarm = Integer.parseInt(AlarmTypeEnum.LOCK_ALARM_8.getValue());
            } else {
                fAlarm = -1;
            }
        }
        return fAlarm;
    }

    /**
     * 指令应答回复
     * @param msgType
     * @param itemList
     * @return
     */
    private static String replyMessage(String msgType,List<String> itemList)
    {
        String replyContent = null;
        switch (msgType)
        {
            case "P22":
                ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
                replyContent = String.format("(P22,%s)", currentDateTime.format(formatter));
                break;
            case "P43":
                if (itemList.get(2).equals("0")) {
                    //密码重置
                    replyContent = String.format("(P44,1,888888)");
                }
                break;
            case "P45":
                replyContent = String.format("(P69,0,%s)", itemList.get(16));
                break;
            case "P52":
                if (itemList.get(2).equals("2")) {
                    replyContent = String.format("(P52,2,%s)", itemList.get(3));
                }
                break;
            default:
                break;
        }
        return replyContent;
    }

    /**
     * 指令应答回复
     * @param msgType
     * @param index
     * @return
     */
    public static String replyMessage(String msgType, int index)
    {
        String replyContent = null;
        switch (msgType)
        {
            case "WLNET5":
            case "WLNET7":
                replyContent = String.format("(P69,0,%s)", index);
                break;
            default:
                break;
        }
        return replyContent;
    }
}
