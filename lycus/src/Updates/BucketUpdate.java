package Updates;

import Model.UpdateModel;
import Utils.Logit;

public class BucketUpdate  extends BaseUpdate{
	public BucketUpdate(UpdateModel update) {
		super(update);
	}

	@Override
	public Boolean New()
	{
//		super.New();
	
		return true;
	}
	
	@Override
	public Boolean Update()
	{
//		super.Update();
		
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
		super.Delete();
		getUser().deleteBucket(getUpdate().object_id);
		Logit.LogCheck("Bucket: " + getUpdate().object_id + " has removed");
		return true;
	}
}
