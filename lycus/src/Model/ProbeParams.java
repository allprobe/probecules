package Model;

public class ProbeParams {
	public String user_id;
	public String probe_id;
	public String template_id;
	public String name;
	public Long interval;
	public Float multiplier;
	public boolean is_active;
	public String type;
	public Integer npings;
	public Integer bytes;
	public Integer timeout;
	public String protocol;
	public Integer port;
	public String send_type;
	public String port_extra;
	public String rbl;
	public String oid;
	public String snmp_datatype;
	public String snmp_unit;
	public Integer snmp_store_as;
	public String http_request;
	public String http_auth;
	public String http_auth_username;
	public String http_auth_password;
	public String url;
	public Integer element_interval;
	public String trigger_id;;
	
	// Discovery
	public String discovery_type;
	public DiscoveryTrigger[] discovery_triggers;

	


}