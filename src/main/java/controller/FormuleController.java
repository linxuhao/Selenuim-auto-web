package controller;

import java.io.File;
import java.lang.System.Logger.Level;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import actions.AbstractAction;
import customExceptions.LoggedException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.FormuleModel;
import model.FormuleModelDeserializer;
import utils.LogUtils;
import utils.WebDriverUtils;

public class FormuleController {

	@FXML
	private Button removeButton;

	@FXML
	private Button addButton;

	@FXML
	private Button loadButton;

	@FXML
	private Button saveButton;

	@FXML
	private Button startButton;

	@FXML
	private Button clearLogButton;

	@FXML
	private TextFlow logConsole;

	@FXML
	private ScrollPane logPane;

	@FXML
	private ScrollPane displayPane;

	@FXML
	private VBox actionDisplayZone;

	@FXML
	private Pagination launcnNumberPicker;

	public Stage primaryStage;

	private FormuleModel model = new FormuleModel();

	public final void activateButtons() {
		addButton.setDisable(false);
		removeButton.setDisable(false);
		loadButton.setDisable(false);
		saveButton.setDisable(false);
		startButton.setDisable(false);
		launcnNumberPicker.setDisable(false);
	}

	public final void addLog(final Level level, final String message) {
		LogUtils.addLog(logConsole, level, message);
	}

	public final void addLogLater(@Nullable final Integer currentLaunchIndex, final Level level, final String message) {
		String launchIndexMessage;
		if(null == currentLaunchIndex) {
			launchIndexMessage = "";
		}else {
			launchIndexMessage = "[" + currentLaunchIndex + "] ";
		}
		final String launchIndexedMessage = launchIndexMessage + message;
		Platform.runLater(() -> addLog(level, launchIndexedMessage));
	}
	
	public final void addLogLater(final Level level, final String message) {
		addLogLater(null, level, message);
	}

	public final void disactiveButtons() {
		addButton.setDisable(true);
		removeButton.setDisable(true);
		loadButton.setDisable(true);
		saveButton.setDisable(true);
		startButton.setDisable(true);
		launcnNumberPicker.setDisable(true);
	}

	public void initialize() {
		// when ever the content of scroll pane are changed (added or removed)
		// it will set the height value (>>1) to scroll pane vvalue (0-1) which means
		// always set to 1 => scroll to bottom
		displayPane.vvalueProperty().bind(actionDisplayZone.heightProperty());
		logPane.vvalueProperty().bind(logConsole.heightProperty());
	}

	private void addNewInputField() {
		final ActionInput actionInput = new ActionInput();
		actionDisplayZone.getChildren().add(actionInput);
	}

	private List<AbstractAction> getActionList(@Nullable final WebDriver webDriver) {
		final List<AbstractAction> actionList = new ArrayList<>();
		for (Node node : actionDisplayZone.getChildrenUnmodifiable()) {
			if (node instanceof ActionInput) {
				final ActionInput actionInput = (ActionInput) node;
				try {
					final AbstractAction action = actionInput.toAction();
					if (null != webDriver) {
						action.setWebDriver(webDriver);
					}
					actionList.add(action);
				} catch (LoggedException e) {
					addLogLater(e.getLogLevel(), e.getMessage());
					if (Level.ERROR == e.getLogLevel()) {
						throw e;
					}
				}
			}
		}
		return actionList;
	}

	private FileChooser getFileChooser(final String fileChooserName) {
		final FileChooser fileChooser = new FileChooser();
		final String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
		fileChooser.setInitialDirectory(new File(currentPath));
		fileChooser.setTitle(fileChooserName);
		return fileChooser;
	}

