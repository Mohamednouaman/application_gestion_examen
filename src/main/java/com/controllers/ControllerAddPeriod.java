package com.controllers;

import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.ResourceBundle;

import com.dao_services.ServiceProvider;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ControllerAddPeriod implements Initializable {

	@FXML
	private DatePicker date_debut_periode;

	@FXML
	private DatePicker date_fin_periode;

	@FXML
	private Button btn_ajouter;
	@FXML
	private Label label_add_exam;
	@FXML
	private Label label_compte;
	@FXML
	private Hyperlink add_exam;
	@FXML
	private AnchorPane anchor_after_add_period, anchor_before_add_period;
	@FXML
	private Button btn_quitter;
	@FXML 
	private ProgressIndicator  indicator_progress;

	private Connection connexion;
	private boolean sending_all_messages=false;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub

		anchor_after_add_period.setVisible(false);
		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream("Config.properties"));
			Class.forName(properties.getProperty("jdbc.driver.class"));
			connexion = DriverManager.getConnection(properties.getProperty("jdbc.url"),
					properties.getProperty("jdbc.user"), properties.getProperty("jdbc.password"));
		} catch (FileNotFoundException e) {
			System.out.println("Il vous manque le fichier de configuration !");
			return;
		} catch (Exception e) {
			// IOException :: NullPointerException :: SQLException.
			System.out.println("il n'y a pas de connexion !");
			return;
		}
	    indicator_progress.setVisible(false);

	}

	@FXML
	void ajouterPeriode(ActionEvent event) {

		boolean add = ServiceProvider.addExamPeriodIfIsNotBeing(connexion, date_debut_periode.getValue(),
				date_fin_periode.getValue());
		if (add) {
	          new Message().start();
	          Service verify_sending_all_email=  new Service();
	          verify_sending_all_email.start();
	         if(!verify_sending_all_email.sendingAllMessages()) {
	        	 indicator_progress.setVisible(true); 
	        	 label_add_exam.setText("veuillez patienter ...");
	        	 label_add_exam.setStyle("-fx-text-fill: red");
	        	 btn_ajouter.setDisable(true);
	        	 date_debut_periode.setDisable(true);
	        	 date_fin_periode.setDisable(true);
	        	 
	         } else {
	        	anchor_after_add_period.setVisible(true);
	   			anchor_before_add_period.setVisible(false);
	   		   }


	     	} else {

			label_add_exam.setText("La periode n'est pas ajoutee .Ressayez a nouveau");
			label_add_exam.setStyle("-fx-background-color: #f0b4b4");
			label_add_exam.setStyle("-fx-text-fill: red");

		}

	}

	@FXML
	void ajouterExamen(ActionEvent event) {

		try {

			add_exam.getScene().getWindow().hide();
			Stage primaryStage = new Stage();

			Parent root = FXMLLoader.load(getClass().getResource("/filesfxml/Connexion.fxml"));

			Scene scene = new Scene(root);
			primaryStage.setScene(scene);

			primaryStage.setMinWidth(700);
			primaryStage.setMaxWidth(1039);
			primaryStage.setMinHeight(600);
			primaryStage.setMaxHeight(700);
			primaryStage.show();
			primaryStage.centerOnScreen();
			primaryStage.setTitle("Gestion de passassion d'examens");
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	@FXML
	void quitterPage(ActionEvent event) {

		btn_quitter.getScene().getWindow().hide();

	}
  
  
  
  
    class Message extends Thread{
    	
    	@Override
		public void run() {
		    sendEmailAdmin();
		}

		public  void sendEmailAdmin() {
    		
    		ServiceProvider.sendEmailPasswordAdmin(connexion, true);
    		    sending_all_messages=true;	       
	        	anchor_after_add_period.setVisible(true);
	   			anchor_before_add_period.setVisible(false);
	   		    
    		
    	}
    	
	 
 }
    class Service extends Thread{
    	
    	@Override
		public void run() {
			sendingAllMessages();
		}

		public boolean sendingAllMessages() {
    	   return sending_all_messages;
    	}
    	
    }
  
 
	
}
