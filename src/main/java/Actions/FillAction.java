package Actions;

import java.time.LocalTime;

public class FillAction extends Action {
	
	private String content;

	public FillAction(LocalTime executionTime,  String target, String content) {
		super(executionTime, ActionType.FILL, target);
		this.setContent(content);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	

}
