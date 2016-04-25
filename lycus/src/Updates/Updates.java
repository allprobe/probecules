/**
 * 
 */
package Updates;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import DAL.ApiInterface;
import DAL.DAL;
import GlobalConstants.Enums;
import Interfaces.IDAL;
import Model.ThreadsUpdates;
import Model.UpdateModel;
import Utils.JsonUtil;

/**
 * @author orenharari
 *
 */
public class Updates implements Runnable {

	public Boolean getUpdates() {
		IDAL dal = DAL.getInstanece();
		if (dal == null) {
			return false;
		} else {
			// JSONObject jsonObject = dal.get(ApiAction.DevGetThreadsUpdates);

			Object json = DAL.getInstanece().get(Enums.ApiAction.GetThreadsUpdates);
			JSONObject jsonObject = (JSONObject) json;
			// JSONObject jsonObject = dal.get(ApiAction.GetThreadsUpdates);
			runUpdates(jsonObject);
		}

		return true;
	}

	static Logger log = Logger.getLogger(Updates.class.getName());

	private Boolean runUpdates(JSONObject jsonObject) {
		try {
			ThreadsUpdates threadsUpdates = (ThreadsUpdates) JsonUtil.ToObject(jsonObject, ThreadsUpdates.class);
			if (threadsUpdates.threads_updates.length < 1)
				return false;

			for (UpdateModel update : threadsUpdates.threads_updates) {
				BaseUpdate baseUpdate = UpdateFactory.getUpdate(update);
				try {
					baseUpdate.Run();
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		return true;
	}

	@Override
	public void run() {
		getUpdates();
	}
}
