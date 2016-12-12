package Collectors;

import GlobalConstants.Enums;
import Interfaces.ICollectorIssuesContainer;
import lycus.Host;
import GlobalConstants.Constants;
import org.apache.bcel.classfile.Constant;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by roi on 12/11/16.
 */
public class CollectorIssuesContainer implements ICollectorIssuesContainer {

        private static CollectorIssuesContainer instance;
        private JSONArray issuesArray;
        private int count;
        private Object lock = new Object();

        public static CollectorIssuesContainer getInstance() {
            if (instance == null) {
                instance = new CollectorIssuesContainer();
            }
            return instance;
        }
    public CollectorIssuesContainer() {
        issuesArray = new JSONArray();
        int count = 0;
    }
    @Override
    public void addIssue(Host host, Enums.CollectorType type, String issue) {
        JSONObject issueObject = new JSONObject();
        issueObject.put("user_id", host.getUserId().toString());
        issueObject.put("host_id", host.getHostId().toString());
        issueObject.put("type", type.toString());
        issueObject.put("exra_info", issue);
        count++;
        synchronized (lock) {
            issuesArray.add(issueObject);
        }
    }
    public void clearAll() {
        issuesArray.clear();
        count=0;
    }

    public int length()
    {
        return count;
    }

    @Override
    public JSONObject getAllIssues() {
        synchronized (lock) {
            JSONObject issuesObject=new JSONObject();
            issuesObject.put(Constants.issues,issuesArray.toJSONString());
            clearAll();
            return issuesObject;
        }
    }
}
