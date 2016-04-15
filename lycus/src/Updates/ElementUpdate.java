package Updates;

import Model.UpdateModel;

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
		
		
		return true;
	}
	
	@Override
	public Boolean Delete()
	{
		super.Delete();
		
		
		return true;
	}
}
