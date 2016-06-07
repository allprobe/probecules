package Updates;

import java.util.HashMap;

import Elements.BaseElement;
import GlobalConstants.Enums.DiscoveryElementType;
import Model.ElementModel;
import Model.UpdateModel;
import lycus.ElementsContainer;
import lycus.RunnableProbe;
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
	    ElementsContainer.getInstance().updateElements(getUpdate());
	    
		
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
