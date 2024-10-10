package com.jointech.sdk.jt701.utils;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * <p>Description: 用来存储一些解析中遇到的公共方法</p>
 *
 * @author lenny
 * @version 1.0.1
 * @date 20210328
 */
public class CommonUtil {
    private CommonUtil()
    {

    }

    /**
     * 反转义文本透传数据
     *
     * @param in
     * @param frame
     * @param bodyLen
     */
    public static void unescape(ByteBuf in, ByteBuf frame, int bodyLen) {
        int i = 0;
        while (i < bodyLen) {
            int b = in.readUnsignedByte();
            if (b == 0x3D) {
                int nextByte = in.readUnsignedByte();
                if (nextByte == 0x14) {
                    frame.writeByte(0x3D ^ 0x14);
                } else if (nextByte == 0x15) {
                    frame.writeByte(0x3D ^ 0x15);
                } else if (nextByte == 0x00) {
                    frame.writeByte(0x3D ^ 0x00);
                } else if (nextByte == 0x11) {
                    frame.writeByte(0x3D ^ 0x11);
                } else {
                    frame.writeByte(b);
                    frame.writeByte(nextByte);
                }
                i += 2;
            } else {
                frame.writeByte(b);
                i++;
            }
        }
    }

    /**
     * 去掉字符串最后一个字符
     * @param inStr 输入的字符串
     * @param suffix 需要去掉的字符
     * @return
     */
    public static String trimEnd(String inStr, String suffix) {
        while(inStr.endsWith(suffix)){
            inStr = inStr.substring(0,inStr.length()-suffix.length());
        }
        return inStr;
    }

    /**
     * 16进制转byte[]
     * @param hex
     * @return
     */
    public static byte[] hexStr2Byte(String hex) {
        ByteBuffer bf = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i++) {
            String hexStr = hex.charAt(i) + "";
            i++;
            hexStr += hex.charAt(i);
            byte b = (byte) Integer.parseInt(hexStr, 16);
            bf.put(b);
        }
        return bf.array();
    }
}
