package tftp.datagram;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class TftpDatagram {

    public static final short RRQ_OPCODE = 1;
    public static final short WRQ_OPCODE = 2;
    public static final short DATA_OPCODE = 3;
    public static final short ACK_OPCODE = 4;
    public static final short ERROR_OPCODE = 5;

    public static TftpDatagram makeTftpDatagram(byte[] data, int length) {
        int opcode = data[0] * 16 + data[1];

        switch (opcode) {
            case RRQ_OPCODE:
                int filenameStartIndex = 2;
                int filenameEndIndex = 2;
                while (data[filenameEndIndex] != 0)
                    filenameEndIndex++;
                String filename = new String(Arrays.copyOfRange(data, filenameStartIndex, filenameEndIndex));

                int modeStartIndex = filenameEndIndex + 1;
                int modeEndIndex = filenameEndIndex + 1;
                while (data[modeEndIndex] != 0)
                    modeEndIndex++;
                String mode = new String(Arrays.copyOfRange(data, modeStartIndex, modeEndIndex));

                return new RrqDatagram(filename, mode);
            case WRQ_OPCODE:
                System.out.println("Not implemented WRQ_OPCODE");
            case DATA_OPCODE:
                short blockNumber = (short) (data[2] * 128 + data[3]);
                int dataStartIndex = 4;
                byte[] dataBody = Arrays.copyOfRange(data, dataStartIndex, length);
                return new DataDatagram(blockNumber, dataBody);
            case ACK_OPCODE:
                short acknowledgeNumber = (short)(((data[2] & 0xFF) << 8) | (data[3] & 0xFF));;
                return new AckDatagram(acknowledgeNumber);
            case ERROR_OPCODE:
                int errorCode = data[2] * 16 + data[3];
                int errorMessageStartIndex = 4;
                int errorMessageEndIndex = 4;
                while (data[errorMessageEndIndex] != 0)
                    errorMessageEndIndex++;
                String errorMessage = new String(Arrays.copyOfRange(data, errorMessageStartIndex, errorMessageEndIndex));
                return new ErrorDatagram(errorCode, errorMessage);
            default:
                throw new UnsupportedOperationException("Invalid opcode: " + opcode);
        }
    }

    public abstract byte[] toBytes();
}
