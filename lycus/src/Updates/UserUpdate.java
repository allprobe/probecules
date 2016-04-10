package Updates;

import Model.UpdateModel;

public class UserUpdate extends BaseUpdate{

	public UserUpdate(UpdateModel update) {
		super(update);
		// TODO UserUpdate()
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
		super.Update();
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
//		super.Delete();
		return true;
	}
}
