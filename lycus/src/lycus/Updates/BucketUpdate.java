package lycus.Updates;

import lycus.Model.UpdateModel;

public class BucketUpdate  extends BaseUpdate{
	public BucketUpdate(UpdateModel update) {
		super(update);
		// TODO Auto-generated constructor stub
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
		
		return true;
	}
}
