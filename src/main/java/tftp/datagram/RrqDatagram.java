package tftp.datagram;

public class RrqDatagram extends TftpDatagram {

    private final String filename;
    private final String mode;

    public RrqDatagram(String filename, String mode) {
        this.filename = filename;
        this.mode = mode;
    }

    public String getFilename() {
        return filename;
    }

    public String getMode() {
        return mode;
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
