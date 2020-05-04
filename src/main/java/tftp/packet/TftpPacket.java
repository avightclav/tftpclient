package tftp.packet;

import tftp.exception.PacketWithUnsupportedOpcodeException;
import tftp.exception.UnterminatedPacketFieldException;
import tftp.sendmode.SendMode;
import tftp.util.Util;

import java.util.Arrays;

import static tftp.util.Util.byteToShort;

public abstract class TftpPacket {

    public static final short RRQ_OPCODE = 1;
    public static final short WRQ_OPCODE = 2;
    public static final short DATA_OPCODE = 3;
    public static final short ACK_OPCODE = 4;
    public static final short ERROR_OPCODE = 5;

    public static TftpPacket makeTftpPacket(byte[] data, int length) throws PacketWithUnsupportedOpcodeException, UnterminatedPacketFieldException {
        int opcode = data[0] * 16 + data[1];

        switch (opcode) {
            case RRQ_OPCODE: {
                final int filenameStartIndex = 2;
                int filenameEndIndex = 2;
                try {
                    while (data[filenameEndIndex] != 0)
                        filenameEndIndex++;
                } catch (IndexOutOfBoundsException e) {
                    throw new UnterminatedPacketFieldException("Unterminated field 'filename' in RRQ packet");
                }

                String filename = new String(Arrays.copyOfRange(data, filenameStartIndex, filenameEndIndex));

                int modeStartIndex = filenameEndIndex + 1;
                int modeEndIndex = filenameEndIndex + 1;
                try {
                    while (data[modeEndIndex] != 0)
                        modeEndIndex++;
                } catch (IndexOutOfBoundsException e) {
                    throw new UnterminatedPacketFieldException("Unterminated field 'mode' in RRQ packet");
                }

                SendMode mode = SendMode.valueOf(new String(Arrays.copyOfRange(data, modeStartIndex, modeEndIndex)).toUpperCase());

                return new RrqPacket(filename, mode);
            }
            case WRQ_OPCODE: {
                final int filenameStartIndex = 2;
                int filenameEndIndex = 2;
                try {
                    while (data[filenameEndIndex] != 0)
                        filenameEndIndex++;
                } catch (IndexOutOfBoundsException e) {
                    throw new UnterminatedPacketFieldException("Unterminated field 'filename' in WRQ packet");
                }

                String filename = new String(Arrays.copyOfRange(data, filenameStartIndex, filenameEndIndex));

                int modeStartIndex = filenameEndIndex + 1;
                int modeEndIndex = filenameEndIndex + 1;
                try {
                    while (data[modeEndIndex] != 0)
                        modeEndIndex++;
                } catch (IndexOutOfBoundsException e) {
                    throw new UnterminatedPacketFieldException("Unterminated field 'mode' in WRQ packet");
                }

                SendMode mode = SendMode.valueOf(new String(Arrays.copyOfRange(data, modeStartIndex, modeEndIndex)).toUpperCase());

                return new WrqPacket(filename, mode);
            }
            case DATA_OPCODE:
                final short blockNumber = byteToShort(data[2], data[3]);
                final int dataStartIndex = 4;
                byte[] dataBody = Arrays.copyOfRange(data, dataStartIndex, length);
                return new DataPacket(blockNumber, dataBody);
            case ACK_OPCODE:
                short acknowledgeNumber = byteToShort(data[2], data[3]);
                return new AckPacket(acknowledgeNumber);
            case ERROR_OPCODE:
                short errorCode = byteToShort(data[2], data[3]);
                int errorMessageStartIndex = 4;
                int errorMessageEndIndex = 4;
                while (data[errorMessageEndIndex] != 0)
                    errorMessageEndIndex++;
                String errorMessage = new String(Arrays.copyOfRange(data, errorMessageStartIndex, errorMessageEndIndex));
                return new ErrorPacket(errorCode, errorMessage);
            default:
                throw new PacketWithUnsupportedOpcodeException("Can't create any TftpPacket with opcode: " + opcode);
        }
    }

    public abstract byte[] toBytes();
}
