package Updates;

import java.util.concurrent.ConcurrentHashMap;
import GlobalConstants.Constants;
import Model.UpdateModel;
import Utils.Logit;
import lycus.RunnableProbe;
import lycus.RunnableProbeContainer;

public class TemplateUpdate extends BaseUpdate {

	public TemplateUpdate(UpdateModel update) {
		super(update);
		// TODO TemplateUpdate()
	}

	@Override
	public Boolean New() {
		return true;
	}

	@Override
	public Boolean Update() {
		super.Update();
		String templateId = getUpdate().template_id;
		boolean isActive = getUpdate().update_value.status.equals(Constants._true);

		ConcurrentHashMap<String, RunnableProbe> runnableProbes = RunnableProbeContainer.getInstanece()
				.getByTemplate(templateId);
		if (runnableProbes == null)
			return true;
		for (RunnableProbe runnableProbe : runnableProbes.values()) {
			String rpStr = runnableProbe.getId();
			if (rpStr.contains(
					"0b05919c-6cc0-42cc-a74b-de3b0dcd4a2a@74cda666-3d85-4e56-a804-9d53c4e16259@inner_aecc1485-6849-471d-b446-8e4ba05519da"))
				Logit.LogDebug("BREAKPOINT");
			if (isActive != runnableProbe.getProbe().isActive()) {
				runnableProbe.getProbe().setActive(isActive);
				runnableProbe.setActive(isActive);
				Logit.LogCheck(
						"Is active for " + runnableProbe.getProbe().getName() + " Is " + isActive + ". Update_id: "
								+ getUpdate().update_id + ", probe_id: " + runnableProbe.getProbe().getProbe_id());
			}
		}

		Logit.LogCheck("Template " + getUpdate().object_id + " was updated");
		return true;
	}

	// object_id = templateId
	@Override
	public Boolean Delete() {
		super.Delete();
		try {
			RunnableProbeContainer.getInstanece().removeByTemplateId(getUpdate().template_id);
			getUser().removeTemplateProbe(getUpdate().template_id);
		} catch (Exception e) {
			Logit.LogError("TemplateUpdate - Delete()", "Error removing TemplateId:  " + getUpdate().template_id);
			e.printStackTrace();
		}
		Logit.LogCheck("Template " + getUpdate().object_id + " was removed");
		return true;
	}
}
