package utils;

import java.lang.System.Logger.Level;

import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class LogUtils {
	
	public static final String INFO_MESSAGE_TEMPLATE = "%s, the action will be executed\n";
	public static final String WARNING_MESSAGE_TEMPALTE = "%s, the action input is incomplete, it is ignored \n The action input is : %s";

	public static final void addLog(final Pane logConsole, final Level level, final String message) {
		if (null != logConsole) {
			String style = "-fx-fill: ";
			switch (level) {
			case ERROR:
				style += "red; -fx-font-weight: bolder;";
				break;
			case WARNING:
				style += "darkorange;-fx-font-weight: bold;";
				break;
			case INFO:
				style += "black;";
				break;
			case DEBUG:
				style += "blueviolet;";
				break;
			default:
				style += "navy;";
				break;
			}
			final Text logText = new Text();
			logText.setStyle(style);
			logText.setText(">> " + level + ", " + message + "\n\n");
			logConsole.getChildren().add(logText);
		}
	}

	public static final void clearLog(final Pane logConsole) {
		logConsole.getChildren().clear();
	}
}
