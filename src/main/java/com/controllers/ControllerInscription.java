package com.controllers;

import java.io.FileInputStream;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bo.Ecole;
import com.dao_services.ServiceProvider;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ControllerInscription implements Initializable {

	@FXML
	private TextField cne_massar;

	@FXML
	private TextField confirmation_password_utilisateur;

	@FXML
	private PasswordField mot_de_passe_utilsateur;

	@FXML
	private Button b_inscrire;

	@FXML
	private Button b_connecter;
	@FXML
	AnchorPane container_message_success;
	@FXML
	HBox hbox_inscription;
	@FXML
	GridPane gridpane_inscription;
	@FXML
	VBox vbox_inscription;

	@FXML
	private Label input_message, label_message_success;

	@FXML
	private Hyperlink compte_utilisateur;
	@FXML
	private Label label_compte;
	private Connection connection;
	private Ecole ensah;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		container_message_success.setVisible(false);
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

	@FXML
	void choisir(ActionEvent event) {

	}

	@FXML
	void connecter(ActionEvent event) {

		b_connecter.getScene().getWindow().hide();
		Stage stageInscription = new Stage();

		try {
			Parent rootParent = FXMLLoader.load(getClass().getResource("/filesfxml/Connexion.fxml"));
			Scene scene = new Scene(rootParent);
			stageInscription.setScene(scene);
			stageInscription.setMinWidth(600);
			stageInscription.setMaxWidth(839);
			stageInscription.setMinHeight(600);
			stageInscription.setMaxHeight(700);
			stageInscription.show();
			stageInscription.centerOnScreen();
			stageInscription.setTitle("Gestion de passassion d'examens");
		} catch (IOException e1) {

			e1.printStackTrace();
		}

	}

	@FXML
	void inscrire(ActionEvent event) throws ClassNotFoundException, SQLException {

		if (verifierChamp() != null) {

			input_message.setText(verifierChamp());

		} else {
			String massarEtdudiant = cne_massar.getText().toString();
			String mot_de_passeEtudiant = mot_de_passe_utilsateur.getText().toString();

			boolean inscrire = ServiceProvider.inscriptionEtudiant(connection, massarEtdudiant, mot_de_passeEtudiant);
			if (inscrire) {
				label_message_success.setStyle("-fx-text-fill:green;-fx-background-color:#d3f7d7");
				label_message_success.setText("Votre inscription a été effectué avec succès");
				vbox_inscription.setVisible(false);
				hbox_inscription.setVisible(false);
				gridpane_inscription.setVisible(false);
				container_message_success.setVisible(true);

				compte_utilisateur.setOnAction((e) -> {
					compte_utilisateur.getScene().getWindow().hide();
					Stage stageInscription = new Stage();

					try {
						Parent rootParent = FXMLLoader.load(getClass().getResource("/filesfxml/Connexion.fxml"));
						Scene scene = new Scene(rootParent);
						stageInscription.setScene(scene);
						stageInscription.setMinWidth(600);
						stageInscription.setMaxWidth(839);
						stageInscription.setMinHeight(600);
						stageInscription.setMaxHeight(700);
						stageInscription.show();
						stageInscription.centerOnScreen();
						stageInscription.setTitle("Gestion de passassion d'examens");
					} catch (IOException e1) {

						e1.printStackTrace();
					}

				});

			} else {
				input_message.setStyle("-fx-font-weight:bold");
				input_message.setText("Aucun étudiant ne correspond à ce CNE / MASSAR");

			}

		}

	}

	public String verifierChamp() {

		if (mot_de_passe_utilsateur.getText().toString().isEmpty() || cne_massar.getText().toString().isEmpty()
				|| confirmation_password_utilisateur.getText().toString().isEmpty()) {
			ajouterStyleLabel();

			return "Tous les champs sont obligatoires";
		} else if (!validerCode_Massar(cne_massar.getText().toString())) {
			ajouterStyleLabel();
			return "CNE | MASSAR est incorrect (Indication: verifier que vous avez entrer 9 chiffres et un caractère (MASSAR) ou 10 chiffres (CNE)";

		} else if (!confirmerMotDePasse(confirmation_password_utilisateur.getText().toString())) {
			ajouterStyleLabel();

			return "Les mot de passe ne sont pas identiques";

		} else {
			return null;
		}

	}

	public boolean confirmerMotDePasse(String password) {

		return mot_de_passe_utilsateur.getText().toString().equals(password);
	}

	public boolean validerCode_Massar(String massar) {
		String ePattern = "^[a-zA-Z]{0,1}[0-9]{9,10}$";
		Pattern p = Pattern.compile(ePattern);
		Matcher m = p.matcher(massar);

		return (m.matches() && massar.length() == 10);
	}

	public void ajouterStyleLabel() {
		input_message.setStyle("-fx-background-color: #f0b4b4");
	}

}
