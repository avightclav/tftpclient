import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import tftp.TftpClient;
import tftp.sendmode.SendMode;

import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    @Argument(required = true, usage = "tftp destination address", metaVar = "address")
    private String address;

    @Argument(index = 1, required = true, usage = "tftp destination port", metaVar = "port")
    private int port;

    @Option(name = "--root", usage = "root path for files", metaVar = "path")
    private String rootPath = "";

    private SendMode sendMode = SendMode.OCTET;

    private boolean doCommand(String command) {

        String[] parts = command.split(" ");

        try {
            if (parts.length == 1) {
                if (parts[0].equalsIgnoreCase("exit"))
                    return true;
                if (parts[0].equalsIgnoreCase("octet")) {
                    sendMode = SendMode.OCTET;
                    return false;
                }
                if (parts[0].equalsIgnoreCase("netascii")) {
                    sendMode = SendMode.NETASCII;
                    return false;
                }
                System.out.println("?Invalid command");
            } else if (parts.length == 2) {
                if (parts[0].equalsIgnoreCase("get"))
                    TftpClient.getFile(address, port, sendMode, Paths.get(rootPath, parts[1]).toString(), parts[1]);
                else if (parts[0].equalsIgnoreCase("put"))
                    TftpClient.putFile(address, port, sendMode, Paths.get(rootPath, parts[1]).toString(), parts[1]);
                else
                    System.out.println("?Invalid command");
            } else if (parts.length == 3) {
                if (parts[0].equalsIgnoreCase("get"))
                    TftpClient.getFile(address, port, sendMode, Paths.get(rootPath, parts[2]).toString(), parts[1]);
                else if (parts[0].equalsIgnoreCase("put"))
                    TftpClient.putFile(address, port, sendMode, Paths.get(rootPath, parts[1]).toString(), parts[2]);
                else
                    System.out.println("?Invalid command");
            } else {
                System.out.println("?Invalid command");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void main(String[] args) {
        try {
            new Main().doMain(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java Main [--root path] address port");
            e.getParser().printUsage(System.err);
            System.err.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doMain(String[] args) throws CmdLineException {
        CmdLineParser argumentParser = new CmdLineParser(this);
        argumentParser.parseArgument(args);

        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        boolean isExit = doCommand(s);

        while (!isExit) {
            s = scanner.nextLine();
            isExit = doCommand(s);
        }
    }
}
