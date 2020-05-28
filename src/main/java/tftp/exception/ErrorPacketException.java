package tftp.exception;

import tftp.packet.ErrorPacket;

public class ErrorPacketException extends TftpException {
    public ErrorPacketException(String message, ErrorPacket errorPacket) {
        super(message + " " + errorPacket.getErrorCode() + ": " + errorPacket.getErrorMessage());
    }
}
