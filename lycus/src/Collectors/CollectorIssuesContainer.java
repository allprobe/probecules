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

	public CollectorIssuesContainer() {
		issuesArray = new JSONArray();
		liveIssues = new HashMap<String, HashSet<Enums.CollectorType>>();
		int count = 0;
	}

	@Override
	public synchronized void addIssue(Host host, Enums.CollectorType type, String issue, int issueState) {
//		if (host.getHostIp().contains("62.90.132.124"))
//			Logit.LogDebug("BP");
//		if (issueState == 1) {
//			if (this.liveIssues.containsKey(host.getHostId().toString())) {
//				if (this.liveIssues.get(host.getHostId().toString()).contains(type))
//					return;
//				else
//					this.liveIssues.get(host.getHostId().toString()).add(type);
//			} else {
//				HashSet<Enums.CollectorType> newIssueTable = new HashSet<Enums.CollectorType>();
//				newIssueTable.add(type);
//				this.liveIssues.put(host.getHostId().toString(), newIssueTable);
//			}
//		} else {
//			if (!this.liveIssues.containsKey(host.getHostId().toString())
//					|| !this.liveIssues.get(host.getHostId().toString()).contains(type))
//				return;
//			else {
//				this.liveIssues.get(host.getHostId().toString()).remove(type);
//				if (this.liveIssues.get(host.getHostId().toString()).size() == 0)
//					this.liveIssues.remove(host.getHostId().toString());
//			}
//		}
//		JSONObject issueObject = new JSONObject();
//		issueObject.put("user_id", host.getUserId().toString());
//		issueObject.put("host_id", host.getHostId().toString());
//		issueObject.put("type", type.toString());
//		issueObject.put("issue_info", issue);
//		issueObject.put("issue_status", issueState);
//		issueObject.put("time", System.currentTimeMillis());
//
//		count++;
//		issuesArray.add(issueObject);
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
