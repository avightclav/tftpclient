package tftp.exception;

import tftp.packet.ErrorPacket;

public class ErrorDatagramException extends TftpException {
    public ErrorDatagramException(String message, ErrorPacket errorPacket) {
        super(message + " " + errorPacket.getErrorCode() + ": " + errorPacket.getErrorMessage());
    }
}
