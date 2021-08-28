package com.controllers;

import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bo.AnneeFiliere;
import com.bo.Ecole;
import com.bo.Enseignant;
import com.bo.Examen;
import com.bo.Module;
import com.bo.Salle;
import com.dao_services.ServiceInfoExamen;
import com.dao_services.ServiceProvider;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


public class ControllerDashboardAdmin implements Initializable {
	
	
//	private static final  ControllerDashboardAdmin INSTANCE=new ControllerDashboardAdmin();
//	
//	private ControllerDashboardAdmin() {
//		
//	}
//	
//public static ControllerDashboardAdmin getInstance() {
//	return INSTANCE;
//}
//	

	@FXML
	private Label name_admin;

	@FXML
	private Hyperlink deconnecter_admin;

	@FXML
	private AnchorPane anchor_accueil;

	@FXML
	private Button btn_accueil;

	@FXML
	private Button btn_examen;

	@FXML
	private Button btn_aide;
	@FXML
	private Button btn_quitter;

	@FXML
	private Label label_space_admin;
	@FXML
	private ImageView image_espace_admin;

	@FXML
	private AnchorPane menubtn_ajouter_voir_examen;

	@FXML
	private MenuItem btn_ajouter_examen;

	@FXML
	private MenuItem btn_voir_examens;
	@FXML
	Hyperlink accueil;
	@FXML
	private AnchorPane anchor_choix_classe;
	@FXML
	private AnchorPane anchor_success;

	@FXML
	private ComboBox<String> choix_classe;

	@FXML
	private Button btn_choix_classe;

	@FXML
	private AnchorPane anchor_choix_module;
	@FXML
	private TableView<ServiceInfoExamen> table_info_examen;
	@FXML
	private TableColumn<ServiceInfoExamen, String> column_classe_examen;
	@FXML
	private TableColumn<ServiceInfoExamen, String> column_module_examen;
	@FXML
	private TableColumn<ServiceInfoExamen, LocalDate> column_date_examen;
	@FXML
	private TableColumn<ServiceInfoExamen, LocalTime> column_heure_examen;
	@FXML
	private TableColumn<ServiceInfoExamen, String> column_duree_examen;
	@FXML
	private TableColumn<ServiceInfoExamen, String> column_type_examen;

	@FXML
	private ComboBox<String> choix_module;

	@FXML
	private Button bnt_choix_module;

	@FXML
	private AnchorPane anchor_date_examen;

	@FXML
	private JFXDatePicker date_examen;

	@FXML
	private TextField duree_examen;

	@FXML
	private JFXTimePicker heure_examen;

	@FXML
	private ComboBox<Boolean> type_examen;

	@FXML
	private Button btn_date_type_examen;

	@FXML
	private AnchorPane anchor_salle_examen;

	@FXML
	private MenuButton controlleur_examen;

	@FXML
	private MenuButton salle_examen;
	@FXML
	HBox container_image_admin;
	@FXML
	private Label label_notes, label_alert_danger_classe, label_alert_danger_module, label_alert_danger_date,
			label_alert_danger_salle, planning_exam_admin;
	@FXML
	private AnchorPane anchor_info_examen;

	@FXML
	private Button btn_choix_salle_controlleur_examen;

	private Connection connection;
	private Ecole ensah;
	private ArrayList<AnneeFiliere> anneeFiliere;
	private ArrayList<com.bo.Module> modules;
	private AnneeFiliere anneeFiliere1;
	private ArrayList<Salle> sallesDisponibleExamen;
	private ArrayList<Enseignant> enseignantsDisponibleExamen;
	private ArrayList<CheckBox> sallesArrayList = new ArrayList<>();
	private ArrayList<CheckBox> controlleursArraylist = new ArrayList<>();
	private ArrayList<Salle> sallesChoisis;
	private ArrayList<Enseignant> controlleursChoisis;
	private ArrayList<Examen> listeExamens;
	private ServiceInfoExamen serviceInfoExamen;

