import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class DNSService extends Thread {
    private static final int DNS_PORT = 9999;
    private static final String[][] DNS_ENTRIES = {
        { "example.com", "192.0.2.1" },
        { "google.com", "172.217.168.238" },
        // Add more DNS entries here
    };

    private DatagramSocket dnsSocket;

    public DNSService(DatagramSocket dnsSocket) {
        this.dnsSocket = dnsSocket;
    }

    @Override
    public void run() {
        System.out.println("DNS Service is running on port " + DNS_PORT);
        byte[] receiveData = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                dnsSocket.receive(receivePacket);
                String query = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Received DNS query: " + query);

                String response = resolveDNS(query);
                byte[] sendData = response.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        receivePacket.getAddress(), receivePacket.getPort());
                dnsSocket.send(sendPacket);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String resolveDNS(String query) {
        String result = "DNS lookup failed. No matching entry found.";
        for (String[] entry : DNS_ENTRIES) {
            if (entry[0].equalsIgnoreCase(query) || entry[1].equals(query)) {
                result = "Domain: " + entry[0] + "\tIP Address: " + entry[1];
                break;
            }
        }
        return result;
    }
}
