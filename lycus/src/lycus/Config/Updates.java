/**
 * 
 */
package lycus.Config;

import javax.print.attribute.standard.PrinterLocation;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Model.ThreadsUpdates;
import Model.UpdateModel;
import Utils.JsonUtil;
import lycus.DAL;
import lycus.Enums.ApiAction;
import lycus.Interfaces.IDAL;
import lycus.Updates.BaseUpdate;
import lycus.Updates.UpdateFactory;

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
			JSONObject jsonObject = dal.get(ApiAction.GetThreadsUpdates);
			runUpdates(jsonObject);
		}
		
		return true;
	}

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
