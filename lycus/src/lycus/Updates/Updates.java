/**
 * 
 */
package lycus.Updates;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import lycus.GlobalConstants.Enums;
import lycus.GlobalConstants.Enums.ApiAction;
import lycus.Model.ThreadsUpdates;
import lycus.Model.UpdateModel;
import lycus.Utils.JsonUtil;
import lycus.DAL.ApiInterface;
import lycus.DAL.DAL;
import lycus.Interfaces.IDAL;

/**
 * @author orenharari
 *
 */
public class Updates implements Runnable {

	public Boolean getUpdates() {
		IDAL dal = DAL.getInstanece();
		if (dal == null)
		{
			return false;
		}
		else
		{
//			JSONObject jsonObject = dal.get(ApiAction.DevGetThreadsUpdates);
			JSONObject jsonObject = (JSONObject)ApiInterface.executeRequest(Enums.ApiAction.GetThreadsUpdates, "GET", null);
//			JSONObject jsonObject = dal.get(ApiAction.GetThreadsUpdates);
			runUpdates(jsonObject);
		}
		
		return true;
	}
	static Logger log = Logger.getLogger(Updates.class.getName());
	
		
	private Boolean runUpdates(JSONObject jsonObject) {
		try
		{
			ThreadsUpdates threadsUpdates = (ThreadsUpdates)JsonUtil.ToObject(jsonObject, ThreadsUpdates.class);
			if (threadsUpdates.threads_updates.length < 1)
				return false;
			
			for (UpdateModel update : threadsUpdates.threads_updates)
			{
				BaseUpdate baseUpdate = UpdateFactory.getUpdate(update);
				try
				{
					baseUpdate.Run();
				}
				catch (Exception ex)
				{
					System.out.println(ex.getMessage());
				}
			}
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	
		return true;
	}

	@Override
	public void run() {
		getUpdates();
	}
}
