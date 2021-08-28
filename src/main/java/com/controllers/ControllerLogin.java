package com.controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;

import com.bo.Ecole;
import com.dao_services.ServiceProvider;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControllerLogin implements Initializable {

	@FXML
	private Button connexion;

	@FXML
	private CheckBox remember;
	@FXML
	private ProgressIndicator progress;

	@FXML
	private Hyperlink forgetPassword;

	@FXML
	private PasswordField password;
	@FXML
	private TextField username;

	@FXML
	private Button inscription;

	@FXML
	private Label message_error_success;

	private static ControllerLogin Instance;

	private Connection connection;
	private Ecole ensah;

	void chekbox(ActionEvent e) {

	}

	@FXML
	void connecter(ActionEvent event) throws InterruptedException, ClassNotFoundException, SQLException {

		String usernameString = username.getText().toString();
		String passwordString = password.getText().toString();

		if (usernameString.trim().length() == 0 || passwordString.isEmpty()) {

			message_error_success.setText("Veuillez saisir vos informations");
			ajouterStyle();
		} else {
			if (!message_error_success.getText().isEmpty()) {

				message_error_success.setText("");
				message_error_success.setStyle("fx-background-color:white");

			}

			progress.setVisible(true);
			progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

			if (ServiceProvider.verifyAccountOfUser(connection, usernameString, passwordString)) {
				PauseTransition pauseTransition = new PauseTransition(javafx.util.Duration.seconds(3));
				pauseTransition.setOnFinished(ev -> {
					progress.setProgress(1);

					connexion.getScene().getWindow().hide();
					Stage stage = new Stage();
					Parent rootParent=null;
					try {
					
						rootParent = FXMLLoader.load(getClass().getResource("/filesfxml/dashboardAdmin.fxml"));
						
						Scene scene = new Scene(rootParent);
						stage.setScene(scene);
						stage.setMinWidth(600);
						stage.setMaxWidth(839);
						stage.setMinHeight(600);
						stage.setMaxHeight(700);
						stage.centerOnScreen();
						stage.setResizable(false);
						stage.show();
						stage.setTitle("Gestion de passassion d'examens");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				});
				pauseTransition.play();

			} else if (ServiceProvider.verifyAccountOfStudent(connection, usernameString, passwordString)) {

				PauseTransition pauseTransition = new PauseTransition(javafx.util.Duration.seconds(3));
				pauseTransition.setOnFinished(ev -> {
					progress.setProgress(1);

					connexion.getScene().getWindow().hide();
					Stage stage = new Stage();
					Parent rootParent;
					try {
						rootParent = FXMLLoader.load(getClass().getResource("/filesfxml/dashboard.fxml"));
						Scene scene = new Scene(rootParent);
						stage.setScene(scene);
						stage.setMinWidth(600);
						stage.setMaxWidth(839);
						stage.setMinHeight(600);
						stage.setMaxHeight(700);
						stage.show();
						stage.centerOnScreen();
						stage.setTitle("Gestion de passassion d'examens");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				});
				pauseTransition.play();

			} else {

				PauseTransition pauseTransition = new PauseTransition(javafx.util.Duration.seconds(2));

				pauseTransition.setOnFinished(ev -> {
					progress.setProgress(1);
					message_error_success.setText("Identifiant Incorrect");
					ajouterStyle();
				});
				pauseTransition.play();

			}
		}

	}

	@FXML
	void inscrire(ActionEvent event) throws IOException {

		connexion.getScene().getWindow().hide();
		Stage stageInscription = new Stage();
		Parent rootParent = FXMLLoader.load(getClass().getResource("/filesfxml/Inscription.fxml"));
		Scene scene = new Scene(rootParent);
		stageInscription.setScene(scene);
		stageInscription.setMinWidth(600);
		stageInscription.setMaxWidth(839);
		stageInscription.setMinHeight(600);
		stageInscription.setMaxHeight(700);
		stageInscription.show();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		username.setPromptText("Nom d'utilisateur");
		password.setPromptText("Mot de passe");
		progress.setVisible(false);

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
		/*------------------------------------------------------------------------------------------------------------------------*/

	}

	public void ajouterStyle() {
		message_error_success.setStyle("-fx-background-color: #f0b4b4");
	}

	public ControllerLogin() {
		Instance = this;
	}

	public static ControllerLogin getInstance() {
		return Instance;
	}

	public String getNomEtudiant() throws SQLException {

		ResultSet resultSet = ServiceProvider.recupererInformationsEtudiant(connection, username.getText().toString());

		return resultSet.next() ? resultSet.getString(2) : null;

	}

	public String getUsernameEtudiant() throws SQLException {

		ResultSet resultSet = ServiceProvider.recupererInformationsEtudiant(connection, username.getText().toString());

		return resultSet.next() ? resultSet.getString(1) : null;
	}

	public String getNomAdmin() throws SQLException {
		ResultSet resultSet = ServiceProvider.recupererInformationsAdmin(connection, username.getText().toString());

		return resultSet != null ? resultSet.getString(2) : null;

	}

	public String getUsernameAdmin() throws SQLException {

		ResultSet resultSet = ServiceProvider.recupererInformationsAdmin(connection, username.getText().toString());
		return resultSet.getString(1);

	}

}
	

