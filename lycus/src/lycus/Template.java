package lycus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Template {
	private UUID templateId;
	private Map<String,Probe> probes;
	private boolean status;
	public Template(UUID templateId)
	{
		this.setTemplateId(templateId);	
		this.setProbes(new HashMap<String,Probe>());
		this.setStatus(true);
	}
	//#region Getters/Setters


	public UUID getTemplateId() {
		return templateId;
	}

	public void setTemplateId(UUID templateId) {
		this.templateId = templateId;
	}



	public boolean isStatus() {
		return status;
	}


	public void setStatus(boolean status) {
		this.status = status;
	}


	public Map<String,Probe> getProbes() {
		return probes;
	}


	public void setProbes(Map<String,Probe> probes) {
		this.probes = probes;
	}
	//#endregion

	public String toString()
	{
		StringBuilder s = new StringBuilder("Template "+this.getTemplateId()+":\n");
		for(Probe p:this.getProbes().values())
		{
        s.append(p.toString()).append("\n");
		}
        return s.toString();
		
	}
}
