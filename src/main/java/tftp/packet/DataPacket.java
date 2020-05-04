package tftp.packet;

import tftp.util.Util;

public class DataPacket extends TftpPacket {
    private final static byte[] OPCODE = {0, 3};
    private final short blockNum;
    private final byte[] data;
    private final byte[] packet;
    private final int length;

    public DataPacket(short blockNum, byte[] data) {
        this.blockNum = blockNum;
        this.data = data;
        this.packet = toBytes(blockNum, data);
        this.length = this.packet.length;
    }

    public short getBlockNum() {
        return blockNum;
    }

    public byte[] getData() {
        return data;
    }

    private static byte[] toBytes(short blockNum, byte[] data) {
        byte[]  packet = new byte[OPCODE.length + 2 + data.length];
        byte[] blockNumberBytes = Util.shortToBytesArray(blockNum);

        System.arraycopy(OPCODE, 0, packet, 0, OPCODE.length);
        System.arraycopy(blockNumberBytes, 0, packet, OPCODE.length, blockNumberBytes.length);
        System.arraycopy(data, 0, packet, OPCODE.length + blockNumberBytes.length, data.length);
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