	private Examen examen;

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub

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
		try {
			listeExamens = ServiceProvider.recupererExamensDeAdmin(ensah,
					ControllerLogin.getInstance().getUsernameAdmin());
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


          
		label_notes.setText("NB  :  Periode d'examen est de  " + ensah.getPeriodeExamen().getDateDebutExamen() + " à  "
				+ ensah.getPeriodeExamen().getDateFinExamen());
         System.out.println("Avant intilisation de column ...........");
        
		column_classe_examen.setCellValueFactory(new PropertyValueFactory<ServiceInfoExamen, String>("classe"));
		column_module_examen.setCellValueFactory(new PropertyValueFactory<ServiceInfoExamen, String>("module"));
		column_date_examen.setCellValueFactory(new PropertyValueFactory<ServiceInfoExamen, LocalDate>("date"));
		column_heure_examen.setCellValueFactory(new PropertyValueFactory<ServiceInfoExamen, LocalTime>("heure"));
		column_type_examen.setCellValueFactory(new PropertyValueFactory<ServiceInfoExamen, String>("type"));
		column_duree_examen.setCellValueFactory(new PropertyValueFactory<ServiceInfoExamen, String>("duree"));
		
		table_info_examen.getColumns().addAll(column_classe_examen, column_date_examen, column_duree_examen,
				column_heure_examen, column_type_examen, column_module_examen);
		
		table_info_examen.setVisible(false);
		
//          
		if (listeExamens.size() > 0) {
			for (Examen examen : listeExamens) {

				serviceInfoExamen = new ServiceInfoExamen(examen.getAnneeFiliere().getAnneeFiliere(),
						examen.getModule().getModule(), examen.getDate(), examen.getTempsDebut(),
						examen.getDuree() + " min", (examen.pourTP() ? "Examen TP" : "Examen theorique"));
				table_info_examen.getItems().add(serviceInfoExamen);
			}
		}

		anchor_accueil.setVisible(true);
		type_examen.getItems().addAll(true, false);

		anchor_choix_classe.setVisible(false); //
		anchor_choix_module.setVisible(false);
		anchor_date_examen.setVisible(false);
		anchor_salle_examen.setVisible(false);
		anchor_success.setVisible(false);
		anchor_info_examen.setVisible(false);
		menubtn_ajouter_voir_examen.setVisible(false);
		btn_accueil.fire();
		ajouterIcon("/image/home2.png", btn_accueil);
		ajouterIcon("/image/examss.png", btn_examen);
		ajouterIcon("/image/helps.png", btn_aide);
		// pour le teste

		try {

			name_admin.setText("Bonjour Mr/Mme " + ((ControllerLogin.getInstance().getNomAdmin() != null)
					? ControllerLogin.getInstance().getNomAdmin()
					: ""));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	void valider(ActionEvent event) {
		// System.out.println(choice_controller.getItems().toArray().length);
	}

	@FXML
	void deconnecter(ActionEvent event) {
		try {

			deconnecter_admin.getScene().getWindow().hide();
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
	void ajouterExamen(ActionEvent event) {
		choix_classe.setPromptText("Choisir la classe");
		anchor_accueil.setVisible(false);
		anchor_choix_classe.setVisible(true);
		try {
			anneeFiliere = ServiceProvider.getAnneesFiliereOfThisUser(ensah,
					ControllerLogin.getInstance().getUsernameAdmin());

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			System.out.println("Aucun filiere trouve");

		}
		Iterator<AnneeFiliere> iterator = anneeFiliere.iterator();
		while (iterator.hasNext()) {

			choix_classe.getItems().add(iterator.next().getAnneeFiliere());
		}

	}

	@FXML
	void ajouterVoirExamen(ActionEvent event) {

	}

	@FXML
	void naviguer(ActionEvent event) {

		if (event.getSource() == btn_accueil) {

			btn_examen.setStyle("");
			btn_aide.setStyle("");
			menubtn_ajouter_voir_examen.setVisible(false);
			label_space_admin.setVisible(true);
			container_image_admin.setVisible(true);
			btn_accueil.setStyle("-fx-background-color:#4169e1");

		} else if (event.getSource() == btn_examen) {
			btn_accueil.setStyle("");
			btn_aide.setStyle("");
			menubtn_ajouter_voir_examen.setVisible(true);
			label_space_admin.setVisible(false);
			container_image_admin.setVisible(false);
			btn_examen.setStyle("-fx-background-color:#4169e1");

		} else if (event.getSource() == btn_aide) {
			btn_accueil.setStyle("");
			menubtn_ajouter_voir_examen.setVisible(true);
			label_space_admin.setVisible(false);
			container_image_admin.setVisible(false);
			btn_examen.setStyle("");
			menubtn_ajouter_voir_examen.setVisible(false);
			label_space_admin.setVisible(true);
			container_image_admin.setVisible(true);
			btn_aide.setStyle("-fx-background-color:#4169e1");

		} else {

		}

	}

	@FXML
	void validerChoixClasse(ActionEvent event) throws SQLException {

		if (choix_classe.getValue() == null) {
			label_alert_danger_classe.setText("Veuillez choisir une classe");
		} else {
			anchor_accueil.setVisible(false);
			anchor_choix_classe.setVisible(false);
			anchor_choix_module.setVisible(true);

			anneeFiliere1 = ServiceProvider.getPromoThatWillPassExam(anneeFiliere,
					choix_classe.getItems().indexOf(choix_classe.getValue()));

			modules = ServiceProvider.getModulesOfthisUserAndOfthisAnneeFiliere(connection, ensah, anneeFiliere1,
					ControllerLogin.getInstance().getUsernameAdmin());
			// choix_module.getItems().clear();
			choix_module.setPromptText("Choisir votre module");
			Iterator<Module> iterator = modules.iterator();
			while (iterator.hasNext()) {
				choix_module.getItems().add(iterator.next().getModule());
			}

		}

	}

	@FXML
	void validerChoixModule(ActionEvent event) {

		type_examen.setPromptText("Examen Tp (true) autre (false)");
		if (choix_module.getValue() == null) {
			label_alert_danger_module.setText("Veuillez choisir un module");
		} else {
			anchor_accueil.setVisible(false);
			anchor_choix_module.setVisible(false);
			anchor_date_examen.setVisible(true);

		}

	}

	@FXML
	void validerChoixSalleControlleurExamen(ActionEvent event) {

		ArrayList<Salle> salleArraylist = new ArrayList<>();
		ArrayList<Enseignant> enseignatArrayList = new ArrayList<>();

		for (CheckBox box : sallesArrayList) {

			if (box.isSelected()) {
				salleArraylist.add(sallesDisponibleExamen.get(sallesArrayList.indexOf(box)));

			}

		}
		sallesChoisis = new ArrayList<>(salleArraylist);

		for (CheckBox box : controlleursArraylist) {

			if (box.isSelected()) {

				enseignatArrayList.add(enseignantsDisponibleExamen.get(controlleursArraylist.indexOf(box)));
			}

		}
		controlleursChoisis = new ArrayList<>(enseignatArrayList);
		anchor_salle_examen.setVisible(false);
		anchor_success.setVisible(true);

		ServiceProvider.uploadExamenToDatabase(connection, examen, sallesChoisis, controlleursChoisis);

	}

	@FXML
	void validerDateTypeExamen(ActionEvent event) {

		if (date_examen.getValue() == null) {
			label_alert_danger_date.setText("Veuillez selectioner une date d'examen");

		} else if (heure_examen.getValue() == null) {
			label_alert_danger_date.setText("Veuillez selectioner une heure d'examen");

		} else if (!verifierEntier(duree_examen.getText())) {

			label_alert_danger_date
					.setText("Verifier la duree d'examen (Indication: Entrer seulement le nombre de minutes)");

		} else if (type_examen.getValue() == null) {
			label_alert_danger_date.setText("Veuillez spécifier si vous voulez construire un examen théorique ou TP  ");

		} else {

			Module module = ServiceProvider.getModulWhereWillExamPassed(modules,
					choix_module.getItems().indexOf(choix_module.getValue()));

			examen = ServiceProvider.constructExam(label_alert_danger_date, ensah, anneeFiliere1, module,
					date_examen.getValue(), heure_examen.getValue(), Integer.parseInt(duree_examen.getText()),
					type_examen.getValue());
			System.out.println("aeezzassaa");
			if (examen == null) {
				// label_alert_danger_date.setText("Vous ne pouvez pas construire un examen .
				// Verifier les champs !!!");
				if (!ServiceProvider.verifierTempsExamen(label_alert_danger_date, ensah.getPeriodeExamen(),
						heure_examen.getValue(), Integer.parseInt(duree_examen.getText().trim()),
						date_examen.getValue())) {

				}

			} else {
				anchor_accueil.setVisible(false);
				anchor_date_examen.setVisible(false);
				anchor_salle_examen.setVisible(true);
				sallesDisponibleExamen = ServiceProvider.getSallesExamen(ensah, examen, type_examen.getValue());
				enseignantsDisponibleExamen = ServiceProvider.getControleursExamen(ensah, examen);

				for (Salle salle : sallesDisponibleExamen) {

					CheckBox cocherSalle = new CheckBox(salle.getNom_Salle());

					CustomMenuItem customMenuItem = new CustomMenuItem(cocherSalle);

					salle_examen.getItems().add(customMenuItem);
					sallesArrayList.add(cocherSalle);

				}

				for (Enseignant enseignant : enseignantsDisponibleExamen) {
					CheckBox enseignantBox = new CheckBox(enseignant.getNom() + " " + enseignant.getPrenom());

					CustomMenuItem customMenuItem = new CustomMenuItem(enseignantBox);

					controlleur_examen.getItems().add(customMenuItem);
					controlleursArraylist.add(enseignantBox);

				}

			}
		}

	}

	@FXML
	void voirExamens(ActionEvent event) {

		anchor_accueil.setVisible(false);
		anchor_info_examen.setVisible(true);
		if (listeExamens.size() > 0) {
			table_info_examen.setVisible(true);
			planning_exam_admin.setVisible(false);

		}

	}

	@FXML
	void pageAccueil(ActionEvent event) {

		anchor_accueil.setVisible(true);
		anchor_choix_classe.setVisible(false);
		anchor_choix_module.setVisible(false);
		anchor_date_examen.setVisible(false);
		anchor_salle_examen.setVisible(false);
		label_alert_danger_classe.setText("");
		label_alert_danger_date.setText("");
		label_alert_danger_salle.setText("");
		label_alert_danger_module.setText("");
		btn_accueil.fire();
		if (date_examen.getValue() != null) {
			date_examen.setValue(null);
		}
		if (heure_examen.getValue() != null) {
			heure_examen.setValue(null);
		}
		choix_classe.getItems().clear();
		choix_module.getItems().clear();
		choix_module.setPromptText("  ");
		choix_classe.setPromptText(" ");
		salle_examen.getItems().clear();
		controlleur_examen.getItems().clear();
		if (!duree_examen.getText().isEmpty()) {
			duree_examen.setText(null);

		}
		if (type_examen.getValue() != null) {
			type_examen.setValue(null);
		}
		type_examen.setPromptText(" ");
		anchor_success.setVisible(false);
		anchor_info_examen.setVisible(false);
//		planning_exam_admin.setVisible(false);

	}

	@FXML
	void choisirClasse(ActionEvent event) {

	}

	@FXML
	void choisirModule(ActionEvent event) {

	}

	@FXML
	void choisirTypeExamen(ActionEvent event) {

	}

	@FXML
	void selectionerControlleur(ActionEvent event) {

	}

	@FXML
	void selectionerSalle(ActionEvent event) {

	}

	public void ajouterIcon(String path, Button btn) {

		ImageView imageView = new ImageView(new Image(getClass().getResource(path).toString()));
		imageView.setFitWidth(20);
		imageView.setFitHeight(15);
		imageView.setStyle("-fx-border-color:white");
		btn.setGraphic(imageView);
		btn.setGraphicTextGap(10);
	}

	public static boolean verifierEntier(String isEntier) {
		isEntier = isEntier.trim();

		int i = 0;
		if (isEntier.length() == 0)
			return false;
		while (i < isEntier.length()) {
			Pattern pattern = Pattern.compile("^\\d$");

			Matcher matcher = pattern.matcher("" + isEntier.charAt(i));
			if (matcher.find() == false)
				return false;
			i++;
		}
		if (Integer.parseInt(isEntier) > 240 || Integer.parseInt(isEntier) <= 0)
			return false;

		return true;
	}

	@FXML
	void quitter() {
		btn_quitter.getScene().getWindow().hide();
	}

}