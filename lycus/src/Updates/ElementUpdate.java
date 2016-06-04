package Updates;

import Model.UpdateModel;
import lycus.ElementsContainer;
import lycus.RunnableProbeContainer;

public class ElementUpdate extends BaseUpdate {

	public ElementUpdate(UpdateModel update) {
		super(update);
	
	}

	@Override
	public Boolean New()
	{
		super.New();
		return true;
	}
	
	@Override
	public Boolean Update()
	{
		super.Update();
		
//		RunnableProbeContainer.getInstanece().get(getUpdate().elements)
//		ElementsContainer.getInstance()
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
		super.Delete();
		
		
		return true;
	}
}
