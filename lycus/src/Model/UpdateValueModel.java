package Model;

import java.util.ArrayList;

public class UpdateValueModel
{
	public String user_id;
	public Float multiplier;
	public String name;
	public Long interval;
	public String type;
	public KeyUpdateModel key;
	public Boolean status;
	public String ip;
	public String snmp_template;
	public String bucket;
	public String notifications_group;
	
	//Trigger
	
	public String id;
	public String trigger_type;
	public String severity;
	public String xvalue_unit;
	public String probe_id;
	public ConditionUpdateModel[] conditions;
	
}

