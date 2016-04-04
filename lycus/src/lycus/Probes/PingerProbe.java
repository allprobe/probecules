/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lycus.Probes;

import java.util.UUID;
import lycus.Model.UpdateValueModel;
import lycus.Results.BaseResult;
import lycus.Results.PingResult;
import lycus.Utils.Logit;
import lycus.Host;
import NetConnection.NetResults;
import lycus.User;

/**
 *
 * @author Roi
 */
public class PingerProbe extends BaseProbe {

	private int count;
	private int bytes;
	private int timeout;

	PingerProbe() {
	}

	public PingerProbe(User user, String probe_id, UUID template_id, String name, long interval, float multiplier,
			boolean status, int timeout, int count, int bytes) {
		super(user, probe_id, template_id, name, interval, multiplier, status);
		this.count = count;
		this.bytes = bytes;
		this.timeout = timeout;
	}

	// Getters/Setters
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return the bytes
	 */
	public int getBytes() {
		return bytes;
	}

	/**
	 * @param bytes
	 *            the bytes to set
	 */
	public void setBytes(int bytes) {
		this.bytes = bytes;
	}

	@Override
	public BaseResult getResult(Host h) {
		if (!h.isHostStatus())
			return null;

		PingResult pingerResult = NetResults.getInstanece().getPingResult(h, this);

		return pingerResult;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(super.toString());
		s.append("Num Of Pings:").append(this.getCount()).append("; ");
		s.append("Num Of Bytes:").append(this.getBytes()).append("; ");
		s.append("Timeout:").append(this.getTimeout()).append("; ");
		return s.toString();
	}

	public boolean updateKeyValues(UpdateValueModel updateValue) {
		super.updateKeyValues(updateValue);
		if (updateValue.key.npings != null && getCount() != updateValue.key.npings)
		{
			this.setCount(updateValue.key.npings);
			Logit.LogDebug("Npings count for " + getName() +  " has changed to " + updateValue.key.npings);
		}
		
		if (updateValue.key.bytes != null &&  getBytes() != updateValue.key.npings)
		{
			this.setBytes(updateValue.key.bytes);
			Logit.LogDebug("Bytes for " + getName() +  " has changed to " + updateValue.key.bytes);
		}
			
		if (updateValue.key.timeout != null && getTimeout() != updateValue.key.timeout)
		{
			this.setTimeout(updateValue.key.timeout);
			Logit.LogDebug("Timeout for" + getName() +  " has changed to " + updateValue.key.timeout);
		}
			
		return true;
	}
}
