package tftp.packet;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static tftp.util.Util.appendNullByte;
import static tftp.util.Util.shortToBytesArray;

public class ErrorPacket extends TftpPacket {
    public final static short TFTP_ERROR_NOT_DEFINED = 0;
    public final static short TFTP_ERROR_FILE_NOT_FOUND = 1;
    public final static short TFTP_ERROR_ACCESS_VIOLATION = 2;
    public final static short TFTP_ERROR_DISK_FULL = 3;
    public final static short TFTP_ERROR_ILLEGAL_TFTP_OP = 4;
    public final static short TFTP_ERROR_UNKNOWN_TID = 5;
    public final static short TFTP_ERROR_FILE_ALREADY_EXISTS = 6;
    public final static short TFTP_ERROR_NO_SUCH_USER = 6;

    private final static byte[] OPCODE = {0, 5};
    public final static byte[] TFTP_ERROR_NOT_DEFINED_BYTES = {0, TFTP_ERROR_NOT_DEFINED};
    public final static byte[] TFTP_ERROR_FILE_NOT_FOUND_BYTES = {0, TFTP_ERROR_FILE_NOT_FOUND};
    public final static byte[] TFTP_ERROR_ACCESS_VIOLATION_BYTES = {0, TFTP_ERROR_ACCESS_VIOLATION};
    public final static byte[] TFTP_ERROR_DISK_FULL_BYTES = {0, TFTP_ERROR_DISK_FULL};
    public final static byte[] TFTP_ERROR_ILLEGAL_TFTP_OP_BYTES = {0, TFTP_ERROR_ILLEGAL_TFTP_OP};
    public final static byte[] TFTP_ERROR_UNKNOWN_TID_BYTES = {0, TFTP_ERROR_UNKNOWN_TID};
    public final static byte[] TFTP_ERROR_FILE_ALREADY_EXISTS_BYTES = {0, TFTP_ERROR_FILE_ALREADY_EXISTS};
    public final static byte[] TFTP_ERROR_NO_SUCH_USER_BYTES = {0, TFTP_ERROR_NO_SUCH_USER};



    private final int errorCode;
    private final String errorMessage;
    private final byte[] packet;
    private final int length;

    public ErrorPacket(short errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.packet = toBytes(errorCode, errorMessage);
        this.length = this.packet.length;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private static byte[] toBytes(short errorCode, String errorMessage) {
        byte[] messageASCII = appendNullByte(errorMessage.getBytes(StandardCharsets.US_ASCII));
        byte[] errorCodeBytes = shortToBytesArray(errorCode);

        byte[] packet = new byte[OPCODE.length + errorCodeBytes.length + messageASCII.length];
        System.arraycopy(OPCODE, 0, packet, 0, OPCODE.length);
        System.arraycopy(errorCodeBytes, 0, packet, OPCODE.length, errorCodeBytes.length);
        System.arraycopy(messageASCII, 0, packet, OPCODE.length + errorCodeBytes.length, messageASCII.length);
        return packet;
    }

    @Override
    public byte[] toBytes() {
        return this.packet;
    }

    public int getLength() {
        return length;
    }
}
