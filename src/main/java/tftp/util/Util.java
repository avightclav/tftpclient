package tftp.util;

public class Util {

    public static byte[] appendNullByte(byte[] data) {
        byte[] extendedSizeData = new byte[data.length + 1];
        System.arraycopy(data, 0, extendedSizeData, 0, data.length);
        extendedSizeData[extendedSizeData.length - 1] = (byte) 0;
        return extendedSizeData;
    }

    public static byte[] shortToBytesArray(short number) {
        return new byte[]{(byte) (number >>> 8), (byte) (number & 0xFF)};
    }

    public static short byteToShort(byte high, byte low) {
        return (short) (((high & 0xFF) << 8) | (low & 0xFF));
    }
}
