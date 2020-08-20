/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import java.io.IOException;

import controller.FormuleController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.WebDriverUtils;

/**
 *
 * @author Xuhao
 */
public class AutoWebApplication extends Application {
    
    @Override
    public void start(Stage primaryStage) throws IOException {
    	
    	final String formuleViewAdress = "/view/formule.fxml";
    	
    	//load the root
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(formuleViewAdress));
        
        Parent root = loader.load();
        FormuleController controller = loader.getController();
        controller.primaryStage = primaryStage;
        //add all elements
        
        Scene scene = new Scene(root);
        primaryStage.setTitle("Linxuhao's Automatic Web Task");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	WebDriverUtils.setWebDriverSystemProperty();
        launch(args);
    }
    
}
