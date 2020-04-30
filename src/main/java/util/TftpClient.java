package util;

import tftp.datagram.AckDatagram;
import tftp.datagram.DataDatagram;
import tftp.datagram.TftpDatagram;
import tftp.datagram.WrqDatagram;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class TftpClient {
    public static void getFile(String address, int port, String filename) throws Exception {
        DatagramSocket localSocket = new DatagramSocket();
        SocketAddress remoteAddress = new InetSocketAddress(address, port);
        DatagramPacket rrqDatagram = util.Tftp.getRrqDatagram(remoteAddress, filename);
        localSocket.send(rrqDatagram);

        String tempFilename = Tftp.randomFilename(16) + ".temp";
        File yourFile = new File(tempFilename);
        yourFile.createNewFile(); // if file already exists will do nothing
        FileOutputStream oFile = new FileOutputStream(yourFile);

        DatagramPacket bigPack = new DatagramPacket(new byte[1024], 1024);

        localSocket.receive(bigPack);

        TftpDatagram datagram = TftpDatagram.makeTftpDatagram(bigPack.getData(), bigPack.getLength());

        if (!(datagram instanceof DataDatagram))
            throw new Exception("");
        DataDatagram dataDatagram = (DataDatagram) datagram;
        oFile.write(dataDatagram.getData());

        SocketAddress sessionAddress = bigPack.getSocketAddress();
        AckDatagram ackDatagram = new AckDatagram(dataDatagram.getBlockNum());
        DatagramPacket ackPacket = new DatagramPacket(ackDatagram.toBytes(), ackDatagram.toBytes().length, sessionAddress);
        localSocket.send(ackPacket);

        while (dataDatagram.getData().length == 512) {
            localSocket.receive(bigPack);

            datagram = TftpDatagram.makeTftpDatagram(bigPack.getData(), bigPack.getLength());

            if (!(datagram instanceof DataDatagram))
                throw new Exception("");
            dataDatagram = (DataDatagram) datagram;
            oFile.write(dataDatagram.getData());
            ackDatagram = new AckDatagram(dataDatagram.getBlockNum());
            ackPacket = new DatagramPacket(ackDatagram.toBytes(), ackDatagram.toBytes().length, sessionAddress);
            localSocket.send(ackPacket);

            System.out.println("data num: " + dataDatagram.getBlockNum());
        }

        oFile.close();
    }

    public static void sendFile(String address, int port, String filename) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        SocketAddress remoteAddress = new InetSocketAddress(address, port);

        File yourFile = new File(filename);
        FileInputStream oFile = new FileInputStream(yourFile);

        WrqDatagram wrqDatagram = new WrqDatagram(filename, "octet");
        DatagramPacket wrqPacket = new DatagramPacket(wrqDatagram.toBytes(), wrqDatagram.toBytes().length, remoteAddress);

        socket.send(wrqPacket);

        DatagramPacket bigPack = new DatagramPacket(new byte[2048], 2048);
        socket.receive(bigPack);

        TftpDatagram datagram = TftpDatagram.makeTftpDatagram(bigPack.getData(), bigPack.getLength());
        if (!(datagram instanceof AckDatagram) || ((AckDatagram) datagram).getAcknowledgeNumber() != 0)
            throw new Exception();

        byte[] dataBody = new byte[512];
        int readLength = oFile.read(dataBody);
        while (readLength != -1) {
            DataDatagram dataDatagram = new DataDatagram((short) (((AckDatagram) datagram).getAcknowledgeNumber() + 1), Arrays.copyOf(dataBody, readLength));
            DatagramPacket dataPacket = new DatagramPacket(dataDatagram.toBytes(), dataDatagram.toBytes().length, bigPack.getSocketAddress());

            socket.send(dataPacket);

            socket.receive(bigPack);
            datagram = TftpDatagram.makeTftpDatagram(bigPack.getData(), bigPack.getLength());
            if (!(datagram instanceof AckDatagram) || ((AckDatagram) datagram).getAcknowledgeNumber() != dataDatagram.getBlockNum())
                throw new Exception();

            readLength = oFile.read(dataBody);
            System.out.println("Read: " + readLength);
            System.out.println("Ack: " + ((AckDatagram) datagram).getAcknowledgeNumber());
        }
    }
}
