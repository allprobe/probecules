package Model;

public class KeyUpdateModel {
	public String snmp_oid;
	public String value_type;
	public String snmp_extended;
	public String origin;
	public String value_unit;
	public String type;
	
	public String snmp_extention_name;
	
	public String http_auth;
	public String http_auth_user;
	public String http_method;
	public String http_auth_password;
	public String urls;
	public String http_deep;
	
	public Integer port;
	public String proto;
	public String port_extra;
	public String rbl;

	
	public Integer store_value_as;
	public Integer bytes;
	public Integer npings;
	public Integer timeout;
	
	public String trigger_id;
	public String trigger_severity;
	
	// Element
	public String active;
	public String ifSpeed;
	public String hostType;
	public Integer element_interval;
	
	//Discovery
//	public String discovery_trigger_unit;
//	public String discovery_type;
//	public Integer discovery_trigger_code;
//	public String discovery_trigger_x_value;
//	public String discovery_trigger_severity;
	public String discovery_type;
	public DiscoveryTrigger[] discovery_triggers;
}
