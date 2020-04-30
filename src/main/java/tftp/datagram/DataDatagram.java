package tftp.datagram;

import util.Tftp;

public class DataDatagram extends TftpDatagram {
    private final static byte[] OPCODE = {0, 3};
    private final short blockNum;
    private final byte[] data;

    public DataDatagram(short blockNum, byte[] data) {
        this.blockNum = blockNum;
        this.data = data;
    }

    public short getBlockNum() {
        return blockNum;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public byte[] toBytes() {
        byte[]  byteRepresentation = new byte[OPCODE.length + 2 + data.length];
        byte[] blockNumberBytes = Tftp.shortToBytesArray(blockNum);

        System.arraycopy(OPCODE, 0, byteRepresentation, 0, OPCODE.length);
        System.arraycopy(blockNumberBytes, 0, byteRepresentation, OPCODE.length, blockNumberBytes.length);
        System.arraycopy(data, 0, byteRepresentation, OPCODE.length + blockNumberBytes.length, data.length);
        return byteRepresentation;
    }
}
