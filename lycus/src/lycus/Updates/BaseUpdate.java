package lycus.Updates;

import java.util.UUID;

import GlobalConstants.Constants;
import GlobalConstants.Enums;
import GlobalConstants.Enums.Action;
import Model.UpdateModel;
import lycus.Host;
import lycus.User;
import lycus.UsersManager;
import lycus.Interfaces.IUpdate;
import lycus.Probes.Probe;

public abstract class BaseUpdate implements IUpdate{

	protected Enums.Action action;
	private UpdateModel update;
	private User user;
	
	public BaseUpdate(UpdateModel update) {
		this.update = update;
		
		switch (update.update_type)
		{
			case Constants.newProbe:
			case Constants.newTrigger:
				action = Action.New;
				break;
			case Constants.updateBucket:
			case Constants.updateDiscovery:
			case Constants.updateElement:
			case Constants.updateHost:
			case Constants.updateProbe:
			case Constants.updateSnmp:
			case Constants.updateTemplate:	
			case Constants.updateTrigger:
				action = Action.Update;
				break;
			case Constants.deleteBucket:
			case Constants.deleteDiscovery:
			case Constants.deleteHost:
			case Constants.deleteProbe:
			case Constants.deleteSnmp:
			case Constants.deleteTemplate:
			case Constants.deleteTrigger:	
				action = Action.Delete;
				break;
		}
		
		user = UsersManager.getUser(UUID.fromString(getUpdate().user_id));
		if (user == null)
		{
			user = new User(UUID.fromString(getUpdate().user_id));
		}
	}

	@Override
	public Boolean Run() {
		if (action ==  Action.New)
			return New();
		else if (action ==  Action.Update)
			return Update();
		else if (action ==  Action.Delete)
			return Delete();
		
		return true;
	} 
	
	@Override
	public Boolean New() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Boolean Update() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Boolean Delete() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public UpdateModel getUpdate(){
		return this.update;
	}

	public User getUser() {
		return user;
	}

}
