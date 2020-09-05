package utils;

import java.util.logging.Level;

import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class LogUtils {

	public static final String INFO_MESSAGE_TEMPLATE = "%s, the action will be executed\n";
	public static final String WARNING_MESSAGE_TEMPALTE = "%s, the action input is incomplete, it is ignored \n The action input is : %s";

	public static final void addLog(final Pane logConsole, final Level level, final String message) {
		if (null != logConsole) {
			String style = "-fx-fill: ";
			if (Level.SEVERE.equals(level)) {
				style += "red; -fx-font-weight: bolder;";
			} else if (Level.WARNING.equals(level)) {
				style += "darkorange;-fx-font-weight: bold;";
			} else if (Level.FINE.equals(level)) {
				style += "black;";
			} else if (Level.FINER.equals(level)) {
				style += "blueviolet;";
			} else {
				style += "navy;";
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
