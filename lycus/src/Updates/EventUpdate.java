package Updates;

import Model.UpdateModel;
import Utils.Logit;
import lycus.ResultsContainer;

public class EventUpdate extends BaseUpdate{
	public EventUpdate(UpdateModel update) {
		super(update);
		// TODO TemplateUpdate()
	}

	@Override
	public Boolean New() {
		super.New();
		return true;
	}
	
	public Boolean Update() {
		super.Update();
		return true;
	}
	
	public Boolean Delete() {
		super.Delete();
		ResultsContainer resultsContainer = ResultsContainer.getInstance();
		resultsContainer.removeEventsById(getUpdate().object_id);
		Logit.LogCheck("Event: " + getUpdate().object_id + " was deleted");
		return true;
	}
}
