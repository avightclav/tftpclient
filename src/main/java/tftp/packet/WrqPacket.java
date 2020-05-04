package tftp.packet;

import tftp.sendmode.SendMode;

import java.nio.charset.StandardCharsets;

import static tftp.util.Util.appendNullByte;

public class WrqPacket extends TftpPacket {
    private final static byte[] OPCODE = {0, 2};

    private final String filename;
    private final SendMode sendMode;
    private final byte[] packet;
    private final int length;


    public WrqPacket(String filename, SendMode sendMode) {
        this.filename = filename;
        this.sendMode = sendMode;
        this.packet = toBytes(sendMode, filename);
        this.length = this.packet.length;
    }

    public String getFilename() {
        return filename;
    }

    public SendMode getMode() {
        return sendMode;
    }

    private static byte[] toBytes(SendMode sendMode, String filename) {
        byte[] sendModeBytes = appendNullByte(sendMode.toString().toLowerCase().getBytes(StandardCharsets.US_ASCII));
        byte[] filenameBytes = appendNullByte(filename.getBytes(StandardCharsets.US_ASCII));

        byte[] packet = new byte[OPCODE.length + filenameBytes.length + sendModeBytes.length];
        System.arraycopy(OPCODE, 0, packet, 0, 2);
        System.arraycopy(filenameBytes, 0, packet, 2, filenameBytes.length);
        System.arraycopy(sendModeBytes, 0, packet, 2 + filenameBytes.length, sendModeBytes.length);
        return packet;
    }

    @Override
    public byte[] toBytes() {
        return packet;
    }

    public int getLength() {
        return length;
    }
}
