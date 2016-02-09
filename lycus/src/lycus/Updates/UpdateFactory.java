package lycus.Updates;

import Model.UpdateModel;
import lycus.Constants;

public class UpdateFactory {
	 public static BaseUpdate getUpdate(UpdateModel update){
		 switch (update.update_type)
			{
				case Constants.newProbe:
				case Constants.updateProbe:
				case Constants.deleteProbe:
					return new ProbeUpdate(update);
					
				case Constants.newTrigger:
				case Constants.updateTrigger:
				case Constants.deleteTrigger:
					
					
					break;
					
				case Constants.updateBucket:
				case Constants.deleteBucket:
					
					break;
					
					
				case Constants.updateDiscovery:
				case Constants.deleteDiscovery:
					
					
					
				case Constants.updateHost:
				case Constants.deleteHost:
					
					break;
				
				case Constants.updateTemplate:	
				case Constants.deleteTemplate:
					break;
				
				
				
					
				case Constants.updateElement:
					
					break;
					
				case Constants.updateSnmp:
				case Constants.deleteSnmp:
			
					break;
			}
	      
	      return null;
	   }
}
