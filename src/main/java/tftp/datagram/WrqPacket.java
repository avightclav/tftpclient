package tftp.datagram;

import tftp.sendmode.SendMode;

import java.nio.charset.StandardCharsets;

import static util.Tftp.appendNullByte;

public class WrqPacket extends TftpPacket {
    private final static byte[] OPCODE = {0, 2};

    private final String filename;
    private final SendMode sendMode;


    public WrqPacket(String filename, SendMode sendMode) {
        this.filename = filename;
        this.sendMode = sendMode;
    }


    @Override
    public byte[] toBytes() {
        byte[] sendModeBytes = appendNullByte(sendMode.toString().toLowerCase().getBytes(StandardCharsets.US_ASCII));
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

    public SendMode getMode() {
        return sendMode;
    }
}
