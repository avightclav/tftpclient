import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import util.TftpClient;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    @Argument(index = 0, required = true, usage = "tftp destination address", metaVar = "address")
    private String address;

    @Argument(index = 1, required = true, usage = "tftp destination port", metaVar = "port")
    private int port;

    @Option(name = "--root", usage = "root path for files", metaVar = "path")
    private String rootPath = "";

    private Path rootPathPath;

    private boolean parseCommand(String command) {

        String[] parts = command.split(" ");

        if (parts[0].equalsIgnoreCase("exit"))
            return true;
        try {
            if (parts.length == 2) {
                if (parts[0].equalsIgnoreCase("get"))
                    TftpClient.getFile(address, port, Paths.get(rootPath, parts[1]).toString(), parts[1]);

                if (parts[0].equalsIgnoreCase("put"))
                    TftpClient.sendFile(address, port, Paths.get(rootPath, parts[1]).toString(), parts[1]);
            } else if (parts.length == 3) {
                if (parts[0].equalsIgnoreCase("get"))
                    TftpClient.getFile(address, port, Paths.get(rootPath, parts[2]).toString(), parts[1]);

                if (parts[0].equalsIgnoreCase("put"))
                    TftpClient.sendFile(address, port, Paths.get(rootPath, parts[1]).toString(), parts[2]);
            }
            else {
                System.out.println("?Invalid command");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void main(String[] args) throws InterruptedException {
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

        Scanner in = new Scanner(System.in);
        String s = in.nextLine();
        boolean isExit = parseCommand(s);

        while (!isExit) {
            s = in.nextLine();
            isExit = parseCommand(s);
        }
    }
}
