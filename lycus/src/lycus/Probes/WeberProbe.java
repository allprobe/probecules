/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus.Probes;

import java.util.UUID;
import lycus.Model.UpdateValueModel;
import lycus.Results.WebResult;
import lycus.Utils.GeneralFunctions;
import lycus.Utils.Logit;
import lycus.Host;
import NetConnection.NetResults;
import lycus.User;

/**
 *
 * @author Roi
 */
public class WeberProbe extends BaseProbe {

	private String httpRequestType;
	private String authMethod;// none,basic,...
	private String authUsername;
	private String authPassword;
	private String url;
	private int timeout;

	WeberProbe() {
	}

	public WeberProbe(User user, String probe_id, UUID template_id, String name, long interval, float multiplier,
			boolean status, int timeout, String type, String url, String authStatus, String authUsername,
			String authPassword) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		this.httpRequestType = type;
		this.authMethod = authStatus;
		this.authUsername = authUsername;
		this.authPassword = authPassword;
		this.url = url;
		this.timeout = timeout;
	}

	public WeberProbe(User user, String probe_id, UUID template_id, String name, long interval, float multiplier,
			boolean status, int timeout, String type, String url) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		this.httpRequestType = type;
		this.authMethod = null;
		this.authUsername = null;
		this.authPassword = null;
		this.url = url;
		this.timeout = timeout;
	}

	// Getters/Setters

	/**
	 * @return the type
	 */
	public String getHttpRequestType() {
		return httpRequestType;
	}

	/**
	 * @param type
	 *            the type to set
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
	 * @param authStatus
	 *            the authStatus to set
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
	 * @param authUsername
	 *            the authUsername to set
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
	 * @param authPassword
	 *            the authPassword to set
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

	// public void updateProbeAttributes(String probeNewName, long
	// probeNewInterval, float probeNewMultiplier,
	// boolean probeNewStatus,String newUrl,String newRequestType,String
	// newAuthStatus,String newAuthUser,String newAuthPass,int newTimeout)
	// {
	// super.updateProbe(probeNewName, probeNewInterval, probeNewMultiplier,
	// probeNewStatus);
	// this.setUrl(newUrl);
	// this.setHttpRequestType(newRequestType);
	// this.setAuthStatus(newAuthStatus);
	// this.setAuthUsername(newAuthUser);
	// this.setAuthPassword(newAuthPass);
	// this.setTimeout(newTimeout);
	// }

	@Override
	public WebResult getResult(Host h) {
		if (!h.isHostStatus())
			return null;

		WebResult weberResult = NetResults.getInstanece().getWebResult(h, this);
		return weberResult;
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

	public boolean updateKeyValues(UpdateValueModel updateValue) {
		super.updateKeyValues(updateValue);
		// super.updateProbe(probeNewName, probeNewInterval, probeNewMultiplier,
		// probeNewStatus);
		if (updateValue.key.urls != null && !updateValue.key.urls.equals(getUrl()))
		{
			this.setUrl(GeneralFunctions.Base64Decode(updateValue.key.urls));
			Logit.LogCheck("Snmp data for " + getName() +  " has changed to " + updateValue.key.value_type);
		}
		
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.http_method) && !updateValue.key.http_method.equals(getHttpRequestType()))
		{
			this.setHttpRequestType(updateValue.key.http_method);
			Logit.LogCheck("Http requery type for " + getName() +  " has changed to " + updateValue.key.http_method);
		}
			
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.http_auth) && !updateValue.key.http_auth.equals(getAuthStatus()))
		{
			this.setAuthStatus(updateValue.key.http_auth);
			Logit.LogCheck("Authorization status for " + getName() +  " has changed to " + updateValue.key.http_auth);
		}
			
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.http_auth_user) && !updateValue.key.http_auth_user.equals(getAuthUsername()))
		{
			this.setAuthUsername(GeneralFunctions.Base64Decode(updateValue.key.http_auth_user));
			Logit.LogCheck("Authorization user name for " + getName() +  " has changed to " + updateValue.key.http_auth_user);
		}
			
		if (!GeneralFunctions.isNullOrEmpty(updateValue.key.http_auth_password) && !updateValue.key.http_auth_password.equals(getAuthPassword()))
		{
			this.setAuthPassword(GeneralFunctions.Base64Decode(updateValue.key.http_auth_password));
			Logit.LogCheck("Authorization password for " + getName() +  " has changed");
		}
			
		if (updateValue.key.timeout != null && updateValue.key.timeout != getTimeout())
		{
			this.setTimeout(updateValue.key.timeout);
			Logit.LogCheck("Timeout for " + getName() +  " has changed to " + updateValue.key.timeout);
		}
		

		return true;
	}
}
