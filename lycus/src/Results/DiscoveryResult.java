package Results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Elements.BaseElement;
import Elements.DiskElement;
import Elements.NicElement;
import GlobalConstants.Enums;
import GlobalConstants.Enums.DiscoveryElementType;
import Utils.JsonUtil;
import Utils.Logit;
import lycus.Trigger;

public class DiscoveryResult extends BaseResult {

	private HashMap<String, BaseElement> elements;// HashMap<elementName,BaseElement>

	public DiscoveryResult(String runnableProbeId, long timestamp, HashMap<String, BaseElement> elements) {
		super(runnableProbeId, timestamp);
		this.setElements(elements);
	}

	public DiscoveryResult(String runnableProbeId) {
		super(runnableProbeId);
	}

	public DiscoveryElementType getElementsType() {
		for (BaseElement element : getElements().values()) {
			if (element instanceof NicElement)
				return DiscoveryElementType.bw;
			if (element instanceof DiskElement)
				return DiscoveryElementType.ds;
		}
		return null;
	}

	@Override
	public void checkIfTriggerd(HashMap<String, Trigger> triggers) throws Exception {
		super.checkIfTriggerd(triggers);
	}

	@Override
	public Object getResultObject() {
		JSONArray result = new JSONArray();
		if (this.getErrorMessage().equals("")) {
			ArrayList<BaseElement> list = new ArrayList<BaseElement>(elements.values());
			try {
				result = (JSONArray) (new JSONParser()).parse(JsonUtil.ToJson(list));

			} catch (ParseException e) {
				Logit.LogError("WebExtendedResult - getResultObject()",
						"Unable to parse all elements of extended http probe " + this.getRunnableProbeId()
								+ " to json! ",
						e);
			}

		} else {
			result.add(0);
			result.add(this.getErrorMessage());

		}
		return result;
	}

	public HashMap<String, BaseElement> getElements() {
		return elements;
	}

	public void setElements(HashMap<String, BaseElement> elements) {
		this.elements = elements;
	}
}
