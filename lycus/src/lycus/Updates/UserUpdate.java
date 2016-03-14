package lycus.Updates;

import lycus.Model.UpdateModel;

public class UserUpdate extends BaseUpdate{

	public UserUpdate(UpdateModel update) {
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
