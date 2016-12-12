package Interfaces;

import GlobalConstants.Enums;
import lycus.Host;
import org.json.simple.JSONObject;

/**
 * Created by roi on 12/11/16.
 */
public interface ICollectorIssuesContainer {
    void addIssue(Host host, Enums.CollectorType type, String issue);
    JSONObject getAllIssues();
}
