

import java.net.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.zip.*;
import java.io.*;
public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int FTP_PORT = 8888;
    private static final int DNS_PORT = 9999;

    public static void main(String[] args) {
        try {
            FTPService();
//             DNSService();
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    private static void FTPService() throws IOException {
        Socket socket = new Socket(SERVER_ADDRESS, FTP_PORT);
        System.out.println("Connected to server: " + SERVER_ADDRESS + ":" + FTP_PORT);

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter command (UPLOAD, DOWNLOAD, QUIT): ");
            String command = scanner.nextLine();

            if (command.equalsIgnoreCase("UPLOAD")) {
                System.out.print("Enter filename: ");
                String filename = scanner.nextLine();

                File file = new File(filename);
                if (!file.exists() || file.isDirectory()) {
                    System.out.println("File not found");
                    continue;
                }

                long fileSize = file.length();
                writer.write("UPLOAD " + filename);
                writer.newLine();
                writer.flush();

                writer.write(String.valueOf(fileSize));
                writer.newLine();
                writer.flush();

                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    writer.write(new String(buffer, 0, bytesRead));
                }

                fileInputStream.close();
                writer.flush();

                String response = reader.readLine();
                System.out.println(response);
            } else if (command.equalsIgnoreCase("DOWNLOAD")) {
                System.out.print("Enter filename: ");
                String filename = scanner.nextLine();

                writer.write("DOWNLOAD " + filename);
                writer.newLine();
                writer.flush();

                String fileSizeStr = reader.readLine();
                long fileSize = Long.parseLong(fileSizeStr);
                if (fileSize == 0) {
                    System.out.println("File not found");
                    continue;
                }

                FileOutputStream fileOutputStream = new FileOutputStream(filename);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while (fileSize > 0 && (bytesRead = socket.getInputStream().read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    fileSize -= bytesRead;
                }

                fileOutputStream.close();
                System.out.println("File downloaded successfully");
            } else if (command.equalsIgnoreCase("QUIT")) {
                break;
            } else {
                System.out.println("Invalid command");
            }
        }

        reader.close();
        writer.close();
        socket.close();
    }

    public static void DNSService() throws IOException {
        InetAddress serverAddress = InetAddress.getByName("localhost");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the domain name or IP address: ");
        String query = scanner.nextLine();

        DatagramSocket socket = new DatagramSocket();
        byte[] sendData = query.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, DNS_PORT);
        socket.send(sendPacket);

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);

        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.println("DNS Lookup Result:\n" + response);

        socket.close();
    }
}
