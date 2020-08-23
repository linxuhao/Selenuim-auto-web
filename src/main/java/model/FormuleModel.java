package model;

import java.util.ArrayList;
import java.util.List;

import actions.AbstractAction;

public class FormuleModel {
	
	private final List<AbstractAction> actionList;
	
	public FormuleModel() {
		super();
		this.actionList = new ArrayList<>();
	}

	public FormuleModel(List<AbstractAction> actionList) {
		super();
		this.actionList = actionList;
	}

	public List<AbstractAction> getActionList() {
		return actionList;
	}

	public void setActionList(List<AbstractAction> actionList) {
		this.actionList.clear();
		this.actionList.addAll(actionList);
	}

}
