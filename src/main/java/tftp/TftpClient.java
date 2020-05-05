package tftp;

import tftp.exception.ErrorDatagramException;
import tftp.packet.*;
import tftp.sendmode.SendMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class TftpClient {

    final static int DATAGRAM_MAX_SIZE = 65508;
    final static int MAX_ATTEMPTS = 4;
    final static int TIMEOUT_IN_SECONDS = 3;
    private final static int SOCKET_TIMEOUT = TIMEOUT_IN_SECONDS * 1000;

    public static void getFile(String address, int port, SendMode sendMode, String localFilename, String remoteFilename)
            throws Exception {

        final SocketAddress remoteAddress = new InetSocketAddress(address, port);
        final File tempFile = File.createTempFile("tftp-data-", ".temp");
        tempFile.deleteOnExit();

        try (final DatagramSocket socket = new DatagramSocket();
             final FileOutputStream tempFileOutputStream = new FileOutputStream(tempFile)) {

            socket.setSoTimeout(SOCKET_TIMEOUT);

            final RrqPacket rrqPacket = new RrqPacket(remoteFilename, sendMode);
            final DatagramPacket rrqDatagram = new DatagramPacket(rrqPacket.toBytes(), rrqPacket.getLength(), remoteAddress);

            final DatagramPacket incomingDatagram = new DatagramPacket(new byte[DATAGRAM_MAX_SIZE], DATAGRAM_MAX_SIZE);
            TftpPacket inputPacket = null;
            DataPacket lastDataPacket;

            int attempts = 0;
            boolean gotResponse = false;
            while (!gotResponse && attempts++ < MAX_ATTEMPTS) {
                socket.send(rrqDatagram);

                try {
                    socket.receive(incomingDatagram);

                    inputPacket = TftpPacket.makeTftpPacket(incomingDatagram.getData(), incomingDatagram.getLength());
                    if (inputPacket instanceof DataPacket && ((DataPacket) inputPacket).getBlockNum() == 1) {
                        gotResponse = true;
                    } else if (inputPacket instanceof ErrorPacket)
                        throw new ErrorDatagramException("Received error message", (ErrorPacket) inputPacket);
                    else
                        System.err.println("Received non-data packet, skipping it...");

                } catch (SocketTimeoutException ignored) {
                }
            }
            if (attempts == MAX_ATTEMPTS + 1)
                throw new SocketTimeoutException();

            lastDataPacket = (DataPacket) inputPacket;
            tempFileOutputStream.write(lastDataPacket.getData());
            long realDataLength = lastDataPacket.getData().length;

            final SocketAddress sessionAddress = incomingDatagram.getSocketAddress();

            AckPacket lastAckPacket = new AckPacket(lastDataPacket.getBlockNum());
            DatagramPacket ackDatagram = new DatagramPacket(lastAckPacket.toBytes(), lastAckPacket.getLength(), sessionAddress);
            socket.send(ackDatagram);

            socket.setSoTimeout(SOCKET_TIMEOUT * MAX_ATTEMPTS);
            while (lastDataPacket.getData().length == 512) {
                boolean shouldAck = false;
                socket.receive(incomingDatagram);

                if (sessionAddress.equals(incomingDatagram.getSocketAddress())) {
                    inputPacket = TftpPacket.makeTftpPacket(incomingDatagram.getData(), incomingDatagram.getLength());
                    if (inputPacket instanceof DataPacket) {
                        lastDataPacket = (DataPacket) inputPacket;
                        if (lastDataPacket.getBlockNum() == (short) (lastAckPacket.getAcknowledgeNumber() + 1)) {
                            shouldAck = true;
                            tempFileOutputStream.write(lastDataPacket.getData());
                            realDataLength += lastDataPacket.getData().length;
                        } else if (lastDataPacket.getBlockNum() != lastAckPacket.getAcknowledgeNumber()) {
                            shouldAck = true;
                        }
                    } else if (inputPacket instanceof ErrorPacket)
                        throw new ErrorDatagramException("Received error message", (ErrorPacket) inputPacket);
                    else
                        System.err.println("Received non-data packet, skipping it...");
                } else { // if rrq has been duplicated skip it
                    final ErrorPacket errorPacket = new ErrorPacket(ErrorPacket.TFTP_ERROR_UNKNOWN_TID, "This TID already in use");
                    final DatagramPacket errorDatagram = new DatagramPacket(errorPacket.toBytes(),
                            errorPacket.getLength(), incomingDatagram.getSocketAddress());
                    socket.send(errorDatagram);
                }

                if (shouldAck) {
                    lastAckPacket = new AckPacket(lastDataPacket.getBlockNum());
                    ackDatagram = new DatagramPacket(lastAckPacket.toBytes(), lastAckPacket.getLength(), sessionAddress);
                    socket.send(ackDatagram);
                }

                System.out.print("\rFile: " + localFilename + " got " + realDataLength + " bytes");
            }

            socket.setSoTimeout(SOCKET_TIMEOUT);
            attempts = 0;
            gotResponse = true;
            while (gotResponse && attempts++ < MAX_ATTEMPTS) {
                try {
                    socket.receive(incomingDatagram);

                    final TftpPacket receivedPacked = TftpPacket.makeTftpPacket(incomingDatagram.getData(), incomingDatagram.getLength());
                    if ((receivedPacked instanceof DataPacket) && (((DataPacket) receivedPacked).getBlockNum()) == lastDataPacket.getBlockNum()) {
                        final AckPacket ackPacket = new AckPacket(lastDataPacket.getBlockNum());
                        ackDatagram = new DatagramPacket(ackPacket.toBytes(), ackPacket.getLength(), sessionAddress);
                        socket.send(ackDatagram);
                    }
                } catch (SocketTimeoutException ignored) {
                    gotResponse = false;
                }
            }
            if (attempts == MAX_ATTEMPTS + 1)
                System.err.println("Server didn't get last ACK: " + Short.toUnsignedInt(lastDataPacket.getBlockNum()));

            System.out.println("\rFile: " + localFilename + " got " + realDataLength + " bytes. Finished.\n");
        }
        Files.move(Paths.get(tempFile.getAbsolutePath()), Paths.get(localFilename).toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void putFile(String address, int port, SendMode sendMode, String localFilename, String remoteFilename) throws Exception {
        final SocketAddress remoteAddress = new InetSocketAddress(address, port);
        final File localFile = new File(localFilename);

        try (final DatagramSocket socket = new DatagramSocket();
             final FileInputStream fileInputStream = new FileInputStream(localFile)) {

            socket.setSoTimeout(SOCKET_TIMEOUT);

            final WrqPacket wrqDatagram = new WrqPacket(remoteFilename, sendMode);
            final DatagramPacket wrqPacket = new DatagramPacket(wrqDatagram.toBytes(), wrqDatagram.getLength(), remoteAddress);
            final DatagramPacket incomingDatagram = new DatagramPacket(new byte[DATAGRAM_MAX_SIZE], DATAGRAM_MAX_SIZE);
            TftpPacket inputPacket = null;

            int attempts = 0;
            boolean gotResponse = false;
            while (!gotResponse && attempts++ < MAX_ATTEMPTS) {
                socket.send(wrqPacket);

                try {
                    socket.receive(incomingDatagram);

                    inputPacket = TftpPacket.makeTftpPacket(incomingDatagram.getData(), incomingDatagram.getLength());
                    if (inputPacket instanceof AckPacket && ((AckPacket) inputPacket).getAcknowledgeNumber() == 0)
                        gotResponse = true;
                    else if (inputPacket instanceof ErrorPacket)
                        throw new ErrorDatagramException("Received error message", (ErrorPacket) inputPacket);
                    else
                        System.err.println("Received non-ACK packet, skipping it...");
                } catch (SocketTimeoutException ignored) {
                }
            }
            if (attempts == MAX_ATTEMPTS + 1)
                throw new SocketTimeoutException();

            final SocketAddress sessionAddress = incomingDatagram.getSocketAddress();

            final byte[] dataBody = new byte[512];
            int readLength = fileInputStream.read(dataBody);
            long realDataLength = readLength;
            DataPacket dataPacket = new DataPacket((short) (((AckPacket) inputPacket).getAcknowledgeNumber() + 1), Arrays.copyOf(dataBody, readLength));
            DatagramPacket dataDatagram = new DatagramPacket(dataPacket.toBytes(), dataPacket.getLength(), incomingDatagram.getSocketAddress());
            socket.send(dataDatagram);

            socket.setSoTimeout(SOCKET_TIMEOUT * MAX_ATTEMPTS);
            while (readLength == 512) {
                boolean shouldSend = false;
                socket.receive(incomingDatagram);
                inputPacket = TftpPacket.makeTftpPacket(incomingDatagram.getData(), incomingDatagram.getLength());

                if (sessionAddress.equals(incomingDatagram.getSocketAddress())) {
                    if (inputPacket instanceof AckPacket) {
                        if (((AckPacket) inputPacket).getAcknowledgeNumber() == dataPacket.getBlockNum()) {
                            shouldSend = true;
                            readLength = fileInputStream.read(dataBody);
                            realDataLength += readLength;
                        } else if (((AckPacket) inputPacket).getAcknowledgeNumber() == (short) (dataPacket.getBlockNum() - 1)) {
                            shouldSend = true;
                        }
                    } else if (inputPacket instanceof ErrorPacket)
                        throw new ErrorDatagramException("Received error message", (ErrorPacket) inputPacket);
                    else
                        System.err.println("Received non-ACK packet, skipping it...");
                } else {
                    final ErrorPacket errorPacket = new ErrorPacket(ErrorPacket.TFTP_ERROR_UNKNOWN_TID, "My TID already in use");
                    final DatagramPacket errorDatagram = new DatagramPacket(errorPacket.toBytes(),
                            errorPacket.getLength(), incomingDatagram.getSocketAddress());
                    socket.send(errorDatagram);
                }

                if (shouldSend) {
                    dataPacket = new DataPacket((short) (((AckPacket) inputPacket).getAcknowledgeNumber() + 1), Arrays.copyOf(dataBody, readLength));
                    dataDatagram = new DatagramPacket(dataPacket.toBytes(), dataPacket.getLength(), incomingDatagram.getSocketAddress());
                    socket.send(dataDatagram);
                }

                System.out.print("\rFile: " + localFilename + " sent " + realDataLength + " bytes");
            }

            socket.setSoTimeout(SOCKET_TIMEOUT);
            attempts = 0;
            gotResponse = false;
            while (!gotResponse && attempts++ < MAX_ATTEMPTS) {
                boolean shouldResend = false;
                try {
                    socket.receive(incomingDatagram);
                    inputPacket = TftpPacket.makeTftpPacket(incomingDatagram.getData(), incomingDatagram.getLength());
                    if (sessionAddress.equals(incomingDatagram.getSocketAddress())) {
                        if (inputPacket instanceof AckPacket) {
                            if (((AckPacket) inputPacket).getAcknowledgeNumber() == dataPacket.getBlockNum()) {
                                gotResponse = true;
                            } else if (((AckPacket) inputPacket).getAcknowledgeNumber() == (short) (dataPacket.getBlockNum() - 1)) {
                                shouldResend = true;
                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                    shouldResend = true;
                }

                if (shouldResend)
                    socket.send(dataDatagram);
            }
            if (attempts == MAX_ATTEMPTS + 1)
                System.err.println("Didn't received last ACK: " + Short.toUnsignedInt(dataPacket.getBlockNum()));

            System.out.println("\rFile: " + localFilename + " sent " + realDataLength + " bytes. Finished.\n");
        }
    }
}