	private ObjectMapper getObjectMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		final SimpleModule usageModule = new SimpleModule().addDeserializer(FormuleModel.class,
				new FormuleModelDeserializer());
		mapper.registerModule(usageModule);
		return mapper;
	}

	private List<ActionInput> loadFromObjectList(final List<AbstractAction> actionList) {
		final List<ActionInput> actionInputList = new ArrayList<>();
		for (AbstractAction action : actionList) {
			actionInputList.add(action.toActionInput());
		}
		return actionInputList;
	}

	private void removeTheLastInputField() {
		if (!actionDisplayZone.getChildren().isEmpty()) {
			actionDisplayZone.getChildren().remove(actionDisplayZone.getChildren().size() - 1);
		}
	}

	private void taskCancelled(String cancelMessage) {
		addLog(Level.DEBUG, cancelMessage);
		activateButtons();
	}

	private void threadJobCompleted(final Integer currentLaunchIndex, final String completionMessage) {
		addLogLater(currentLaunchIndex, Level.DEBUG, completionMessage);
		Platform.runLater(() -> activateButtons());
	}

	private void updateFromModel() {
		actionDisplayZone.getChildren().clear();
		actionDisplayZone.getChildren().addAll(loadFromObjectList(this.model.getActionList()));
	}

	private void updateToModel() {
		this.model.setActionList(getActionList(null));
	}

	@FXML
	void add(ActionEvent event) {
		addNewInputField();
	}

	@FXML
	void clearLog(ActionEvent event) {
		LogUtils.clearLog(logConsole);
	}

	@FXML
	void loadFromFile(ActionEvent event) throws Exception {
		disactiveButtons();
		final FileChooser fileChooser = getFileChooser("Load Configuration file");
		final File file = fileChooser.showOpenDialog(primaryStage);
		if (null != file) {
			CompletableFuture.runAsync(() -> {
				// Read file in another thread for UI responsiveness
				try {
					final ObjectMapper mapper = getObjectMapper();
					final FormuleModel model = mapper.readValue(file, FormuleModel.class);
					this.model = model;
					Platform.runLater(() -> updateFromModel());
				} catch (Exception e) {
					addLogLater(Level.ERROR, e.getMessage());
					// for IDE consol debug purpose xD
					e.printStackTrace();
					throw new CompletionException(e);
				}
			}).whenComplete((i, t) -> threadJobCompleted(null, "Configuration file load finished"));
		} else {
			taskCancelled("No file to load selected");
		}
	}

	@FXML
	void remove(ActionEvent event) {
		removeTheLastInputField();
	}

	@FXML
	void saveToFile(ActionEvent event) throws Exception {
		disactiveButtons();
		try {
			// Not saving web driver to json, so no need to set it in actions
			final List<AbstractAction> actionList = getActionList(null);
			if (!actionList.isEmpty()) {
				final FileChooser fileChooser = getFileChooser("Save Configuration file");
				final File file = fileChooser.showSaveDialog(primaryStage);
				updateToModel();
				if (null != file) {
					addLog(Level.DEBUG, "Saving configuration to file : " + file.getAbsolutePath());
					CompletableFuture.runAsync(() -> {
						// Write to file in another thread to not block UI
						// Even though i blocked all buttons when writting xD
						try {
							final ObjectMapper mapper = getObjectMapper();
							mapper.writeValue(file, model);
						} catch (Exception e) {
							addLogLater(Level.ERROR, e.getMessage());
							// for IDE consol debug purpose xD
							e.printStackTrace();
							throw new CompletionException(e);
						}
					}).whenComplete((i, t) -> threadJobCompleted(null, "Configuration file save finished"));
				} else {
					taskCancelled("No file to save selected");
				}
			} else {
				taskCancelled("No action to save");
			}
		} catch (Exception e) {
			addLog(Level.ERROR, e.getMessage());
			activateButtons();
			throw e;
		}
	}

	@FXML
	void startTask(ActionEvent event) throws Exception {
		disactiveButtons();
		final int launcnNumber = this.launcnNumberPicker.getCurrentPageIndex() + 1;
		IntStream.range(1, launcnNumber + 1).parallel().forEach((currentLaunchIndex) -> {
			CompletableFuture.runAsync(() -> {
				try {
					final WebDriver webDriver = WebDriverUtils.getNewWebDriver();
					final List<AbstractAction> actionList = getActionList(webDriver);
					addLogLater(currentLaunchIndex, Level.DEBUG, "There are total of " + actionList.size() + " actions to execute");
					for (int i = 0; i < actionList.size(); i++) {
						final int humainReadableActionIndex = i + 1;
						addLogLater(currentLaunchIndex, Level.DEBUG,
								"Executing action " + humainReadableActionIndex + " out of " + actionList.size());
						try {
							actionList.get(i).doAction((level, message) -> addLogLater(currentLaunchIndex, level, message));
						} catch (LoggedException e) {
							// Stops the execution if is error level exception,
							// otherwise stop & skip only the current action
							if (Level.ERROR == e.getLogLevel()) {
								throw e;
							} else {
								addLogLater(currentLaunchIndex, e.getLogLevel(), e.getMessage());
							}
						}
					}
				} catch (Exception e) {
					addLogLater(currentLaunchIndex, Level.ERROR, e.getMessage());
					// for IDE consol debug purpose xD
					e.printStackTrace();
					throw new CompletionException(e);
				}
			}).whenComplete((i, t) -> threadJobCompleted(currentLaunchIndex, "Execution finished"));
		});
	}
}
