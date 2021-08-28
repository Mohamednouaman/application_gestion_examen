package com.controllers;

import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.ResourceBundle;

import com.bo.Ecole;
import com.dao_services.ServiceProvider;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControllerConnexionAdmin implements Initializable {

	@FXML
	private Button connexion;
	@FXML
	private TextField username;

	@FXML
	private PasswordField password;

	@FXML
	private Label label_message;

	private Connection connection;

	private Ecole ensah;

	@FXML
	void connecter(ActionEvent event) {
		String usernameString = username.getText().toString();
		String passwordString = password.getText().toString();

		if (usernameString.trim().length() == 0 || passwordString.isEmpty()) {

			label_message.setText("Veuillez saisir vos informations");

			ajouterStyle();
		} else {
			if (!label_message.getText().isEmpty()) {

				label_message.setText("");
				label_message.setStyle("fx-background-color:white");

			}

			if (ServiceProvider.verifyAccountForAddperiodExamen(connection, usernameString, passwordString)) {

				connexion.getScene().getWindow().hide();
				Stage stage = new Stage();
				Parent rootParent;
				try {
					rootParent = FXMLLoader.load(getClass().getResource("/filesfxml/AddPeriodOfExam.fxml"));
					Scene scene = new Scene(rootParent);
					stage.setScene(scene);
					stage.setMinWidth(600);
					stage.setMaxWidth(839);
					stage.setMinHeight(600);
					stage.setMaxHeight(700);
					stage.show();
					stage.centerOnScreen();
					stage.setTitle("Gestion de passaion d'examens");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				label_message.setText("Identifiant Incorrect");
				ajouterStyle();
			}
		}

	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		/*------------------------------------------------------------------------------------------------------------------------*/
		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream("Config.properties"));
			Class.forName(properties.getProperty("jdbc.driver.class"));
			connection = DriverManager.getConnection(properties.getProperty("jdbc.url"),
					properties.getProperty("jdbc.user"), properties.getProperty("jdbc.password"));
		} catch (FileNotFoundException e) {
			System.out.println("Il vous manque le fichier de configuration !");
			return;
		} catch (Exception e) {
			// IOException :: NullPointerException :: SQLException.
			System.out.println("il n'y a pas de connexion !");
			return;
		}

		/*------------------------------------------------------------------------------------------------------------------------*/

		ensah = ServiceProvider.uploadAndReturnDataOfApp(connection);
	}

	public void ajouterStyle() {
		label_message.setStyle("-fx-background-color: #f0b4b4");
		label_message.setStyle("-fx-text-fill:red");

	}

}
