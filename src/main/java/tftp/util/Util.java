package tftp.util;

import java.util.Random;

public class Util {

    public static String randomFilename(int filenameLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(filenameLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static byte[] appendNullByte(byte[] data) {
        byte[] extendedSizeData = new byte[data.length + 1];
        System.arraycopy(data, 0, extendedSizeData, 0, data.length);
        extendedSizeData[extendedSizeData.length - 1] = (byte) 0;
        return extendedSizeData;
    }
    // https://www.baeldung.com/java-random-string

    public static byte[] shortToBytesArray(short number) {
        return new byte[]{(byte) (number >>> 8), (byte) (number & 0xFF)};
    }

    public static short byteToShort(byte high, byte low) {
        return (short) (((high & 0xFF) << 8) | (low & 0xFF));
    }
}
