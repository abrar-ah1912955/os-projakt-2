import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    private static final int FTP_PORT = 8888;
    private static final int DNS_PORT = 9999;

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(FTP_PORT);
            DatagramSocket dnsSocket = null;

            System.out.println("The server is waiting for the client to connect at port number: " + server.getLocalPort());

            try {
                dnsSocket = new DatagramSocket(DNS_PORT);
            } catch (SocketException e) {
                System.out.println("Failed to create DatagramSocket on port " + DNS_PORT);
                e.printStackTrace();
                return;
            }

            while (true) {
                Socket client = server.accept();

                FTPService ftpServiceThread = new FTPService(client);
                ftpServiceThread.start();

//                DNSService dnsServiceThread = new DNSService(dnsSocket);
//                dnsServiceThread.start();
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}
