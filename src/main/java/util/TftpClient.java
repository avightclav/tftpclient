package util;

import tftp.datagram.AckPacket;
import tftp.datagram.DataPacket;
import tftp.datagram.TftpPacket;
import tftp.datagram.WrqPacket;
import tftp.sendmode.SendMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Paths;
import java.util.Arrays;

public class TftpClient {

    public static void getFile(String address, int port, String localFilename, String remoteFilename) throws Exception {
        DatagramSocket localSocket = new DatagramSocket();
        SocketAddress remoteAddress = new InetSocketAddress(address, port);
        DatagramPacket rrqDatagram = util.Tftp.getRrqDatagram(remoteAddress, remoteFilename);
        localSocket.send(rrqDatagram);

        String tempFilename = Tftp.randomFilename(16) + ".temp";

        String parentPath = Paths.get(localFilename).getParent() != null ? Paths.get(localFilename).getParent().toString() : "";

        File yourFile = new File(Paths.get(parentPath, tempFilename).toString());
        FileOutputStream oFile = new FileOutputStream(yourFile);

        DatagramPacket bigPack = new DatagramPacket(new byte[1024], 1024);

        localSocket.receive(bigPack);

        TftpPacket datagram = TftpPacket.makeTftpDatagram(bigPack.getData(), bigPack.getLength());

        if (!(datagram instanceof DataPacket))
            throw new Exception("");
        DataPacket dataDatagram = (DataPacket) datagram;
        oFile.write(dataDatagram.getData());

        long realDatalen = dataDatagram.getData().length;

        SocketAddress sessionAddress = bigPack.getSocketAddress();
        AckPacket ackDatagram = new AckPacket(dataDatagram.getBlockNum());
        DatagramPacket ackPacket = new DatagramPacket(ackDatagram.toBytes(), ackDatagram.toBytes().length, sessionAddress);
        localSocket.send(ackPacket);

        while (dataDatagram.getData().length == 512) {
            localSocket.receive(bigPack);

            datagram = TftpPacket.makeTftpDatagram(bigPack.getData(), bigPack.getLength());

            if (!(datagram instanceof DataPacket))
                throw new Exception("");
            dataDatagram = (DataPacket) datagram;
            oFile.write(dataDatagram.getData());

            realDatalen += dataDatagram.getData().length;
            ackDatagram = new AckPacket(dataDatagram.getBlockNum());
            ackPacket = new DatagramPacket(ackDatagram.toBytes(), ackDatagram.toBytes().length, sessionAddress);
            localSocket.send(ackPacket);

            System.out.print("\rFile: "+ localFilename + " got " + realDatalen + " bytes");
            System.out.flush();
        }

        oFile.close();
        System.out.println("\rFile: "+ localFilename + " got " + realDatalen + " bytes. Finished.");
        File saveFile = new File(localFilename);
        yourFile.renameTo(saveFile);
    }

    public static void sendFile(String address, int port, String localFilename, String remoteFilename) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        SocketAddress remoteAddress = new InetSocketAddress(address, port);

        File yourFile = new File(localFilename);
        FileInputStream oFile = new FileInputStream(yourFile);

        WrqPacket wrqDatagram = new WrqPacket(remoteFilename, SendMode.OCTET);
        DatagramPacket wrqPacket = new DatagramPacket(wrqDatagram.toBytes(), wrqDatagram.toBytes().length, remoteAddress);

        socket.send(wrqPacket);

        DatagramPacket bigPack = new DatagramPacket(new byte[2048], 2048);
        socket.receive(bigPack);

        TftpPacket datagram = TftpPacket.makeTftpDatagram(bigPack.getData(), bigPack.getLength());
        if (!(datagram instanceof AckPacket) || ((AckPacket) datagram).getAcknowledgeNumber() != 0)
            throw new Exception();

        byte[] dataBody = new byte[512];
        int readLength = oFile.read(dataBody);
        while (readLength != -1) {
            DataPacket dataDatagram = new DataPacket((short) (((AckPacket) datagram).getAcknowledgeNumber() + 1), Arrays.copyOf(dataBody, readLength));
            DatagramPacket dataPacket = new DatagramPacket(dataDatagram.toBytes(), dataDatagram.toBytes().length, bigPack.getSocketAddress());

            socket.send(dataPacket);

            socket.receive(bigPack);
            datagram = TftpPacket.makeTftpDatagram(bigPack.getData(), bigPack.getLength());
            if (!(datagram instanceof AckPacket) || ((AckPacket) datagram).getAcknowledgeNumber() != dataDatagram.getBlockNum())
                throw new Exception();

            readLength = oFile.read(dataBody);
            System.out.println("Read: " + readLength);
            System.out.println("Ack: " + ((AckPacket) datagram).getAcknowledgeNumber());
        }
    }
}
