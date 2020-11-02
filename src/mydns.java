/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jcuel
 */
public class mydns {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        try {
            DnsClient client = new DnsClient(args);
            client.makeRequest();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
