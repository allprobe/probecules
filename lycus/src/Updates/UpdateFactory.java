package Updates;

import GlobalConstants.Constants;
import Model.UpdateModel;
import Utils.Logit;

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
					return new TriggerUpdate(update);
					
				case Constants.updateBucket:
				case Constants.deleteBucket:
					return new BucketUpdate(update);
				
//				case Constants.updateDiscovery:
//				case Constants.deleteDiscovery:
//					return new DiscoveryUpdate(update);
					
				case Constants.updateElement:
					return new ElementUpdate(update);
					
				case Constants.updateHost:
				case Constants.deleteHost:
					return new HostUpdate(update);
				
				case Constants.updateTemplate:	
				case Constants.deleteTemplate:
					return new TemplateUpdate(update);
					
				case Constants.updateSnmp:
//					Logit.LogCheck("Updating snmp collector");
				case Constants.deleteSnmp:
					return new SnmpUpdate(update);
					
				case Constants.deleteEvent:
					return new EventUpdate(update);
			}
	      
	      return null;
	   }
}
