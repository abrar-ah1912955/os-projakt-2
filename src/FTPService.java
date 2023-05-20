

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Random;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.*;
import java.io.*;
import java.net.Socket;

public class FTPService extends Thread {
    private static final int PORT = 8888;
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String UPLOAD_DIR = USER_HOME + "/Desktop/upload/directory";
    private static final String DOWNLOAD_DIR = USER_HOME + "/Desktop/download/directory";
    private static final int MAX_FILE_SIZE = 1024 * 1024; // 1MB

    private Socket clientSocket;

    public FTPService(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started. Listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(new FTPService(clientSocket));
                clientThread.start();
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String request = reader.readLine();
            if (request.startsWith("UPLOAD")) {
                handleUpload(request, reader, writer);
            } else if (request.startsWith("DOWNLOAD")) {
                handleDownload(request, reader, writer);
            }

            reader.close();
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUpload(String request, BufferedReader reader, BufferedWriter writer) throws IOException {
        String filename = request.substring(request.indexOf(' ') + 1);
        if (filename.isEmpty()) {
            writer.write("ERROR: Invalid filename");
            writer.newLine();
            writer.flush();
            return;
        }

        if (filename.contains("/") || filename.contains("\\")) {
            writer.write("ERROR: Invalid filename");
            writer.newLine();
            writer.flush();
            return;
        }

        String uploadFilePath = UPLOAD_DIR + File.separator + filename;
        FileOutputStream fileOutputStream = new FileOutputStream(uploadFilePath);

        long fileSize = Long.parseLong(reader.readLine());
        if (fileSize > MAX_FILE_SIZE) {
            writer.write("ERROR: File size exceeds the maximum allowed limit (" + MAX_FILE_SIZE + " bytes)");
            writer.newLine();
            writer.flush();
            fileOutputStream.close();
            return;
        }

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = clientSocket.getInputStream().read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
        }

        fileOutputStream.close();
        writer.write("SUCCESS: File uploaded successfully");
        writer.newLine();
        writer.flush();
    }

    private void handleDownload(String request, BufferedReader reader, BufferedWriter writer) throws IOException {
        String filename = request.substring(request.indexOf(' ') + 1);
        if (filename.isEmpty()) {
            writer.write("ERROR: Invalid filename");
            writer.newLine();
            writer.flush();
            return;
        }

        String downloadFilePath = DOWNLOAD_DIR + File.separator + filename;
        File file = new File(downloadFilePath);

        if (!file.exists() || file.isDirectory()) {
            writer.write("ERROR: File not found");
            writer.newLine();
            writer.flush();
            return;
        }

        FileInputStream fileInputStream = new FileInputStream(file);

        writer.write(String.valueOf(file.length()));
        writer.newLine();
        writer.flush();

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            writer.write(new String(buffer, 0, bytesRead));
        }

        fileInputStream.close();
        writer.flush();
    }
}
