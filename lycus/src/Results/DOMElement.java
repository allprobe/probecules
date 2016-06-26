package Results;

public class DOMElement {
private String nameEncoded;
private long startTime;
private long time;
private long dnsTime;
private int responseStatusCode;
private long size;
private String mimeType;

public DOMElement(String nameEncoded,long startTime, long time,long dnsTime, int responseStatusCode, long size, String mimeType) {
	super();
	this.nameEncoded=nameEncoded;
	this.startTime = startTime;
	this.time = time;
	this.setDnsTime(dnsTime);
	this.responseStatusCode = responseStatusCode;
	this.size = size;
	this.mimeType = mimeType;
}

public long getStartTime() {
	return startTime;
}
public void setStartTime(long startTime) {
	this.startTime = startTime;
}
public long getTime() {
	return time;
}
public void setTime(long time) {
	this.time = time;
}
public int getResponseStatusCode() {
	return responseStatusCode;
}
public void setResponseStatusCode(int responseStatusCode) {
	this.responseStatusCode = responseStatusCode;
}
public long getSize() {
	return size;
}
public void setSize(long size) {
	this.size = size;
}
public long getDnsTime() {
	return dnsTime;
}

public void setDnsTime(long dnsTime) {
	this.dnsTime = dnsTime;
}

public String getMimeType() {
	return mimeType;
}
public void setMimeType(String mimeType) {
	this.mimeType = mimeType;
}

}
