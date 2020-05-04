package tftp.packet;

import tftp.util.Util;

public class AckPacket extends TftpPacket {
    private final static byte[] OPCODE = new byte[]{0, 4};
    private final short acknowledgeNumber;
    private final byte[] packet;
    private final int length;

    public AckPacket(short acknowledgeNumber) {
        this.acknowledgeNumber = acknowledgeNumber;
        this.packet = toBytes(acknowledgeNumber);
        this.length = this.packet.length;
    }

    private static byte[] toBytes(short acknowledgeNumber) {
        byte[] acknowledgeNumberBytes = Util.shortToBytesArray(acknowledgeNumber);
        byte[] packet = new byte[OPCODE.length + acknowledgeNumberBytes.length];
        System.arraycopy(OPCODE, 0, packet, 0, OPCODE.length);
        System.arraycopy(acknowledgeNumberBytes, 0, packet, OPCODE.length, acknowledgeNumberBytes.length);
        return packet;
    }

    @Override
    public byte[] toBytes() {
        return packet;
    }

    public short getAcknowledgeNumber() {
        return acknowledgeNumber;
    }

    public int getLength() {
        return length;
    }
}
