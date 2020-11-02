/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jcuel
 */
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class DnsResponse{
    private byte[] response;
    private boolean AA;
    private int ANCount, NSCount, ARCount;
    private DNSRecord[] answerRecords;
    private DNSRecord[] nameServerRecords;
    private DNSRecord[] additionalRecords;
    private QueryType queryType;

    public DnsResponse(byte[] response, int requestSize, QueryType queryType) {
        this.response = response;
        this.queryType = queryType;

        this.parseHeader();

        answerRecords = new DNSRecord[ANCount];
        int offSet = requestSize;
        for(int i = 0; i < ANCount; i ++){
            answerRecords[i] = this.parseAnswer(offSet);
            offSet += answerRecords[i].getByteLength();
        }

        //ns count
        nameServerRecords=  new DNSRecord[NSCount];
        for(int i = 0; i < NSCount; i++){
            nameServerRecords[i] = this.parseAnswer(offSet);
            offSet += nameServerRecords[i].getByteLength();
        }

        additionalRecords = new DNSRecord[ARCount];
        for(int i = 0; i < ARCount; i++){
                additionalRecords[i] = this.parseAnswer(offSet);
                offSet += additionalRecords[i].getByteLength();
        }
    }

    public AnswersAndIP outputResponse() {
        AnswersAndIP aaip = new AnswersAndIP(ANCount, "");
        
        System.out.println("Reply received. Content overview:");
        System.out.println("\t" + ANCount + " Answers.");
        System.out.println("\t" + NSCount + " Intermediate Name Servers");
        System.out.println("\t" + ARCount + " Additional Information Records.");

        System.out.println("Answers section:");
        for (DNSRecord record : answerRecords){
            record.outputRecord();	
        }
        
        System.out.println("Authoritive Section:");
        for (DNSRecord record : nameServerRecords){
            record.outputRecord();
        }

        System.out.println("Additional Information Section:");
        for (DNSRecord record : additionalRecords){
            if (aaip.IP.equals("")){
                aaip.IP = record.getDomain();
            }
            record.outputRecord();
        }
        
        return aaip;
    }
	
    private void parseHeader(){
        //AA
        this.AA = getBit(response[2], 2) == 1;

        //ANCount
        byte[] ANCount = { response[6], response[7] };
        ByteBuffer wrapped = ByteBuffer.wrap(ANCount);
        this.ANCount = wrapped.getShort();

        //NSCount
        byte[] NSCount = { response[8], response[9] };
        wrapped = ByteBuffer.wrap(NSCount);
        this.NSCount = wrapped.getShort();

        //ARCount
        byte[] ARCount = { response[10], response[11] };
        wrapped = ByteBuffer.wrap(ARCount);
        this.ARCount = wrapped.getShort();
    }

    private DNSRecord parseAnswer(int index){
    	DNSRecord result = new DNSRecord(this.AA);
    	
        String domain = "";
        int countByte = index;

        rDataEntry domainResult = getDomainFromIndex(countByte);
        countByte += domainResult.getBytes();
        domain = domainResult.getDomain();
        
        //Name
        result.setName(domain);

        //TYPE
        byte[] ans_type = new byte[2];
        ans_type[0] = response[countByte];
        ans_type[1] = response[countByte + 1];
        
        result.setQueryType(getQTYPEFromByteArray(ans_type));

        countByte += 2;
        //CLASS
        byte[] ans_class = new byte[2];
        ans_class[0] = response[countByte];
        ans_class[1] = response[countByte + 1];
        if (ans_class[0] != 0 && ans_class[1] != 1) {
            throw new RuntimeException(("ERROR\tThe class field in the response answer is not 1"));
        }
        result.setQueryClass(ans_class);

        countByte +=2;
        //TTL
        byte[] TTL = { response[countByte], response[countByte + 1], response[countByte + 2], response[countByte + 3] };
        ByteBuffer wrapped = ByteBuffer.wrap(TTL);
        result.setTimeToLive(wrapped.getInt());

        countByte +=4;
        //RDLength
        byte[] RDLength = { response[countByte], response[countByte + 1] };
        wrapped = ByteBuffer.wrap(RDLength);
        int rdLength = wrapped.getShort();
        result.setRdLength(rdLength);

        countByte +=2;
        switch (result.getQueryType()) {
            case A:
                result.setDomain(parseATypeRDATA(rdLength, countByte));
                break;
            case NS:
                result.setDomain(parseNSTypeRDATA(rdLength, countByte));
                break;
            case OTHER:
            	break;
        }
        result.setByteLength(countByte + rdLength - index);
        return result;
    }

    private String parseATypeRDATA(int rdLength, int countByte) {
        String address = "";
        byte[] byteAddress= { response[countByte], response[countByte + 1], response[countByte + 2], response[countByte + 3] };
        try {
            InetAddress inetaddress = InetAddress.getByAddress(byteAddress);
            address = inetaddress.toString().substring(1);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return address;
        
    }

    private String parseNSTypeRDATA(int rdLength, int countByte) {
        rDataEntry result = getDomainFromIndex(countByte);
        String nameServer = result.getDomain();
    	
    	return nameServer;
    }

    private rDataEntry getDomainFromIndex(int index){
    	rDataEntry result = new rDataEntry();
    	int wordSize = response[index];
    	String domain = "";
    	boolean start = true;
    	int count = 0;
    	while(wordSize != 0){
            if (!start){
                domain += ".";
            }
            if ((wordSize & 0xC0) == (int) 0xC0) {
                byte[] offset = { (byte) (response[index] & 0x3F), response[index + 1] };
                ByteBuffer wrapped = ByteBuffer.wrap(offset);
                domain += getDomainFromIndex(wrapped.getShort()).getDomain();
                index += 2;
                count +=2;
                wordSize = 0;
            }else{
                domain += getWordFromIndex(index);
                index += wordSize + 1;
                count += wordSize + 1;
                wordSize = response[index];
            }
            start = false; 
    	}
        
    	result.setDomain(domain);
    	result.setBytes(count);
    	return result;
    }
    private String getWordFromIndex(int index){
    	String word = "";
    	int wordSize = response[index];
    	for(int i =0; i < wordSize; i++){
            word += (char) response[index + i + 1];
        }
    	return word;
    }

    private int getBit(byte b, int position) {
    	return (b >> position) & 1;
    }

    private QueryType getQTYPEFromByteArray(byte[] qType) {
        if (qType[0] == 0) {
            if (qType[1] == 1) {
                return QueryType.A;
            } else if (qType[1] == 2) {
                return QueryType.NS;
            }else {
            	return QueryType.OTHER;
            }
        } else {
        	return QueryType.OTHER;
        }
    }
}
