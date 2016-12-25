package Collectors;

import GlobalConstants.Enums;
import Interfaces.ICollectorIssuesContainer;
import Utils.GeneralFunctions;
import Utils.Logit;
import lycus.Host;
import GlobalConstants.Constants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by roi on 12/11/16.
 */
public class CollectorIssuesContainer implements ICollectorIssuesContainer {

	private static CollectorIssuesContainer instance;
	private JSONArray issuesArray;
	private int count;
	private Object lockIssues = new Object();
	private Object lockLiveIssues = new Object();

	private HashMap<String, HashSet<Enums.CollectorType>> liveIssues;// HashMap<HostId,HashSet<CollectorType>>

	public synchronized static CollectorIssuesContainer getInstance() {
		if (instance == null) {
			instance = new CollectorIssuesContainer();
		}
		return instance;
	}

	private CollectorIssuesContainer() {
		issuesArray = new JSONArray();
		liveIssues = new HashMap<String, HashSet<Enums.CollectorType>>();
		int count = 0;
	}

	@Override
	public synchronized void addIssue(Host host, Enums.CollectorType type, String issue, int issueState) {
		if (host.getHostIp().contains("62.90.132.124"))
			Logit.LogDebug("BP");
		String hostId = host.getHostId().toString();
		if (issueState == 1) {
			if (this.liveIssues.containsKey(hostId)) {
				if (this.liveIssues.get(hostId).contains(type))
					return;
				else
					this.liveIssues.get(hostId).add(type);
			} else {
				HashSet<Enums.CollectorType> newIssueTable = new HashSet<Enums.CollectorType>();
				newIssueTable.add(type);
				this.liveIssues.put(hostId, newIssueTable);
			}
		} else {
			if (!this.liveIssues.containsKey(hostId)
					|| !this.liveIssues.get(hostId).contains(type))
				return;
			else {
				this.liveIssues.get(hostId).remove(type);
				if (this.liveIssues.get(hostId).size() == 0)
					this.liveIssues.remove(hostId);
			}
		}
		JSONObject issueObject = new JSONObject();
		String userId = "";
		try {
			userId = host.getUserId();
			if (userId != null)
				issueObject.put("user_id", userId);
		} catch (Exception e) {
			Logit.LogWarn("Error getting userId from host: " + hostId + " userId: " + userId);
			e.printStackTrace();
		}
		issueObject.put("host_id", hostId);
		issueObject.put("type", type.toString());
		issueObject.put("issue_info", issue);
		issueObject.put("issue_status", issueState);
		issueObject.put("time", System.currentTimeMillis());

		count++;
		issuesArray.add(issueObject);
	}

	public void clearAll() {

		issuesArray.clear();
		count = 0;
	}

	public int length() {
		return count;
	}

	@Override
	public synchronized JSONObject getAllIssues() {
		JSONObject issuesObject = new JSONObject();
		issuesObject.put(Constants.issues, GeneralFunctions.Base64Encode(issuesArray.toJSONString()));
		clearAll();
		return issuesObject;
	}
}
