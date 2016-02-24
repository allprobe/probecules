/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus.Probes;

import java.util.ArrayList;
import java.util.UUID;

import Model.KeyUpdateModel;
import lycus.GeneralFunctions;
import lycus.Host;
import lycus.Net;
import lycus.User;

/**
 *
 * @author Roi
 */
public class WeberProbe extends Probe {

    private String httpRequestType;
    private String authMethod;//none,basic,...
    private String authUsername;
    private String authPassword;
    private String url;
    private int timeout;

    WeberProbe() {
    }

    public WeberProbe(User user, String probe_id,UUID template_id,String name,long interval, float multiplier,boolean status, int timeout, String type,String url, String authStatus, String authUsername, String authPassword) {
        super(user,probe_id,template_id,name,interval,multiplier,status);
        this.httpRequestType = type;
        this.authMethod = authStatus;
        this.authUsername = authUsername;
     this.authPassword = authPassword;
     this.url=url;
     this.timeout=timeout;
    }
    
    public WeberProbe(User user, String probe_id,UUID template_id,String name,long interval, float multiplier,boolean status, int timeout, String type,String url) {
        super(user,probe_id,template_id,name,interval,multiplier,status);
        this.httpRequestType = type;
        this.authMethod = null;
        this.authUsername = null;
     this.authPassword = null;
     this.url=url;
     this.timeout=timeout;
    }

    
 // Getters/Setters

    /**
     * @return the type
     */
    public String getHttpRequestType() {
        return httpRequestType;
    }

    /**
     * @param type the type to set
     */
    public void setHttpRequestType(String type) {
        this.httpRequestType = type;
    }

    /**
     * @return the authStatus
     */
    public String getAuthStatus() {
        return authMethod;
    }

    /**
     * @param authStatus the authStatus to set
     */
    public void setAuthStatus(String authStatus) {
        this.authMethod = authStatus;
    }

    /**
     * @return the authUsername
     */
    public String getAuthUsername() {
        return authUsername;
    }

    /**
     * @param authUsername the authUsername to set
     */
    public void setAuthUsername(String authUsername) {
        this.authUsername = authUsername;
    }

    /**
     * @return the authPassword
     */
    public String getAuthPassword() {
        return authPassword;
    }

    /**
     * @param authPassword the authPassword to set
     */
    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
     * @return the authCredentials
     */
    
	
//	public void updateProbeAttributes(String probeNewName, long probeNewInterval, float probeNewMultiplier,
//			boolean probeNewStatus,String newUrl,String newRequestType,String newAuthStatus,String newAuthUser,String newAuthPass,int newTimeout)
//    {
//    	super.updateProbe(probeNewName, probeNewInterval, probeNewMultiplier, probeNewStatus);
//    	this.setUrl(newUrl);
//    	this.setHttpRequestType(newRequestType);
//    	this.setAuthStatus(newAuthStatus);
//    	this.setAuthUsername(newAuthUser);
//    	this.setAuthPassword(newAuthPass);
//    	this.setTimeout(newTimeout);
//    }
	
	@Override
    public ArrayList<Object> Check(Host h)
    {
		if (!h.isHostStatus())
			return null;
		
		String rpStr = h.getHostId().toString()+"@"+this.getProbe_id();
		if (rpStr.contains(
				"d934aa3b-f703-4d4b-99c6-66b470c782f2"))
			System.out.println("BREAKPOINT - RunnableWeberProbeResults");
		ArrayList<Object> results=Net.Weber(this.getUrl(), this.getHttpRequestType(), this.getAuthUsername(), this.getAuthPassword(), this.getTimeout());
    	return results;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
        s.append("Http Method:").append(this.getHttpRequestType()).append("; ");
        s.append("Auth Type:").append(this.getAuthStatus()).append("; ");
        s.append("Auth UserName:").append(this.getAuthUsername()).append("; ");
        s.append("Auth Password:").append(this.getAuthPassword()).append("; ");
        s.append("Timeout:").append(this.getTimeout()).append("; ");
        return s.toString();
    }
    
    public boolean updateKeyValues(KeyUpdateModel key)
	{
		super.updateKeyValues(key);
//		super.updateProbe(probeNewName, probeNewInterval, probeNewMultiplier, probeNewStatus);
		if (key.urls != null)
			this.setUrl(GeneralFunctions.Base64Decode(key.urls));
		if (!GeneralFunctions.isNullOrEmpty(key.http_method))
			this.setHttpRequestType(key.http_method);
		if (!GeneralFunctions.isNullOrEmpty(key.http_auth))
			this.setAuthStatus(key.http_auth);
		if (!GeneralFunctions.isNullOrEmpty(key.http_auth_user))
			this.setAuthUsername(GeneralFunctions.Base64Decode(key.http_auth_user));
		if (!GeneralFunctions.isNullOrEmpty(key.http_auth_password))
			this.setAuthPassword(GeneralFunctions.Base64Decode(key.http_auth_password));
		if (key.timeout != null)
			this.setTimeout(key.timeout);
    	
		return true;
	}
}
