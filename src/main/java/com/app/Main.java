package com.app;

import java.io.FileInputStream;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.bo.Ecole;
import com.dao_services.ServiceProvider;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	
	
	private Connection connection;
	private Ecole ensah;
	
	@Override
	public void start(Stage primaryStage) {
		 
		
  		Properties properties = new Properties() ;

  		try {
  			properties.load( new FileInputStream("Config.properties") );
  			Class.forName( properties.getProperty("jdbc.driver.class") ) ;
  			connection = DriverManager.getConnection( properties.getProperty("jdbc.url" ) ,
  												   	 properties.getProperty("jdbc.user"),
  						     						 properties.getProperty("jdbc.password") ) ;
  		} catch ( FileNotFoundException e ) {
  			System.out.println( "Il vous manque le fichier de configuration !" );
  			return ;
  		} catch ( Exception e ) {  
  			// IOException :: NullPointerException :: SQLException.
  			System.out.println( "il n'y a pas de connexion !" );
  			return ;
  		} 
  		/*---------------------------------------------------------------------------------------------*/
  		
  		 ensah = ServiceProvider.uploadAndReturnDataOfApp(connection);	
  		 System.out.println("Ensah "+ensah);
  		 
  		 if(ensah.getPeriodeExamen()!=null) {
  			
  			  try {
  			
  			  
  			 Parent root=FXMLLoader.load(getClass().getResource("/filesfxml/Connexion.fxml"));


  			        
  			  Scene scene = new Scene(root);
  			  primaryStage.setScene(scene);
  			  
  			  primaryStage.setMinWidth(700); 
  			  primaryStage.setMaxWidth(1039);
  			  primaryStage.setMinHeight(600); 
  			  primaryStage.setMaxHeight(700);
  			  primaryStage.show(); 
  			  primaryStage.centerOnScreen();
  			  primaryStage.setTitle("Gestion de passassions d'examens");
  			  } catch(Exception e) {
  			  
  			  e.printStackTrace(); 
  			  }
  			  }else {
  				
  				  try {
  				
  				 
  				 Parent root=FXMLLoader.load(getClass().getResource("/filesfxml/ConnexionAdmin.fxml"));
                 

  				        
  				  Scene scene = new Scene(root);
  				  primaryStage.setScene(scene);
  				  
  				  primaryStage.setMinWidth(700); 
  				  primaryStage.setMaxWidth(1039);
  				  primaryStage.setMinHeight(600); 
  				  primaryStage.setMaxHeight(700);
  				  primaryStage.show(); primaryStage.centerOnScreen();
  		 	    	primaryStage.setTitle("Gestion de passassion d'examens");
  				  
  				  } catch(Exception e) {
  				  
  				  e.printStackTrace(); 
  				  
  				  }
  				 
  				  
  			  }
  			 
  			 
  		 }


	

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		

	       launch(args);
	
	
			
		
		

	}
}
	     

		
		
  
