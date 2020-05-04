package tftp.packet;

import tftp.sendmode.SendMode;

import java.nio.charset.StandardCharsets;

import static tftp.util.Util.appendNullByte;

public class RrqPacket extends TftpPacket {

    private final static byte[] OPCODE = {0, 1};

    private final String filename;
    private final SendMode sendMode;

    public RrqPacket(String filename, SendMode sendMode) {
        this.filename = filename;
        this.sendMode = sendMode;
    }

    public String getFilename() {
        return filename;
    }

    public SendMode getMode() {
        return sendMode;
    }

    @Override
    public byte[] toBytes() {
        byte[] sendModeBytes = appendNullByte(sendMode.toString().getBytes(StandardCharsets.US_ASCII));
        byte[] filenameBytes = appendNullByte(filename.getBytes(StandardCharsets.US_ASCII));

        byte[] data = new byte[2 + sendModeBytes.length + filenameBytes.length];
        System.arraycopy(OPCODE, 0, data, 0, 2);
        System.arraycopy(filenameBytes, 0, data, 2, filenameBytes.length);
        System.arraycopy(sendModeBytes, 0, data, 2 + filenameBytes.length, sendModeBytes.length);
        return data;
    }
}
