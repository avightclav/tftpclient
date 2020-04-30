package tftp.datagram;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

import static util.Tftp.appendNullByte;

public class WrqDatagram extends TftpDatagram {
    private final static byte[] OPCODE = {0, 2};

    private final String filename;
    private final String mode;


    public WrqDatagram(String filename, String mode) {
        this.filename = filename;
        this.mode = mode;
    }


    @Override
    public byte[] toBytes() {
        byte[] sendModeBytes = appendNullByte(mode.getBytes(StandardCharsets.US_ASCII));
        byte[] filenameBytes = appendNullByte(filename.getBytes(StandardCharsets.US_ASCII));

        byte[] data = new byte[OPCODE.length + filenameBytes.length + sendModeBytes.length];
        System.arraycopy(OPCODE, 0, data, 0, 2);
        System.arraycopy(filenameBytes, 0, data, 2, filenameBytes.length);
        System.arraycopy(sendModeBytes, 0, data, 2 + filenameBytes.length, sendModeBytes.length);
        return data;
    }

    public String getFilename() {
        return filename;
    }

    public String getMode() {
        return mode;
    }
}
