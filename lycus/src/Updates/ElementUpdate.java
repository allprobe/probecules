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
		String runnableProbeId = Utils.GeneralFunctions.getRunnableProbeId(getUpdate().template_id, getUpdate().host_id, getUpdate().probe_id);
	    RunnableProbe runnableProbe = RunnableProbeContainer.getInstanece().get(runnableProbeId);
	    DiscoveryElementType elementType = null;
	    
	    for (ElementModel element : getUpdate().elements)
	    {
	    	if (element.ifSpeed != null)
	    		elementType = elementType.bw;
	    	else if(element.hrStorageAllocationUnits != null)
	    		elementType = elementType.ds;
	    			
	    	BaseElement element1 = ElementsContainer.getInstance().getElement(runnableProbeId, element.name, elementType);
	    	element1.setActive(element.active);
	    }
	
	    
		
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
