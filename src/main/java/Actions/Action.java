package Actions;

import java.time.LocalTime;

public class Action {
	
	private LocalTime executionTime;
	private final ActionType actionType;
	private String target;
	
	public Action(LocalTime executionTime, ActionType actionType, String target) {
		super();
		this.executionTime = executionTime;
		this.actionType = actionType;
		this.target = target;
	}

	public LocalTime getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(LocalTime executionTime) {
		this.executionTime = executionTime;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public ActionType getActionType() {
		return actionType;
	}
	
	public void doAction() {
		throw new RuntimeException("Not implemented YET");
	}
	
}
