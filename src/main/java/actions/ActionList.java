package actions;

import java.util.ArrayList;
import java.util.List;

public class ActionList {
	
	private List<AbstractAction> actionList;
	

	public ActionList(List<AbstractAction> actionList) {
		super();
		this.actionList = actionList;
	}

	public ActionList() {
		super();
		this.actionList = new ArrayList<>();
	}

	public List<AbstractAction> getActionList() {
		return actionList;
	}

	public void setActionList(List<AbstractAction> actionList) {
		this.actionList = actionList;
	}
	
}
