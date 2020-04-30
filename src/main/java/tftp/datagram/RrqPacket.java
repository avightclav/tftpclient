package tftp.datagram;

import tftp.sendmode.SendMode;

public class RrqPacket extends TftpPacket {

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
        return new byte[0];
    }
}
