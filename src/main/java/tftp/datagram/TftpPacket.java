package tftp.datagram;

import tftp.sendmode.SendMode;
import util.Tftp;

import java.util.Arrays;

public abstract class TftpPacket {

    public static final short RRQ_OPCODE = 1;
    public static final short WRQ_OPCODE = 2;
    public static final short DATA_OPCODE = 3;
    public static final short ACK_OPCODE = 4;
    public static final short ERROR_OPCODE = 5;

    public static TftpPacket makeTftpDatagram(byte[] data, int length) {
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
                SendMode mode = SendMode.valueOf(new String(Arrays.copyOfRange(data, modeStartIndex, modeEndIndex)).toUpperCase());

                return new RrqPacket(filename, mode);
            case WRQ_OPCODE:
                System.out.println("Not implemented WRQ_OPCODE");
            case DATA_OPCODE:
                short blockNumber = Tftp.byteToShort(data[2], data[3]);
                int dataStartIndex = 4;
                byte[] dataBody = Arrays.copyOfRange(data, dataStartIndex, length);
                return new DataPacket(blockNumber, dataBody);
            case ACK_OPCODE:
                short acknowledgeNumber = Tftp.byteToShort(data[2], data[3]);
                return new AckPacket(acknowledgeNumber);
            case ERROR_OPCODE:
                int errorCode = data[2] * 16 + data[3];
                int errorMessageStartIndex = 4;
                int errorMessageEndIndex = 4;
                while (data[errorMessageEndIndex] != 0)
                    errorMessageEndIndex++;
                String errorMessage = new String(Arrays.copyOfRange(data, errorMessageStartIndex, errorMessageEndIndex));
                return new ErrorPacket(errorCode, errorMessage);
            default:
                throw new UnsupportedOperationException("Invalid opcode: " + opcode);
        }
    }

    public abstract byte[] toBytes();
}
