package Updates;

import GlobalConstants.Constants;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import DAL.DAL;
import GlobalConstants.Enums;
import Interfaces.IDAL;
import Model.ThreadsUpdates;
import Model.UpdateModel;
import Utils.JsonUtil;
import Utils.Logit;

/**
 * @author orenharari
 *
 */
public class Updates implements Runnable {

	public Boolean getUpdates() {
		JSONObject jsonObject = null;
		try {
			IDAL dal = DAL.getInstanece();
			if (dal == null) {
				return false;
			} else {
				Object json = DAL.getInstanece().get(Enums.ApiAction.GetThreadsUpdates);
				if (json == null)
					return false;
				jsonObject = (JSONObject) json;
				runUpdates(jsonObject);
			}

			return true;
		} catch (Exception e) {
			Logit.LogError("Updates - getUpdates()", "Update thread was aborted the json: " + jsonObject);
			return false;
		}
	}

	static Logger log = Logger.getLogger(Updates.class.getName());

	private Boolean runUpdates(JSONObject jsonObject) {

		try {
			ThreadsUpdates threadsUpdates = (ThreadsUpdates) JsonUtil.ToObject(jsonObject, ThreadsUpdates.class);
			if (threadsUpdates.threads_updates.length < 1)
				return false;

			for (UpdateModel update : threadsUpdates.threads_updates) {
				Logit.LogCheck("updating update from type: "+update.update_type);
				if(update.update_type == Constants.updateSnmp)
					Logit.LogCheck("Updating snmp collector");
				BaseUpdate baseUpdate = UpdateFactory.getUpdate(update);
				try {
					baseUpdate.Run();
				} catch (Exception ex) {
					Logit.LogError("Updates - runUpdates0()", "Failed to run update " + "\n" + jsonObject, ex);
				}
			}
		} catch (Exception ex) {
			Logit.LogError("Updates - runUpdates1()", "Failed to retrieve updates " + "\n" + jsonObject, ex);
		}

		return true;
	}

	@Override
	public void run() {
		Logit.LogCheck("Retrieving updates from server...");
		getUpdates();
	}
}
