package application;

import javafx.application.Preloader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.application.Preloader.StateChangeNotification.Type;

public class ApplicationPreloader extends Preloader {

	@FXML
	private ImageView splashView;
	private Stage preloaderStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.preloaderStage = primaryStage;

		final String viewAdress = "/view/preloader.fxml";
		primaryStage.initStyle(StageStyle.TRANSPARENT);

		// load the root
		final FXMLLoader loader = new FXMLLoader();
		loader.setController(this);
		loader.setLocation(getClass().getResource(viewAdress));
		final Parent root = loader.load();
		splashView.setImage(new Image(getClass().getResourceAsStream("/Bean-Eater-1s-200px.gif")));
		// add all elements
		final Scene scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		primaryStage.centerOnScreen();
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	@Override
	public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
		if (stateChangeNotification.getType() == Type.BEFORE_START) {
			preloaderStage.hide();
		}
	}

}
