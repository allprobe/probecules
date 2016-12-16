package Collectors;

import GlobalConstants.Enums.CollectorType;

public class BaseCollector {
	private String id;
	private String name;
	private CollectorType type;
	private int timeout;
	private boolean isActive;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CollectorType getType() {
		return type;
	}

	public void setType(CollectorType type) {
		this.type = type;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
