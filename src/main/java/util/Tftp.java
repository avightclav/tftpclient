package util;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Tftp {

    public static final int RRQ_WRQ_MIN_LENGTH = 2;
    public static final byte[] RRQ_OPCODE = new byte[] {(byte) 0, (byte) 1};

    public static byte[] appendNullByte(byte[] data) {
        byte[] extendedSizeData = new byte[data.length + 1];
        System.arraycopy(data, 0, extendedSizeData, 0, data.length);
        extendedSizeData[extendedSizeData.length - 1] = (byte) 0;
        return extendedSizeData;
    }

    // https://www.baeldung.com/java-random-string
    public static String randomFilename(int filenameLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(filenameLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    public static DatagramPacket getRrqDatagram(SocketAddress address, String filename) {
        byte[] sendModeBytes = appendNullByte("netascii".getBytes(StandardCharsets.US_ASCII));
        byte[] filenameBytes = appendNullByte(filename.getBytes(StandardCharsets.US_ASCII));

        byte[] data = new byte[RRQ_WRQ_MIN_LENGTH + sendModeBytes.length + filenameBytes.length];
        System.arraycopy(RRQ_OPCODE, 0, data, 0, 2);
        System.arraycopy(filenameBytes, 0, data, 2, filenameBytes.length);
        System.arraycopy(sendModeBytes, 0, data, 2 + filenameBytes.length, sendModeBytes.length);
        DatagramPacket packet = new DatagramPacket(data, data.length, address);
        return packet;
    }

    public static byte[] shortToBytesArray(short number) {
        return new byte[]{(byte)(number >>>8),(byte)(number &0xFF)};
    }

    public static short byteToShort(byte high, byte low) {
        return (short) (((high & 0xFF) << 8) | (low & 0xFF));
    }
}
