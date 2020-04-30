package tftp.datagram;

import util.Tftp;

public class AckPacket extends TftpPacket {
    private final byte[] OPCODE = new byte[]{0, 4};

    public short getAcknowledgeNumber() {
        return acknowledgeNumber;
    }

    private final short acknowledgeNumber;

    public AckPacket(short acknowledgeNumber) {
        this.acknowledgeNumber = acknowledgeNumber;
    }

    @Override
    public byte[] toBytes() {
        byte[] acknowledgeNumberBytes = Tftp.shortToBytesArray(acknowledgeNumber);
        byte[] byteRepresentation = new byte[OPCODE.length + acknowledgeNumberBytes.length];
        System.arraycopy(OPCODE, 0, byteRepresentation, 0, OPCODE.length);
        System.arraycopy(acknowledgeNumberBytes, 0, byteRepresentation, OPCODE.length, acknowledgeNumberBytes.length);
        return byteRepresentation;
    }

}
