package tftp.datagram;

public class ErrorPacket extends TftpPacket {
    private final int errorCode;
    private final String errorMessage;

    public ErrorPacket(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
