package Collectors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import GlobalConstants.Enums;

public class SnmpCollector extends BaseCollector{
	private String communityName;
	private Integer version;
	private String sec;     //noAuthNoPriv/authNoPriv/authPriv
	private String userName;
	private String authPass;
	private String algo;    //MD5/SHA1
	private String cryptPass;
	private String cryptType;//DES/AES
	private int port;

	// constructor for version 1,2
	public SnmpCollector(String id,String name,
			String commName, int version,int port,int timeout,boolean status) {
		this.setId(id);
		this.setName(name);
		this.setCommunityName(commName);
		this.setVersion(version);
		this.setPort(port);
		this.setTimeout(timeout);
		this.setSec(null);
		this.setUserName(null);
		this.setAuthPass(null);
		this.setAlgo(null);
		this.setCryptPass(null);
		this.setCryptType(null);
		this.setActive(true);
		this.setType(Enums.CollectorType.Snmp);
	}

	// constructor for version 3
	public SnmpCollector(String id,String name, int version,int port, String sec, String userName,
			String authPass, String algo,String cryptPass,String cryptType,int timeout,boolean status) {
		this.setId(id);
		this.setName(name);
		this.setType(Enums.CollectorType.Snmp);
		this.setVersion(version);
		this.setPort(port);
		this.setTimeout(timeout);
		this.setSec(sec);
		this.setUserName(userName);
		this.setActive(status);
		if(sec.equals("noAuthNoPriv"))
		{
			this.setAuthPass(null);
			this.setAlgo(null);
			this.setCryptPass(null);
			this.setCryptType(null);
		}
		else if(sec.equals("authNoPriv"))
		{
		this.setAuthPass(authPass);
		this.setAlgo(algo);
		this.setCryptPass(null);
		this.setCryptType(null);
		}
		else if(sec.equals("authPriv"))
		{
			this.setAuthPass(authPass);
			this.setAlgo(algo);
		this.setCryptPass(cryptPass);
		this.setCryptType(cryptType);
		}
	}

	public String getCommunityName() {
		return communityName;
	}

	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getSec() {
		return sec;
	}

	public void setSec(String sec) {
		this.sec = sec;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getAuthPass() {
		return authPass;
	}

	public void setAuthPass(String authPass) {
		this.authPass = authPass;
	}

	public String getAlgo() {
		return algo;
	}

	public void setAlgo(String algo) {
		this.algo = algo;
	}

	public String getCryptPass() {
		return cryptPass;
	}

	public void setCryptPass(String cryptPass) {
		this.cryptPass = cryptPass;
	}

	public String getCryptType() {
		return cryptType;
	}

	public void setCryptType(String cryptType) {
		this.cryptType = cryptType;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public static Map<UUID, SnmpCollector> BuildMap() {
		return new HashMap<UUID, SnmpCollector>();
	}
	
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
        s.append("Snmp Template ID:").append(this.getId().toString()).append("; ");
        s.append("Name:").append(this.getName()).append("; ");
        s.append("Community:").append(this.getCommunityName()).append("; ");
        s.append("Ver:").append(this.getVersion()).append("; ");
        s.append("Sec:").append(this.getSec()).append("; ");
        s.append("User:").append(this.getUserName()).append("; ");
        s.append("Algo:").append(this.getAlgo()).append("; ");
        s.append("Port:").append(this.getPort()).append("; ");
        s.append("Timeout:").append(this.getTimeout()).append("; ");
        return s.toString();
		
	}

}
