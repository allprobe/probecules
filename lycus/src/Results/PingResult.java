package Results;

import org.json.simple.JSONArray;
import GlobalConstants.Constants;
import GlobalConstants.ProbeTypes;

public class PingResult extends BaseResult {

	private Integer packetLoss;
	private Double rtt;
	private Integer ttl;

	public PingResult(String runnableProbeId, long timestamp, int packetLoss, double rtt, int ttl) {
		super(runnableProbeId, timestamp);
		this.probeType = ProbeTypes.ICMP;
		this.packetLoss = packetLoss;
		this.rtt = rtt;
		this.ttl = ttl;
	}

	public PingResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public Integer getPacketLost() {
		return packetLoss;
	}

	public void setPacketLost(Integer packetLost) {
		this.packetLoss = packetLost;
	}

	public Double getRtt() {
		return rtt;
	}

	public void setRtt(Double rtt) {
		this.rtt = rtt;
	}

	public Integer getTtl() {
		return ttl;
	}

	public void setTtl(Integer ttl) {
		this.ttl = ttl;
	}

	@Override
	public String getName() {
		return super.getName();
	}

	// TODO: Oren ask ran what is true?
	public Boolean isActive() {
		return packetLoss < Constants.pingPacketLostMin;
	}

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		result.add(1);
		if (this.getErrorMessage().equals("")) {
			result.add(packetLoss);
			result.add(rtt);
			result.add(ttl);
		} else
			result.add(this.getErrorMessage());
		return result;
	}

	@Override
	public String toString() {
		return "PingResult{" +
				"packetLoss=" + packetLoss +
				", rtt=" + rtt +
				", ttl=" + ttl +
				'}';
	}
}
