import java.net.*;

public class DnsClient {

    public QueryType queryType = QueryType.A;
    public int MAX_DNS_PACKET_SIZE = 512;
    private byte[] server = new byte[4];
    private String name;
    private int port = 53;

    public DnsClient(String args[]) {
        try {
            this.parseInputArguments(args);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("ERROR\tIncorrect input syntax: Please check arguments and try again.");
        }
        if (server == null || name == null) {
            throw new IllegalArgumentException("ERROR\tIncorrect input syntax: Server IP and domain name must be provided.");
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

            //Send packet and time response
            socket.send(requestPacket);
            socket.receive(responsePacket);
            socket.close();
         
            DnsResponse response = new DnsResponse(responsePacket.getData(), requestBytes.length, queryType);
            
            
            AnswersAndIP aaip = response.outputResponse();
            if (aaip.answers == 0){
                System.out.println("----------------------------------------------------------------");
                String[] name_and_ip = {name, aaip.IP};
                parseInputArguments(name_and_ip);
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

    private void parseInputArguments(String args[]) {
        name = args[0];
        String[] addressComponents = args[1].split("\\.");
        for (int i = 0; i < addressComponents.length; i++) {
            int ipValue = Integer.parseInt(addressComponents[i]);
            if (ipValue < 0 || ipValue > 255) {
                throw new NumberFormatException("ERROR\tIncorrect input syntax: IP Address numbers must be between 0 and 255, inclusive.");
            }
            server[i] = (byte) ipValue;
        }
    }

}
