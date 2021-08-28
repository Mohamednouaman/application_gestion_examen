package com.controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

import com.bo.Ecole;
import com.bo.Examen;
import com.dao_services.ServiceExamen;
import com.dao_services.ServiceProvider;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ControllerDashboard extends ListCell<String> implements Initializable {

	@FXML
	private Label label_username;

	@FXML
	private MenuButton menubutton;

	@FXML
	private Button button_home;

	@FXML
	private Button button_exams;
	@FXML
	private Button btn_voir_examens;

	@FXML
	private Button button_help;

	@FXML
	private AnchorPane student_informations;

	@FXML
	private AnchorPane anchor_accuiel;

	@FXML
	private AnchorPane container_image;
	@FXML
	private AnchorPane anchor_success;

	@FXML
	private AnchorPane anchor_first_palnning;
	@FXML
	private TableView<ServiceExamen> table_exam_planning;
	@FXML
	private TableColumn<ServiceExamen, String> table_exam_module;
	@FXML
	private TableColumn<ServiceExamen, Date> table_exam_date;
	@FXML
	private TableColumn<ServiceExamen, Integer> table_exam_heure;
	@FXML
	private TableColumn<ServiceExamen, Time> table_exam_duree;
	@FXML
	private TableColumn<ServiceExamen, String> table_exam_salle;
	private Connection connection;
	private ServiceExamen serviceExamen;
	private ArrayList<Examen> listeExamenEtudiant;
	private Ecole ensah;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		ajouterIcon("/image/home2.png", button_home);
		ajouterIcon("/image/examss.png", button_exams);

		ajouterIcon("/image/helps.png", button_help);

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

		ensah = ServiceProvider.uploadAndReturnDataOfApp(connection);

		anchor_first_palnning.setVisible(false);
		anchor_success.setVisible(false);

		try {
			label_username.setText("Bonjour " + ((ControllerLogin.getInstance().getNomEtudiant() != null)
					? ControllerLogin.getInstance().getNomEtudiant()
					: ""));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			System.out.println(ControllerLogin.getInstance().getUsernameEtudiant());
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		button_home.fire();
		table_exam_heure.setStyle("-fx-alignment:center");
		table_exam_module.setStyle("-fx-alignment:center");
		table_exam_date.setStyle("-fx-alignment:center");
		table_exam_duree.setStyle("-fx-alignment:center");
		table_exam_salle.setStyle("-fx-alignment:center");
		table_exam_module.setCellValueFactory(new PropertyValueFactory<ServiceExamen, String>("module"));
		table_exam_date.setCellValueFactory(new PropertyValueFactory<ServiceExamen, Date>("date"));
		table_exam_heure.setCellValueFactory(new PropertyValueFactory<ServiceExamen, Integer>("heure"));
		table_exam_duree.setCellValueFactory(new PropertyValueFactory<ServiceExamen, Time>("durre"));
		table_exam_salle.setCellValueFactory(new PropertyValueFactory<ServiceExamen, String>("salle"));
		table_exam_planning.getColumns().addAll(table_exam_module, table_exam_date, table_exam_heure, table_exam_duree,
				table_exam_salle);

		try {
			listeExamenEtudiant = ServiceProvider.recupererInfoEspaceEtudiant(ensah,
					ControllerLogin.getInstance().getUsernameEtudiant());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (listeExamenEtudiant.size() > 0) {
			for (Examen examen : listeExamenEtudiant) {

				serviceExamen = new ServiceExamen(
						examen.getModule().getModule() + " \n(" + ((examen.pourTP()) ? "Tp)" : "Théorique )"),
						examen.getDate(), examen.getTempsDebut(), examen.getDuree() + " min",
						examen.getSalles().stream().map(s -> s.getNom_Salle()).reduce("",
								(init, iterateur) -> init + iterateur + "\n"));
				table_exam_planning.getItems().add(serviceExamen);

			}

		}

	}

	@FXML
	void cliquerBtn(ActionEvent event) {
		if (event.getSource() == button_home) {
			FadeTransition fadeTransition = new FadeTransition(javafx.util.Duration.millis(3000.0), container_image);
			fadeTransition.setToValue(3000.0);
			fadeTransition.setFromValue(0.0);
			fadeTransition.setCycleCount(Timeline.INDEFINITE);
			fadeTransition.setAutoReverse(true);
			fadeTransition.play();

			anchor_accuiel.setVisible(true);

			anchor_first_palnning.setVisible(false);
			anchor_success.setVisible(false);
			button_home.setStyle("-fx-background-color:#191970 white #0000cd;" + "-fx-text-fill:white;");
			button_exams.setStyle(null);
		} else if (event.getSource() == button_exams || event.getSource() == btn_voir_examens) {

			anchor_accuiel.setVisible(false);
			button_home.setStyle(null);
			button_exams.setStyle("-fx-background-color:#191970 white #0000cd;" + "-fx-text-fill:white;");

			if (listeExamenEtudiant.size() > 0) {

				anchor_first_palnning.setVisible(true);

			} else {
				anchor_success.setVisible(true);
			}
		}

	}

	@FXML
	void seDeconnecter(ActionEvent event) {

		try {

			menubutton.getScene().getWindow().hide();
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
	void voirCompte(ActionEvent event) {

		// Affichage des informations d'etudiant

	}

	public void ajouterIcon(String path, Button btn) {

		ImageView imageView = new ImageView(new Image(getClass().getResource(path).toString()));
		imageView.setFitWidth(20);
		imageView.setFitHeight(15);
		imageView.setStyle("-fx-border-color:white");

		btn.setGraphic(imageView);
		btn.setGraphicTextGap(10);
	}

}
