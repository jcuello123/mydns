/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jcuel
 */
import java.net.*;

public class DnsClient {

    public QueryType queryType = QueryType.A;
    private byte[] server = new byte[4];
    private String name;
    private int port = 53;

    public DnsClient(String args[]) {
        try {
            this.parseServer(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (server == null || name == null) {
            throw new IllegalArgumentException("Server IP and domain name must be provided.");
        }
    }

    public void makeRequest(String address) {
        try {
            System.out.println("----------------------------------------------------------------");
            System.out.println("DNS server to query " + address);
            //Create Datagram socket and request object(s)
            DatagramSocket socket = new DatagramSocket();
            InetAddress inetaddress = InetAddress.getByAddress(server);
            DnsRequest request = new DnsRequest(name, queryType);

            byte[] requestBytes = request.getRequest();

            byte[] responseBytes = new byte[1024];

            DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, inetaddress, port);
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);

            //Send packet
            socket.send(requestPacket);
            socket.receive(responsePacket);
            socket.close();
         
            DnsResponse response = new DnsResponse(responsePacket.getData(), requestBytes.length, queryType);
            
            AnswersAndIP aaip = response.outputResponse();
            if (aaip.answers == 0){
                System.out.println("----------------------------------------------------------------");
                String[] name_and_ip = {name, aaip.IP};
                parseServer(name_and_ip);
                makeRequest(aaip.IP);
            }
            else {
                System.out.println("----------------------------------------------------------------");
            }
        } catch (SocketException e) {
            System.out.println("ERROR\tCould not create socket");
        } catch (UnknownHostException e ) {
            System.out.println("ERROR\tUnknown host");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void parseServer(String args[]) {
        name = args[0];
        String[] addressComponents = args[1].split("\\.");
        for (int i = 0; i < addressComponents.length; i++) {
            int ipValue = Integer.parseInt(addressComponents[i]);
            if (ipValue < 0 || ipValue > 255) {
                throw new NumberFormatException();
            }
            server[i] = (byte) ipValue;
        }
    }

}
