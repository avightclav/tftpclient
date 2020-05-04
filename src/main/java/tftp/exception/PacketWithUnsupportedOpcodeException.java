package tftp.exception;

public class PacketWithUnsupportedOpcodeException extends TftpException {
    public PacketWithUnsupportedOpcodeException(String s) {
        super(s);
    }
}
