package Updates;

import Model.UpdateModel;
import Utils.Logit;
import lycus.ElementsContainer;

public class ElementUpdate extends BaseUpdate {

	public ElementUpdate(UpdateModel update) {
		super(update);
	}

	@Override
	public Boolean New() {
		super.New();
		return true;
	}

	@Override
	public Boolean Update() {
		super.Update();
		ElementsContainer.getInstance().updateElements(getUpdate());
		Logit.LogCheck("Element " + getUpdate().object_id + " was removed");
		return true;
	}

	@Override
	public Boolean Delete() {
		super.Delete();
		return true;
	}
}
