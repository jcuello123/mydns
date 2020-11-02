/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jcuel
 */
public class DNSRecord {
    private int timeToLive, rdLength, mxPreference;
    private String name, domain;
    private byte[] queryClass;
    private QueryType queryType;
    private boolean auth;
    private int byteLength;

    public DNSRecord(boolean auth){
        this.auth = auth;
     }

    public void outputRecord() {
        switch(this.queryType) {
            case A:
                this.outputATypeRecords();
                break;
            case NS:
                this.outputNSTypeRecords();
                break;
	    default:
		break;
        }
    }

    private void outputATypeRecords() {
        System.out.println("\t" + "Name: " + this.name + "\t" + "IP: " + this.domain);
    }

    private void outputNSTypeRecords() {
        System.out.println("\t" + "Name: " + this.name + "\t" + "Name Server: " + this.domain);
    }
	
    public int getByteLength() {
		return byteLength;
	}
	
	public void setByteLength(int byteLength) {
		this.byteLength = byteLength;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	public void setRdLength(int rdLength) {
		this.rdLength = rdLength;
	}

	public void setMxPreference(int mxPreference) {
		this.mxPreference = mxPreference;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public byte[] getQueryClass() {
		return queryClass;
	}

	public void setQueryClass(byte[] queryClass) {
		this.queryClass = queryClass;
	}

	public QueryType getQueryType() {
		return queryType;
	}

	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}
}
