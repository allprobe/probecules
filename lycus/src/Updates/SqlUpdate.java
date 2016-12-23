package Updates;

import Model.UpdateModel;

public class SqlUpdate extends BaseUpdate{

	public SqlUpdate(UpdateModel update) {
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
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
		super.Delete();
		return true;
	}
}
