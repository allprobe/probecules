/**
 * 
 */
package lycus.Config;

import org.json.simple.JSONObject;
import lycus.DAL;
import lycus.Enums.ApiAction;
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
			JSONObject jsonObject = dal.get(ApiAction.GetThreadsUpdates);
			runUpdates(jsonObject);
		}
		
		return true;
	}

	private Boolean runUpdates(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
		getUpdates();
	}
}
