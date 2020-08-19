package controller;

import java.io.IOException;

import Actions.ActionType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class ActionInput extends HBox{

	@FXML 
	private ComboBox<ActionType> actionTypeSelect;
	@FXML 
	private Label timeLabel;
	@FXML 
	private Label timeLabel1;
	@FXML 
	private Label timeLabel2;
	@FXML 
	private TextField hours;
	@FXML 
	private TextField minutes;
	@FXML 
	private TextField seconds;
	@FXML 
	private Label targetLabel;
	@FXML 
	private TextField target;
	@FXML 
	private Label contentLabel;
	@FXML 
	private TextField content;

    public ActionInput() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/action.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            actionTypeSelect.getItems().addAll(ActionType.values());
            actionTypeSelect.getSelectionModel().selectFirst();
            onActionTypeChanged();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    @FXML
    void onActionTypeChange(ActionEvent event) {
    	System.out.println("changed to " + actionTypeSelect.getSelectionModel().getSelectedItem());
    	onActionTypeChanged();
    }

	private void onActionTypeChanged() {
		switch(actionTypeSelect.getSelectionModel().getSelectedItem()) {
    	case OPEN:
    	case CLICK:
    	case DELAY:
    		hideContentInput();
    		break;
      	case FILL:
		default:
			showContentInput();
			break;
    	
    	}
	}

	private void showContentInput() {
		contentLabel.setVisible(true);
		content.setVisible(true);
	}

	private void hideContentInput() {
		contentLabel.setVisible(false);
		content.setVisible(false);
	}
}
