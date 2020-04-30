package tftp.datagram;

public class ErrorDatagram extends TftpDatagram{
    private final int errorCode;
    private final String errorMessage;

    public ErrorDatagram(int errorCode, String errorMessage) {
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
