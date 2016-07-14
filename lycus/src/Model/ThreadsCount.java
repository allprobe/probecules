package Model;

public class ThreadsCount {
	public int ping;
	public int port;
	public int web;
	public int snmp;
	public int rbl;
	public int discovery;
	public int nic;
	public int disk;
	public int traceroute;

	public void ThreadCount() {
		ping = 0;
		port = 0;
		web = 0;
		snmp = 0;
		rbl = 0;
		discovery = 0;
		nic = 0;
		disk = 0;
		traceroute = 0;
	}
}
