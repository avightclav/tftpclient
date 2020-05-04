package tftp.exception;

public class UnterminatedPacketFieldException extends TftpException {
    public UnterminatedPacketFieldException(String s) {
        super(s);
    }
}
