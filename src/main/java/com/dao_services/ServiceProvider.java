package com.dao_services;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bo.Amphi;
import com.bo.AnneeFiliere;
import com.bo.Bloc;
import com.bo.CoordonnateurDepartement;
import com.bo.CoordonnateurFiliere;
import com.bo.Departement;
import com.bo.Ecole;
import com.bo.Enseignant;
import com.bo.Etudiant;
import com.bo.Examen;
import com.bo.ExamenTP;
import com.bo.ExamenTheorique;
import com.bo.Filiere;
import com.bo.Module;
import com.bo.PeriodeExamen;
import com.bo.Salle;
import com.bo.SalleBlocTP;

import javafx.scene.control.Label;


public class ServiceProvider {
	
	
	private  static boolean sent=false;

/*----------------------------------------------------------------------------------------------------------------------------------------------------*/
	
/***********************transformer le fichier xml a un document pouvant de se parser en java********************************************
  *															  
   *  														  
    */
	private static Document transformXmlToDocument(String fichier) {
		
		/*
		 *  ON CREE UN FICHIER XML QUI CONTIENT LES DONNEES DONT ON A BESOIN.
		 */
		
		File file = new File( fichier ) ; 
		
		/*
		 *  TRAITONS LE CAS SI LE FICHIER XML EST PERDU DONC ON CREE UN AUTRE QUI CONTIENT LA MEME TEMPLATE ET QU'EST BIEN SUR DEMANDEE A REMPLIR.
		 */
		
		if( !file.exists() ) {
			System.out.println( "CREATION DU TEMPLATE REQUIS A REMPLIR" );
			try {	
					  String templateInfoSystemOrSallesEcoleXML  =   switch( fichier ) {
					  
					  		case "info-system-ecole.xml" ->  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
															 + "<ecole nom = \"\" ><departement nom = \"\">"
															 + 		"<chef-departement><username></username><prenom></prenom><nom></nom><email></email></chef-departement>"
															 + 		"<filieres><filiere nom = \"\">"
															 + 				"<coordonnateur-filiere><username></username><prenom></prenom><name></name><email></email></coordonnateur-filiere>"
															 + 				"<annees><annee niveau = \"\" option = \"\" >"
															 + 						"<modules><module semestre = \"\" ><nom-module></nom-module>"
															 + 								"<enseignant><username></username><nom></nom><prenom></prenom><email></email></enseignant>"
															 + 						"</module></modules>"
															 + 						"<etudiants><etudiant><CNE></CNE><nom></nom><prenom></prenom><email></email></etudiant></etudiants>"
															 + 				"</annee></annees></filiere></filieres>"
							        						 + "</departement></ecole>" ;
															 
					  		case "info-salles-ecole.xml"  -> "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					  										 + "<ecole><blocs><bloc nom = \"\">" 
					  										 + 		"<salles-tp><salle>"
					  										 +			"<nom>salle A</nom>\r\n"
					  										 + 				"<modules>"
					  										 + 					"<module></module>"
					  										 + 				"</modules>" 		
					  										 +		"</salle></salles-tp>"
					  										 + 		"<salles-theorique><salle></salle></salles-theorique>"
					  										 + "</bloc></blocs>"
					  										 + "<amphis><amphi></amphi></amphis></ecole>" ;
					  										 
					  	    default                       -> "" ;    
					  };	   
				file.createNewFile() ;
				FileWriter remplir_file = new FileWriter(file) ;
				remplir_file.write( templateInfoSystemOrSallesEcoleXML );
				remplir_file.close() ;
			} catch (IOException e) {
				return null ;
			}
		}
		
		/*
		 * CREONS LE DOCUMENT QUI VA NOUS PERMET DE PARSER NOTRE XML.
		 */
		
		DocumentBuilderFactory facteurCreateurDocument = DocumentBuilderFactory.newInstance() ; 
		DocumentBuilder createurDocument = null ;
		Document documentInfoEcole = null ;
		try {
			createurDocument = facteurCreateurDocument.newDocumentBuilder();
			documentInfoEcole = createurDocument.parse( file ) ;
		} catch (Exception ignore) {
		} 
		return documentInfoEcole ;
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	
	
/******************* generer un password dont on va donner au professeur et au coordonnateurs *******************************************
  *
   * 
    */
	private static String genererPassword( HashSet <String> pwds ) {
		String pwd = "" ;
		Random rand = new Random() ;
		while(   ( pwd.length() < 6 )    ||    ( ! pwds.add(pwd) )   ) {
			pwd += "" + Integer.toString(   rand.nextInt( 10 )   );
			if( pwd.length() > 8 )  pwd = "" ;
		}
		return pwd ;
	}

/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	
	
/******************* enregestrer les donnees dans la base de données apres de leurs telecharger du fichier xml **************************
  *
   *  								there is 2 xml files, and this is the : info-system-ecole...
    */
	private static void uploadXmlSystemEcoleIfUpdatedToDatabase( Connection connexion ) {
		Properties properties = new Properties(  ) ;
		File fichierConfiguration  = new File( "Config.properties" ) ;
		File fichierXmlSystemEcole = new File( "info-system-ecole.xml" ) ;
		
		try {
			BufferedReader modifyConfiguration = new BufferedReader( new FileReader( fichierConfiguration ) ) ;
			properties.load( modifyConfiguration );
			
			/*
			 *  si la structure conditionnelle nous retourne true on doit quitter la fonction,
			 *  car pas de modification requise a s'enregistrer...
			 */
			
			if (  fichierXmlSystemEcole.lastModified()  ==  Long.parseLong(properties.getProperty("lastModification.xml.system.ecole"))  ) {
				return ;
			}			
			properties.setProperty(  "lastModification.xml.system.ecole" , ""+fichierXmlSystemEcole.lastModified()  ) ;
			properties.store( new FileOutputStream(fichierConfiguration) , null );
		} catch (FileNotFoundException e) {
			System.err.println( "probleme de connexion au fichier" );     return ;
		} catch (IOException e) {
			System.err.println( "probleme de connexion au fichier" );     return ;
		} catch (NumberFormatException e ) {
			try {
				properties.setProperty( "lastModification.xml.system.ecole" , ""+fichierXmlSystemEcole.lastModified()  ) ;
				properties.store( new FileOutputStream (fichierConfiguration) , null);
			} catch (IOException e1) {
				System.err.println( "probleme de connexion au fichier" ); return ;
			}
		}
		
		/*
		 *  si on arrive la on c-a-d que il ya une modification requise a s'enregistrer...
		 */
		
/*-------------------------------------------------------------------------------------------------------------------------*/

		HashSet <String> passwordCoordonnateurFiliere     = new HashSet<>() ,
				 		 passwordCoordonnateurDepartement = new HashSet<>() ,
				         passwordEnseignant 			  = new HashSet<>() ;
		Document doc = transformXmlToDocument(  "info-system-ecole.xml"  ) ;
		
		try( Statement operationDatabase = connexion.createStatement() ){
			
			// enregistrer les mots de passes des coordonnateurs des filieres dans un hashset.
			String selectPwdsCoordonnateursFiliere    = " SELECT `mot_de_passe` FROM `coordonnateurfiliere`  ;" ;
			ResultSet pwdsCoordonnateursFilieres = operationDatabase.executeQuery( selectPwdsCoordonnateursFiliere ) ;
			while( pwdsCoordonnateursFilieres.next() ) {
				passwordCoordonnateurFiliere.add( pwdsCoordonnateursFilieres.getString("mot_de_passe") ) ;
			}
			
			// enregistrer les mots de passes des coordonnateurs des departement dans un hashset.
			String selectPwdsCoordonnateursDepartement    = " SELECT `mot_de_passe` FROM `coordonnateurdepartement`  ;" ;
			ResultSet pwdsCoordonnateursDepartements = operationDatabase.executeQuery( selectPwdsCoordonnateursDepartement ) ;
			while( pwdsCoordonnateursDepartements.next() ) {
				passwordCoordonnateurDepartement.add( pwdsCoordonnateursDepartements.getString("mot_de_passe") ) ;
			}
			
			// enregistrer les mots de passes des enseignants dans un hashset.
			String selectPwdsEnseignant    = " SELECT `mot_de_passe` FROM `enseignant`  ;" ;
			ResultSet pwdsEnseignant = operationDatabase.executeQuery( selectPwdsEnseignant ) ;
			while( pwdsEnseignant.next() ) {
				passwordEnseignant.add( pwdsEnseignant.getString("mot_de_passe") ) ;
			}
		} catch (SQLException e) {
		}
		
		/**************************** ajout du departements *****************************************************************************/
			NodeList nodesDepartement = doc.getDocumentElement().getElementsByTagName("departement") ;
			int lengthDepartement     = nodesDepartement.getLength() ;	
			
			for ( int i = 0 ; i< lengthDepartement ; i++ ) {
	
			/********************** ajout du chef du departement ************************************************************************/
				NodeList nodesInfosCoordonnateurDepartement =  nodesDepartement.item(i).getChildNodes().item(0).getChildNodes() ; 

				String queryCoordonnateurDepartement        = " INSERT INTO `coordonnateurdepartement` VALUES ( '"    + nodesInfosCoordonnateurDepartement.item(0).getTextContent() + "'  ,  '"
																								          		      + nodesInfosCoordonnateurDepartement.item(1).getTextContent() + "'  ,  '"
																									                  + nodesInfosCoordonnateurDepartement.item(2).getTextContent() + "'  ,  '"
																									                  + nodesInfosCoordonnateurDepartement.item(3).getTextContent() + "'  ,  '"
																									                  + genererPassword (passwordCoordonnateurDepartement)          + "'  )  ;" ;
				try(   Statement operationDatabase = connexion.createStatement()   ) {
					operationDatabase.executeUpdate( queryCoordonnateurDepartement ) ;
				} catch (SQLException e) {
				}
						
			/// ajout des informations du departement ***********************************************************************************
			String queryDepartement     = " INSERT INTO `departement` VALUES ( '"   + nodesDepartement.item(i).getAttributes().item(0).getTextContent()                          + "'  ,  '"
																					+ nodesDepartement.item(i).getChildNodes().item(0).getChildNodes().item(0).getTextContent()  + "'  )  ;" ;
			try(   Statement operationDatabase = connexion.createStatement()   ) {
				operationDatabase.executeUpdate( queryDepartement ) ;
			} catch (SQLException e) {
			}
			
			/*********************** ajout de filieres de departement *******************************************************************/
				NodeList nodesFilieresDepartement = nodesDepartement.item(i).getChildNodes().item(1).getChildNodes() ;
				for ( int j = 0 ; j < nodesFilieresDepartement.getLength() ; j++   ) {
					
					/**************** ajout de coordonnateur de filiere  ****************************************************************/
						NodeList nodesInfosCoordonnateurFiliere  = nodesFilieresDepartement.item(j).getChildNodes().item(0).getChildNodes()  ;
						String queryCoordonnateurFiliere         = " INSERT INTO `coordonnateurfiliere` VALUES ( '"  +  nodesInfosCoordonnateurFiliere.item(0).getTextContent()  + "'  , '"
																													 +  nodesInfosCoordonnateurFiliere.item(1).getTextContent()  + "'  , '"
																													 +  nodesInfosCoordonnateurFiliere.item(2).getTextContent()  + "'  , '"
																													 +  nodesInfosCoordonnateurFiliere.item(3).getTextContent()  + "'  , '"
																													 +  genererPassword (passwordCoordonnateurFiliere)           + "'  ) ;" ; 
						try(   Statement operationDatabase = connexion.createStatement()   ) {
							operationDatabase.executeUpdate( queryCoordonnateurFiliere ) ;
						} catch (SQLException e) {
						}	
						
					/// ajout des informations du filiere ********************************************************************************	
					String queryFiliere = " INSERT INTO `filiere` VALUES ( '"  +  nodesFilieresDepartement.item(j).getAttributes().item(0).getTextContent() + "' , '"
																			   +  nodesInfosCoordonnateurFiliere.item(0).getTextContent()                   + "' , '"
																			   +  nodesDepartement.item(i).getAttributes().item(0).getTextContent()         + "' ) ;" ;
					try(   Statement operationDatabase = connexion.createStatement()   ) {
						operationDatabase.executeUpdate( queryFiliere ) ;
					} catch (SQLException e) {
					}
			
					/********************* ajout des annees scolaire ********************************************************************/
						NodeList nodeAnnees  =  nodesFilieresDepartement.item(j).getChildNodes().item(1).getChildNodes()  ;
						for ( int k = 0 ; k < nodeAnnees.getLength() ; k++ ) {
							NamedNodeMap nodeAnnee = nodeAnnees.item(k).getAttributes() ;
							String queryAnneeFiliere = " INSERT INTO `anneefiliere` VALUES ( '" + nodesFilieresDepartement.item(j).getAttributes().item(0).getTextContent()      + "-"
																								+ nodeAnnee.item(0).getTextContent() + " " + nodeAnnee.item(1).getTextContent()  + "' ,  "
																								+ nodeAnnee.item(0).getTextContent()                                             + "  , '"
																								+ nodeAnnee.item(1).getTextContent()										     + "' , '"
																								+ nodesFilieresDepartement.item(j).getAttributes().item(0).getTextContent()      + "' ) ;"  ;
							try(   Statement operationDatabase = connexion.createStatement()   ) {
								operationDatabase.executeUpdate( queryAnneeFiliere ) ;
							} catch (SQLException e) {
							}		
					
							/************ ajout des modules du chaque niveau/option  ****************************************************/
								NodeList nodeModules =  nodeAnnees.item(k).getChildNodes().item(0).getChildNodes()  ;
								for ( int r = 0 ; r < nodeModules.getLength() ; r++ ) {
								    	 
									/*********** ajout d'enseignant du module ***********************************************************/
										NodeList nodeEnseignantModule = nodeModules.item(r).getChildNodes().item(1).getChildNodes() ;
										String queryEnseignant = " INSERT INTO `enseignant` VALUES ( '" + nodeEnseignantModule.item(0).getTextContent()						+ "' , '"
																										+ nodeEnseignantModule.item(1).getTextContent() 					+ "' , '"
																										+ nodeEnseignantModule.item(2).getTextContent()   					+ "' , '"
																										+ nodeEnseignantModule.item(3).getTextContent() 					+ "' , '"
																										+ genererPassword(passwordEnseignant)							    + "' , '"
																										+ nodesDepartement.item(i).getAttributes().item(0).getTextContent() + "' ) ;" ;
										try(   Statement operationDatabase = connexion.createStatement()   ) {
											operationDatabase.executeUpdate( queryEnseignant ) ;
										} catch (SQLException e) {
										}
						
									//on ajout le module ********************************************************************
									Node nodeModule = nodeModules.item(r).getChildNodes().item(0) ;
									String queryModule = " INSERT INTO `module` VALUES ( '"  +  nodeModule.getTextContent()                  								     +  "' ,  " 
																							 +  nodeModules.item(r).getAttributes().item(0).getTextContent()					 +  "  , '"
																							 +  nodeEnseignantModule.item(0).getTextContent()									 +  "' , '"
																							 +  nodesFilieresDepartement.item(j).getAttributes().item(0).getTextContent()        +  "-"
																							 +  nodeAnnee.item(0).getTextContent() + " " + nodeAnnee.item(1).getTextContent()    +  "' ) ;"  ;
									try(   Statement operationDatabase = connexion.createStatement()   ) {
										operationDatabase.executeUpdate( queryModule ) ;
									} catch (SQLException e) {
									}	
								}
					
							/******************  ajout des etudiants de chaque Filiere / niveau / option *********************************/
								NodeList nodeEtudiantsAnneeFiliere = nodeAnnees.item(k).getChildNodes().item(1).getChildNodes() ;
								for ( int r = 0 ; r < nodeEtudiantsAnneeFiliere.getLength() ; r++ ) {
									String queryEtudiant = " INSERT INTO `etudiant` (    `CNE`   ,   `nom`   ,   `prenom`   ,   `email` , `anneefiliere`  )   "
																				+ " VALUES ( '"  +  nodeEtudiantsAnneeFiliere.item(r).getChildNodes().item(0).getTextContent()      + "' , '"
											                                                     +  nodeEtudiantsAnneeFiliere.item(r).getChildNodes().item(1).getTextContent()      + "' , '"
											                                                     +  nodeEtudiantsAnneeFiliere.item(r).getChildNodes().item(2).getTextContent()    	+ "' , '"
											                                                     +  nodeEtudiantsAnneeFiliere.item(r).getChildNodes().item(3).getTextContent()    	+ "' , '"
											                                                     +  nodesFilieresDepartement .item(j).getAttributes().item(0).getTextContent()      + "-"
																								 +  nodeAnnee.item(0).getTextContent() + " " + nodeAnnee.item(1).getTextContent()  	+ "' ) ;";
									try(   Statement operationDatabase = connexion.createStatement()   ) {
										operationDatabase.executeUpdate( queryEtudiant ) ;
									} catch (SQLException e) {
									}	
								}					
						}	
				}	
			}
	}
		
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	
	
/******************* enregistrer du fichier xml du bloc,salle,amphi... dans la base de données*******************************************
  * 
   * 								there is 2 xml files, and this is the : info-salles-ecole...
    */
	private static void uploadXmlSalleEcoleIfUpdatedToDatabase( Connection connexion ) {
		Properties properties = new Properties(  ) ;
		File fichierConfiguration  = new File( "Config.properties" ) ;
		File fichierXmlSallesEcole = new File( "info-salles-ecole.xml" ) ;
		try {
			BufferedReader modifyConfiguration = new BufferedReader( new FileReader( fichierConfiguration ) ) ;
			properties.load( modifyConfiguration );
			
			/*
			 *  si la structure conditionnelle nous retourne true on doit quitter la fonction,
			 *  car pas de modification requise a s'enregistrer...
			 */
			
			if (  fichierXmlSallesEcole.lastModified()  ==  Long.parseLong(properties.getProperty("lastModification.xml.salles.ecole"))  ) {
				return ;
			}			
			properties.setProperty(  "lastModification.xml.salles.ecole" , ""+fichierXmlSallesEcole.lastModified()  ) ;
			properties.store( new FileOutputStream(fichierConfiguration) , null );
		} catch (FileNotFoundException e) {
			System.err.println( "probleme de connexion au fichier" );     return ;
		} catch (IOException e) {
			System.err.println( "probleme de connexion au fichier" );     return ;
		} catch (NumberFormatException e ) {
			try {
				properties.setProperty( "lastModification.xml.salles.ecole" , ""+fichierXmlSallesEcole.lastModified()  ) ;
				properties.store( new FileOutputStream (fichierConfiguration) , null);
			} catch (IOException e1) {
				System.err.println( "probleme de connexion au fichier" ); return ;
			}
		}
		
		/*
		 *  si on arrive la c-a-d une modification a lieu le fichier xml des salles de l'ecole, on doit la mettre au base données.
		 */
	
/*-------------------------------------------------------------------------------------------------------------------------*/
		
		Document doc = transformXmlToDocument(  "info-salles-ecole.xml"  ) ;
		
		NodeList blocs   =  doc.getElementsByTagName( "blocs" ).item(0).getChildNodes() ;
		NodeList amphis  =  doc.getElementsByTagName( "amphis" ).item(0).getChildNodes() ;
		
		/*
		 * on va maintenant prendre les donnees du chaque bloc afin de leur enregistrer dans la base de données
		 */
		
			for (    int i = 0    ;    i < blocs.getLength()    ;    i++    ) {
				Node bloc  	      =  blocs.item(i) ;
				String nomBloc    =  bloc.getAttributes().item(0).getTextContent() ;
				
				String queryBloc  =  " INSERT INTO `bloc` VALUES ( '"   + nomBloc +   "' ) ;" ;
				
				try(   Statement operationDatabase = connexion.createStatement()   ) {
					operationDatabase.executeUpdate( queryBloc ) ;
				} catch (SQLException e) {
				}
				
				/*
				 * on va ajouter les salles TP/Theorique...
				 */
				
				NodeList sallesTP     =  bloc.getChildNodes().item(0).getChildNodes() ;
				//salle tp::
				for (   int j = 0   ;   j < sallesTP.getLength()   ;   j++   ) {
					Node salleTP  =  sallesTP.item(j).getChildNodes().item(0) ;
					String querySalleTP  =  " INSERT INTO `salle` VALUES (  '"   + salleTP.getTextContent() +  nomBloc +   "'  ,  '"
																			  	 + salleTP.getTextContent()			   +   "'  ,   "
																			     + 1		        				   +   "   ,  '"
																			     + nomBloc							   +   "'  ,   " 
																				 + 0    							   +   "   )  ;" ;
					try(   Statement operationDatabase = connexion.createStatement()   ) {
						operationDatabase.executeUpdate( querySalleTP ) ;
					} catch (SQLException e) {
					}
					
					NodeList modulesCorrespondantAuSalleTP = sallesTP.item(j).getChildNodes().item(1).getChildNodes() ;
					for( int k = 0   ;   k < modulesCorrespondantAuSalleTP.getLength()  ;  k++ ) {
						String queryModule = " INSERT INTO `salleexamentpmodule` VALUES ( '"  +	 modulesCorrespondantAuSalleTP.item(k).getTextContent() + salleTP.getTextContent() +  nomBloc + "'  ,  '"
																							  +  modulesCorrespondantAuSalleTP.item(k).getTextContent() 						   			  + "'  ,  '"
																							  +  salleTP.getTextContent() +  nomBloc                     						   			  + "'  )  ;"  ;
						try(   Statement operationDatabase = connexion.createStatement()   ) {
							operationDatabase.executeUpdate( queryModule ) ;
						} catch (SQLException e) {
						}						
					}
					
				}
				
				NodeList sallesTheorique     =  bloc.getChildNodes().item(1).getChildNodes() ;
				//salle theorique ::
				for (   int j = 0   ;   j < sallesTheorique.getLength()   ;   j++   ) {
					Node salleTheorique  =  sallesTheorique.item(j) ;
					
					String querySalleTheorique  =  " INSERT INTO `salle` VALUES (  '"   + salleTheorique.getTextContent() +  nomBloc +   "'  ,  '"
																			  	 		+ salleTheorique.getTextContent()			 +   "'  ,  '"
																			  	 		+ 0		        				  			 +   "'  ,  '"
																			  	 		+ nomBloc							         +   "'  ,   " 
																						+ 0    							   			 +   "   )  ;" ;
					try(   Statement operationDatabase = connexion.createStatement()   ) {
						operationDatabase.executeUpdate( querySalleTheorique ) ;
					} catch (SQLException e) {
					}
				}
			}
			
		/*
		 * on va maintenant prendre les donnees du chaque amphi afin de leur enregistrer dans la base de données
		 */	

			for (    int i = 0    ;    i < amphis.getLength()   ;   i++    ) {
				Node amphi         =  amphis.item(i) ;
				String nomAmphi    =  amphi.getTextContent() ;
				
				String queryAmphi  =  " INSERT INTO `salle` (`id` , `nomSalle` , `pourTP` , `isAmphi` ) VALUES (  '"   + nomAmphi +   "' , '"
																													   + nomAmphi +   "' ,  "
																													   + 0        +   "  ,  "
																													   + 1        +   "  ) ;" ;
				try(   Statement operationDatabase = connexion.createStatement()   ) {
					operationDatabase.executeUpdate( queryAmphi ) ;
				} catch (SQLException e) {
				}
			}
	}

/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	

/***************** telecharger les donnees de la base de données et construire un objet ecole qu'on va utiliser *************************
  * 
   * 
    */
	private static Ecole downloadDataFromDatabaseToConstructSchoolObject(  Connection connexion  ) {

		 Ecole ensah = new Ecole( "Ecole nationale des sciences appliquees" ) ;
		 
		 try {
			 Statement declarations = connexion.createStatement() ;

			 /*
			  * premierement on va creer les salles, amphi, bloc ... en respectant les types (tp, theorique)...
			  */
			 
			declarations = connexion.createStatement() ;
			ResultSet amphis = declarations.executeQuery(  "SELECT (`nomSalle`) FROM `salle` WHERE isAmphi = " + 1 + " ;"  ) ;
			while ( amphis.next() ) {
				ensah.ajouterAmphi( new Amphi( amphis.getString( "nomSalle" ) ) ) ;
			}
			
			declarations = connexion.createStatement() ;
			ResultSet blocs  = declarations.executeQuery(  "SELECT * FROM `bloc` ;"  )   ;
			while ( blocs.next() ) {
				Bloc bloc = new Bloc( blocs.getString("nomBloc") ) ;
				
				declarations = connexion.createStatement() ; 
				ResultSet salles = declarations.executeQuery( "SELECT `nomSalle`,`pourTP` FROM `salle` WHERE `bloc` = '" + bloc.getNomBloc() + "' ;" ) ;
				while ( salles.next() ) {
						Boolean pourTP = switch(   salles.getString( "pourTP" )   ) {  case "1" -> true; /**/ default  -> false;  } ;
						bloc.ajouterSalleBloc( salles.getString( "nomSalle" ) , pourTP );
				}
				ensah.ajouterBloc( bloc ) ;
			}

			/*
			 * telecharger la periode d'examen de la base de données afin de creer un objet ...
			 */

			declarations = connexion.createStatement() ;
			ResultSet periodeExamenResultDatabase = declarations.executeQuery(  "SELECT * FROM `periodeexamen`  "  ) ;
			periodeExamenResultDatabase.next () ;
			
			ensah.ajouterPeriodeExamen( new PeriodeExamen(periodeExamenResultDatabase.getDate("date_debut").toLocalDate() ,
										periodeExamenResultDatabase.getDate( "date_fin" ).toLocalDate() ,
										ensah)  )   ;

			/*
			 * apres la construction des salles, on va maintenant creer les departement de l'ecole...
			 */
			
			declarations = connexion.createStatement() ;
			ResultSet departements = declarations.executeQuery(  "SELECT * FROM `departement` ;"  ) ;
						
			while ( departements.next() ) {
				declarations = connexion.createStatement() ;
				
			
				ResultSet chefDepartement = declarations.executeQuery(  "SELECT * FROM `coordonnateurdepartement` WHERE `username` = '" + departements.getString("chefDepartement") + "' ;" ) ;
				chefDepartement.next() ;
				
				
				ArrayList<Filiere> filieresDepartement = new ArrayList<>() ; 
				
				/*
				 *  on cree maintenant les filieres de chaque departement...
				 */
				
				declarations = connexion.createStatement() ;
				ResultSet filieres = declarations.executeQuery(  "SELECT * FROM `filiere` WHERE `departementContenant` = '" + departements.getString( "nomDepartement" ) + "' ;"  ) ;
				
				      
				       
				while ( filieres.next()) {
				
				
					
					declarations = connexion.createStatement() ;
				
				

					ResultSet chefFiliere = declarations.executeQuery( "SELECT * FROM `coordonnateurfiliere` WHERE `username` = '" + filieres.getString("usernameCoordonnateur") + "' ;" ) ;
					chefFiliere.next() ;
					
					ArrayList<AnneeFiliere> arrAnneesFiliere = new ArrayList<>() ;
					
					/*
					 * creation des annees filiere...
					 */
					
					declarations = connexion.createStatement() ;
					ResultSet anneesFiliere = declarations.executeQuery(  "SELECT * FROM `anneefiliere` WHERE `filiere` = '" + filieres.getString("nomFiliere") + "' ;"  ) ;
					
					while (  anneesFiliere.next()  ) {
						ArrayList<Etudiant> etudiantsAnneeFiliere = new ArrayList<>() ;
						ArrayList<Module>   modulesAnneeFiliere   = new ArrayList<>() ;
						/*
						 * creation des etuidants de chaque annee-filiere...
						 */
						
						declarations = connexion.createStatement() ;
						ResultSet etudiants = declarations.executeQuery(  "SELECT * FROM `etudiant` WHERE `anneefiliere` = '" + anneesFiliere.getString( "id" ) + "' ;" ) ;
						while ( etudiants.next() ) {
							etudiantsAnneeFiliere.add( new Etudiant( etudiants.getString( "CNE" ) , etudiants.getString( "nom" ) , etudiants.getString( "prenom" ) , etudiants.getString( "email" ) ) ) ;
						}
						
						/*
						 * les modules etudiés par chaque annee-filiere...
						 */
						
						declarations = connexion.createStatement() ;
						ResultSet modules = declarations.executeQuery(  "SELECT * FROM `module` WHERE `anneefiliere` = '" + anneesFiliere.getString( "id" ) + "' ;"  ) ;
						while ( modules.next() ) {
							
							/*
							 * creation des professeurs de chaque module de cette annee-filiere...
							 */
							
							declarations = connexion.createStatement() ;
							ResultSet enseignantModule = declarations.executeQuery(  "SELECT * FROM `enseignant` WHERE `username` = '" + modules.getString( "enseignantmodule" ) + "' and `departement` = '" + departements.getString("nomDepartement") + "' ;" ) ;
							enseignantModule.next() ;
							
							Module module =  new Module(    modules.getString( "nomModule" ) ,
								 	  						new Enseignant( enseignantModule.getString( "username" ) , enseignantModule.getString( "nom" ) , enseignantModule.getString( "prenom" ) , enseignantModule.getString( "email" ) ) ,
								 	  						modules.getInt( "semestre" )    ) ;
							
							modulesAnneeFiliere.add( module  ) ;
							
							/*
							 * creation de salle tp de ce module si il s'utilise...
							 */

							// on telecharge dabord de la base de donnees les salle tp de ce module...
							declarations = connexion.createStatement() ;
							String querySalleTPDeModule = " SELECT `id_salle` FROM `salleexamentpmodule` WHERE `module` = '" + modules.getString( "nomModule" ) + "' ;" ; 
							ResultSet SalleTPDeModule = declarations.executeQuery( querySalleTPDeModule ) ;
							// on parcourt chaque salle pour prendre le bloc et le nom de la salle...
							while ( SalleTPDeModule.next() ) {
								
								declarations = connexion.createStatement() ;
								String queryBlocSalleTP = " SELECT `nomSalle` , `bloc` FROM `salle` WHERE `pourTP` = 1 AND `id` = '"  + SalleTPDeModule.getString( "id_salle" ) +  "' ; " ;
								ResultSet blocSalleTP = declarations.executeQuery(queryBlocSalleTP) ;
								// ici on ajoute chaque salle...
								while ( blocSalleTP.next() ) {		
									
									Bloc bloc = null ;
									Iterator <Bloc> iterateurBloc = ensah.getBlocs().iterator() ;
									while (  iterateurBloc.hasNext()  ) {
										Bloc bloc_ = iterateurBloc.next() ;
										if(    bloc_.getNomBloc().equals(blocSalleTP.getString("bloc"))    ) {
											bloc = bloc_ ;
											break ;
										}	
									}
									
									SalleBlocTP salleTP = null ;
									Iterator <SalleBlocTP> iterateurSalleTP = bloc.getSallesTP().iterator() ;
									while (  iterateurSalleTP.hasNext()  ) {
										SalleBlocTP salleTP_ = iterateurSalleTP.next() ;
										if(    salleTP_.getSalle().equals(blocSalleTP.getString( "nomSalle" ))    ) {
											salleTP = salleTP_ ;
											break ;
										}
									}
									
									module.ajouterSalleTPModule(  salleTP  );
								}
							}
						}
						arrAnneesFiliere.add(  new AnneeFiliere(  anneesFiliere.getInt("niveau") , anneesFiliere.getString( "optionfiliere" ) , modulesAnneeFiliere , etudiantsAnneeFiliere )  ) ;
					
						
						
					}
					filieresDepartement.add(   new Filiere(    filieres.getString("nomFiliere") ,
											   				   new CoordonnateurFiliere(chefFiliere.getString("username"),chefFiliere.getString("nom"),chefFiliere.getString("prenom"),chefFiliere.getString("email")   ) ,
											   				   arrAnneesFiliere     )    ) ;	
					
					/*
					 * creer des examens des annees de la filiere en cours , existant dans la base de données ...
					 */
					
					declarations = connexion.createStatement() ;
					ResultSet examenAnneeFiliere  =  declarations.executeQuery(  " SELECT * FROM `examen` ; "   ) ; 
					
					while ( examenAnneeFiliere.next() ) {
						
						/*
						 * enregistrer l i-eme examen de la base de données a un objet...
						 */
						
						declarations = connexion.createStatement() ;
						ResultSet examen_AnneeFiliere  =  declarations.executeQuery(  " SELECT * FROM `anneefiliere` WHERE id = '" + examenAnneeFiliere.getString("anneeFiliere") + "' ; "   ) ; 
						examen_AnneeFiliere.next() ;
						
						/*
						 * l annee filiere qui va passer l'examen qu'on est entraina enregistrer...
						 * 
						 */

						
						AnneeFiliere annee_  =  arrAnneesFiliere.stream()
						.filter(  annee -> {
							try {
							return annee.getNiveau() == examen_AnneeFiliere.getInt("niveau")  			      &&
									   annee.getOption().equals(examen_AnneeFiliere.getString( "optionfiliere" ))  ;
								
							} catch (SQLException e1) {
							}
							
							return false ;
							
						}  ).collect(   Collectors.toList()   ).get(0) ;
					
//						/*
//						 * module a passer en examen a enregistrer...
					
						
						Module module_ = annee_.getModules().stream()
						.filter( module -> {
							try {

								return module.getModule().equals( examenAnneeFiliere.getString( "module" ) );
							} catch (SQLException e) {
							}
							return false ;
						} ).collect(    Collectors.toList()   ).get(0) ;
						
						Examen examen_ = switch (  examenAnneeFiliere.getInt( "pourTP" )  ) {
							case 1  -> new ExamenTP 	  ( annee_  ,  module_  ,  examenAnneeFiliere.getDate( "dateExamen" ).toLocalDate() , examenAnneeFiliere.getTime( "timeExamen" ).toLocalTime()  ,  examenAnneeFiliere.getInt( "duree" )  ) ;
							default -> new ExamenTheorique( annee_  ,  module_  ,  examenAnneeFiliere.getDate( "dateExamen" ).toLocalDate() , examenAnneeFiliere.getTime( "timeExamen" ).toLocalTime()  ,  examenAnneeFiliere.getInt( "duree" )  ) ;
						} ;
												
						switch (   examenAnneeFiliere.getInt( "pourTP" )   ) {
							case 1  -> ensah.getPeriodeExamen().ajouterExamen( examen_ , true );
							default -> ensah.getPeriodeExamen().ajouterExamen( examen_ , false);
							
						}
						
						declarations = connexion.createStatement() ;
						ResultSet salles_examen_  =  declarations.executeQuery(  " SELECT * FROM `salleexamen` WHERE examen = '" + examenAnneeFiliere.getInt("id") + "' ; "   ) ; 
						ArrayList<Salle> salles_to_return = new ArrayList<>();
						
                       while( salles_examen_.next() ) {
							String id_salle_ = salles_examen_.getString("salle") ;
							salles_to_return.add( ensah.getSalles().stream().filter( salle___->salle___.getIdSalle().equalsIgnoreCase(id_salle_) ).collect(Collectors.toList()).get(0) ) ;			
						}
						
						examen_.reserverSalleExamen( salles_to_return ) ;
						
					}
				}
				
				/*
				 * ajouter toutes ces données a l'objet ecole(ensah)... pour le retourner, afin de l'exploiter uterieurement...
				 */
				
				ensah.ajouterDepartement(   new Departement( 		departements.getString( "nomDepartement" ) , 
											    			   		new CoordonnateurDepartement( chefDepartement.getString( "username" ) , chefDepartement.getString( "nom" ) , chefDepartement.getString( "prenom" ) , chefDepartement.getString( "email" )  ) ,
												    		   		filieresDepartement         )    ) ;
			}        
			
		} catch (SQLException e) {
			
			
		
		}
		 
		 /*
		  * on retourne l'objet ecole construit afin de l'exploiter, pour qu'on en tire chaque donnée on l'aura besoin a construire un tel examen...
		  */
		 
		 return ensah ;
	}	

/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	
	
/****************** the start method of this app it will call all the methods required of the *******************************************
  * 
   * 
    */
	public static Ecole uploadAndReturnDataOfApp (  Connection connexion  ) {
		uploadXmlSystemEcoleIfUpdatedToDatabase( connexion ) ;
		uploadXmlSalleEcoleIfUpdatedToDatabase( connexion ) ;
		return downloadDataFromDatabaseToConstructSchoolObject( connexion ) ;
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	

/***************** on ajoute les données de peride d'examen si elle n'y a pas d'une periode deja dans la base de données ****************
  * 	
   *   
    */
	public static boolean addExamPeriodIfIsNotBeing(Connection connexion, LocalDate dateDebut,LocalDate dateFin/*String username,String password*/){
		
		if(  dateDebut.isAfter(dateFin)  ||  dateDebut.isBefore( LocalDate.now() )  ) {
			System.out.println(  "verifier la date !"  );
			return false ;
		}
		
		Statement declaration ;
		
		/*
		 * verifier si ce personne dont les informations(username,password) ont d'un coordonnateur de filiere/departement...
		 */
		
//		try {
//			declaration = connexion.createStatement() ;
//			ResultSet admin  =  declaration.executeQuery(  "SELECT * FROM `coordonnateurdepartement` WHERE `username` = '" + username + "' AND `mot_de_passe` = '" + password + "' ;"   )  ;
//			if (  !admin.next()  ) {
//				throw new SQLException() ;
//			}
//		} catch (SQLException e) {
//			try {
//				declaration = connexion.createStatement() ;
//				ResultSet admin  =  declaration.executeQuery(  "SELECT * FROM `coordonnateurfiliere` WHERE `username` = '" + username + "' AND `mot_de_passe` = '" + password + "' ;"   )  ;
//				if (  !admin.next()  ){
//					throw new SQLException() ; 
//				}
//			} catch (SQLException e1) {
//				System.out.println(  "vous n'etes pas coordonnateur departement ! "  ) ;
//				System.out.println(  "vous n'etes pas coordonnateur de filiere ! "  ) ;
//				System.out.println(  "vous douvez etre soit chef de departement soit chef de filiere pour que vous pouvez effectuer cette tache... !"  );
//				return false ;
//			}
//		}
		
		/*
		 * si on arrive la alors elle n'y a pas de periode, alors on l'ajoute... !
		 */

		try {
			declaration = connexion.createStatement() ;
			declaration.executeUpdate( "INSERT INTO `periodeexamen` ( `date_debut` , `date_fin` ) VALUES ( '" + dateDebut + "' , '" + dateFin + "' )  ;" ) ;
		} catch (SQLException e) {
			return false ;
		}
		
		/*
		 * on verifie si cette periode est bien ajoutée au base de données...
		 */
		
		try {
			
			declaration = connexion.createStatement() ;
			ResultSet periodeExamen = declaration.executeQuery(  "SELECT * FROM  `periodeexamen` "  ) ;
			boolean test=periodeExamen.next();
			
			if( test ) {
				System.out.println(  "la periode est bien ajoutée au base de données. "  );
				return true ;	
			}

		} catch( SQLException ignore ) {
		}
		return false ;
		
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	
	
/***************** on verifie d'abord si l'utilisateur existe ou pas ********************************************************************
  * 
   * 
    */
	public static boolean verifyAccountOfUser ( Connection connexion , String username , String password ) {
		Statement declaration;
		try {
			declaration = connexion.createStatement();
			ResultSet utilisateur_cdepartement    = declaration.executeQuery( "SELECT * FROM `coordonnateurdepartement` WHERE `username` = '" + username + "' AND `mot_de_passe` = '" + password + "' ; " ) ;
			if (  !utilisateur_cdepartement.next()  ) {
				/*------------------------------------*/
				declaration = connexion.createStatement() ;
				ResultSet utilisateur_cfiliere    = declaration.executeQuery( "SELECT * FROM `coordonnateurfiliere` WHERE `username` = '" + username + "' AND `mot_de_passe` = '" + password + "' ; " ) ;
				if (  !utilisateur_cfiliere.next()  ) {
					/*------------------------------------*/
					declaration = connexion.createStatement() ;
					ResultSet utilisateur_enseignant  = declaration.executeQuery( "SELECT * FROM `enseignant` WHERE `username` = '" + username + "' AND `mot_de_passe` = '" + password + "' ; " ) ;
					if (  !utilisateur_enseignant.next()  ) {
						
						return false;
					} else {
						System.out.println( " vous etes verifie, enseignant " );
						return true ;
					}	
				} else {
					System.out.println( " vous etes verifie, chef de filiere " );
					return true  ;
				}
			}
			System.out.println( " vous etes verifie, chef de departement " );
			return  true;
		} catch (SQLException e) {
			return false ;
		}
	}
	
	
/******************************************Verifier Compte Etdudiant    ***************************/
	
	public static boolean verifyAccountOfStudent ( Connection connexion , String username , String password ) {
		Statement declaration;
		try {
			declaration = connexion.createStatement() ;
			ResultSet utilisateur_etudiant  = declaration.executeQuery( "SELECT * FROM `etudiant` WHERE `CNE` = '" + username + "' AND `mot_de_passe` = '" + password + "' ; " ) ;
			if (  !utilisateur_etudiant.next()  ) {									
				//System.out.println( " vous ne pouvez pas vous connecter sauf si vous etes soit un chef de departement/filiere, soit enseignant, soit etudiant !" );
				return false ;
			} else {
	                return true ;
			}
		} catch (SQLException e) {
			 return false;
		}

	}
	
/**************************************************************************************************/	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	

/***************** on va creer une methode permettant de nous rendre une liste designant les annees filiere *****************************
  *
   *               de ce personne si il appartient au systeme d'ecole (chef de departement/filiere, prof)...
    */
	public static ArrayList<AnneeFiliere> getAnneesFiliereOfThisUser(  Ecole ecole , String username ) {
		ArrayList <AnneeFiliere> anneesFiliereRetournees = new ArrayList<>() ;
		
		/*
		 *  on verifie d'abord si ce personne et un coordonnateur de departement ...
		 */
		
		ArrayList<Departement> departements = new ArrayList<>(  
												                ecole.getDepartements().stream()
												                	 .filter(  departement -> departement.getCoordonnateur().getUsername().equalsIgnoreCase(username) )
												                	 .collect( Collectors.toList() )
											  ) ;
		if( 	departements.size()   >   0      ) {
			    departements.forEach(  departement -> departement.getFilieres()
													             .forEach(  filiere -> anneesFiliereRetournees.addAll(filiere.getAnnees())  )  );
			    return anneesFiliereRetournees  ;
		}
		
		/*
		 *  on verifier si il est un coordonnateur filiere ...
		 */
		
		ArrayList<Filiere> filieres = new ArrayList<>( ) ;
		ecole.getDepartements().stream()
	  	     .map( departement -> departement.getFilieres().stream()
	  			   				   		     .filter(   filiere -> filiere.getCoordonnateur().getUsername().equalsIgnoreCase(username) )
	  			   				   		     .collect(   Collectors.toList()   )                )
	  	     .collect(  Collectors.toList()  )
	  	     .forEach(  filieresHasthisCoordonnateur ->  filieres.addAll(  filieresHasthisCoordonnateur  )  ) ;
		
		if( 	filieres.size()   >   0 	) {
			    filieres.forEach(    filiere -> anneesFiliereRetournees.addAll( filiere.getAnnees() )    );
			    return anneesFiliereRetournees ;
		}
		
		/*
		 * mais si on arrive la alors ce utilisateur n'est ni chef de departement ni chef de filiere il peut etre un professeur on verifie ca alors...
		 */
				
		ecole.getDepartements().forEach(   departement -> departement.getFilieres( )
																	 .forEach(   filiere -> filiere.getAnnees( )
																			 					   .forEach(  annee  -> annee.getEnseignants()
																			 							   					 .forEach(  enseignant -> {  if(  enseignant.getUsername().equalsIgnoreCase(username)  )  
																			 							   						 							  anneesFiliereRetournees.add(annee) ; } 
																			 							   					  )
																			 						)
																	 )   
		);
		return anneesFiliereRetournees ;
	}

/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	
	
/***************** on va recuperer les module que cet utilisateur a les previlege d'y ajouter des examens.. *****************************
  * 
   * 
    */
	public static ArrayList<Module> getModulesOfthisUserAndOfthisAnneeFiliere ( Connection connexion , Ecole ecole , AnneeFiliere annee , String username  ) {
		
		try {
			Statement declarations = connexion.createStatement() ; 
			ResultSet result = declarations.executeQuery(  "SELECT * FROM `coordonnateurdepartement` WHERE `username` = '" + username + "' ;"  ) ;
			
			Statement declarations_ = connexion.createStatement() ; 
			ResultSet result_ = declarations_.executeQuery( "SELECT * FROM `coordonnateurfiliere` WHERE `username` = '" + username + "' ;" ) ;
			if( result.next() || result_.next() )
				return annee.getModules() ;
		} catch (SQLException e) {
		}
		
		ArrayList<Module> modulesRetournes = new ArrayList<>( ) ;
		
		modulesRetournes.addAll(   annee.getModules().stream()
				 					    .filter(  module -> module.getEnseignantModule().getUsername().equalsIgnoreCase(username)  )
				 						.collect( Collectors.toList() )    ) ;
		
		return modulesRetournes ;
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	

/*************** on cree une methode qu'on va la donner en paramettre un entier qui designe l annee filiere qu'on desire y passer *******
  *
   * 
    */
	public static AnneeFiliere getPromoThatWillPassExam ( ArrayList<AnneeFiliere> anneeFiliereOfThisUser , int anneeFiliere ) {
		AnneeFiliere anneeQuiVaPasserExamen = anneeFiliereOfThisUser.get(anneeFiliere) ; // on va choisir quellefiliere parmis ceux indixées par l 'indice 0,1... selon la langueur du list... 
		
		return anneeQuiVaPasserExamen ;
	}

/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	

/**************** on choisi le module ou on va passer l'examen **************************************************************************
  *
   * 
    */
	public static Module getModulWhereWillExamPassed ( ArrayList<Module> modules , int module ) {
		Module moduleOuOnVaPasserExamen = modules.get( module ) ; // on va choisir quel module parmis ceux indixées par l'indice 0,1,... selon la langueur du list...
		
		return moduleOuOnVaPasserExamen ;
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	

/**************** on construit un examen en choisissant l heure et la date **************************************************************
  * 	
   *
    */
	public static Examen constructExam (Label message, Ecole ecole , AnneeFiliere annee , Module module , LocalDate date , LocalTime time , int dureeMinutes , boolean pourTP ) {
		Examen examen = null ;		
		if( pourTP == true ) {
			examen = new ExamenTP(  annee , module , date , time , dureeMinutes  ) ;
			if(ServiceProvider.existsIntersectionTime(message, ecole, examen)) {
				return null;
			}
			if ( ecole.getPeriodeExamen().ajouterExamen(examen, true,true) )  
				return examen ;
		}
		if( pourTP == false ) {
			examen = new ExamenTheorique( annee , module , date , time , dureeMinutes ) ;
			if(ServiceProvider.existsIntersectionTime(message, ecole, examen)) {
				return null;
			}
			if ( ecole.getPeriodeExamen().ajouterExamen(examen, false,true) )
				return examen ;
		}
		ecole.getPeriodeExamen().getExamens().add( examen ) ;
		System.out.println( " vous ne pouvez pas construire cet examen !" );
		return null ;
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	
	
/**************** recuperer les salles libre pour qu'on y passe l examen ****************************************************************
  * 
   * 
    */
	public static ArrayList<Salle> getSallesExamen( Ecole ecole , Examen examen , boolean pourTP  ) {
		ArrayList <Salle> sallesDisponiblesExamen = null ;
		if( pourTP == true  )
			sallesDisponiblesExamen = new ArrayList<Salle> ( ecole.getPeriodeExamen().sallesExamenTP(examen) ) ;  
		if( pourTP == false )
			sallesDisponiblesExamen = new ArrayList<Salle> ( ecole.getPeriodeExamen().sallesExamenTheorique(examen) ) ; 
		return sallesDisponiblesExamen ;
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	
	
/**************** recuperer les enseignants qui peuvent se charger de surveiller un examen **********************************************
  * 
   * 
    */
	public static ArrayList<Enseignant> getControleursExamen( Ecole ecole , Examen examen ) {
		return ecole.getPeriodeExamen().enseignantsDisponiblsToControl(examen) ;
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	

/**************** upload les informations de construction de l'examen au base de données ************************************************
  * 
   * 
    */
	public static void uploadExamenToDatabase ( Connection connexion , Examen examen , ArrayList<Salle> salles , ArrayList<Enseignant> enseignants ) {
		String anneeFiliere = examen.getAnneeFiliere().getAnneeFiliere()   ;
		Statement declaration  ;
		
		/*
		 * on va ajouter les informations de cet examen...
		 */
		
		try {
			declaration  =  connexion.createStatement() ;
			declaration.executeUpdate(  "INSERT INTO `examen` ( `anneeFiliere` , `module` , `dateExamen` , `timeExamen` , `duree` , `pourTP` ) " +
									    " VALUES ( '" +  anneeFiliere  				     +"' , '"
													  +  examen.getModule().getModule()  +"' , '"
													  +  examen.getDate()			     +"' , '"
													  +  examen.getTempsDebut()		     +"' ,  "
													  +  examen.getDuree()               +"  ,  "
													  +  examen.pourTP()                 +"  ) ;"  ) ;
			
		}   catch (SQLException e)   {
			return ;
		}
		
		/*
		 * maintenant on va ajouter les salles et les enseignants ...
		 */
		
		try {
			declaration  =  connexion.createStatement() ;
			ResultSet id_examen = declaration.executeQuery( "SELECT `id` FROM `examen` WHERE `anneeFiliere` = '" + anneeFiliere + "' AND `dateExamen` = '" + examen.getDate() + "' AND `timeExamen` = '" + examen.getTempsDebut() + "' ; "  )  ;
			id_examen.next() ;
			String idExamen = id_examen.getString( "id" );
			
			salles.forEach(  salle  ->  {  enseignants.forEach( enseignant  -> {   
																	   try {
						     											   Statement declaration_  =  connexion.createStatement() ;
																		   declaration_.executeUpdate(  "INSERT INTO `salleexamen` ( `examen` , `salle` , `enseignantcontroleur` ) " +
																								        " VALUES ( '"  +  idExamen                  +  "'  ,   '"
																								     			       +  salle.getIdSalle()        +  "'  ,   '"
																								     			       +  enseignant.getUsername()  +  "'  )   ;"   )  ;
																	   } catch (SQLException e) {
																	   }
													   }
								   		   )  ;
						     }
			) ;
		}   catch (SQLException e)   {
		}
		
		examen.reserverSalleExamen( salles ) ;
		examen.ajouterControleurs( enseignants ) ;
		
		sendEmailsStudents ( examen ) ;
		
	}

/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	

/**************** recuperer tous les examens de chaque etudiant *************************************************************************
  * 
   * 
    */
	public static ArrayList<Examen> recupererInfoEspaceEtudiant ( Ecole ecole , String CNEetudiant ) { 
		ArrayList<Examen> examensEtudiant = new ArrayList<>( 
				ecole.getPeriodeExamen().getExamens().stream()
				     					.filter(   examen -> examen.getAnneeFiliere().getEtudiants().stream()
						   										  					 .map( etudiant -> etudiant.getCNE() )
						   										  					 .anyMatch( cne -> cne.equals(CNEetudiant) )    )
				   												  					 .collect(   Collectors.toList()   )	
	    ) ;
		return examensEtudiant ;
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	
	
/**************** recuperer tous les examens de chaque admin( chef departement , chef filiere , enseignant ) ****************************
  * 
   * 	
    */
	public static ArrayList<Examen> recupererExamenControleur ( Ecole ecole , String usernameControleur ) {
		ArrayList<Examen> examensOfControleurs = new ArrayList<>( 
				ecole.getPeriodeExamen().getExamens().stream()
										.filter(   examen -> examen.getEnseignantsControleurs().stream()
														   			.map(  controleur -> controleur.getUsername()  )
														   			.anyMatch(  username -> username.equals(usernameControleur)  ))
														   			.collect( Collectors.toList() ) 
		) ;
		
		return examensOfControleurs ;
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	

/*************** recuperer les examens qui se passeront au modules dont le username du professeur est passe en parametre ****************
  * 
   * 
    */	
	public static ArrayList<Examen> recupererExamenEnseignant( Ecole ecole , String usernameEnseignant ) {
		ArrayList<Examen> examens = new ArrayList<>(
			ecole.getPeriodeExamen().getExamens().stream()
									.filter( exam -> exam.getModule().getEnseignantModule().getUsername().equals( usernameEnseignant ) )
									.collect( Collectors.toList()) 
	    ) ;
		
		return examens ;
	}

/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	

	
/*************** recuperer les examens qui se passeront au filiere dont le username du coordonnateur est passe en parametre *************
  * 
   * 
    */
	public static ArrayList<Examen> recupererExamenCoordonnateurFiliere ( Ecole ecole , String usernameCFiliere ) {
		ArrayList <Examen> examens = new ArrayList<>(
				ecole.getPeriodeExamen().getExamens().stream()
										.filter( exam -> exam.getAnneeFiliere().getFiliere().getCoordonnateur().getUsername().equals( usernameCFiliere ) )
										.collect( Collectors.toList() ) 
		) ;
		
		return examens ;
	}

/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	
	
/*************** recuperer les examens qui se passeront au departement dont le username du coordonnateur est passe en parametre *********
  * 
   * 
    */
	public static ArrayList<Examen> recupererExamenCoordonnateurDepartement ( Ecole ecole , String usernameCDepartement) {
		ArrayList <Examen> examens = new ArrayList<>(
				ecole.getPeriodeExamen().getExamens().stream()
										.filter( exam -> exam.getAnneeFiliere().getFiliere().getDepartement().getCoordonnateur().getUsername().equals( usernameCDepartement ) )
										.collect( Collectors.toList() ) 
		) ;
		
		return examens ;
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	

/*************** envoyer messages aux responsable de l administration de l app **********************************************************
  * 
   * 	 
    */
	public static void sendEmailPasswordAdmin ( Connection connexion , boolean pourEnseignant ) {
		
		try {
			Statement declaration = connexion.createStatement() ;
			ResultSet adminsCDepartement = declaration.executeQuery(  "SELECT * FROM `coordonnateurDepartement` ; "  ) ;
			declaration = connexion.createStatement() ;
			ResultSet adminsCFiliere = declaration.executeQuery(  "SELECT * FROM `coordonnateurFiliere` ; "  ) ;
			
			//Get properties object    
			Properties props = new Properties();    
			props.put("mail.smtp.host", "smtp.gmail.com");    
			props.put("mail.smtp.socketFactory.port", "465");    
			props.put("mail.smtp.socketFactory.class",    
					"javax.net.ssl.SSLSocketFactory");    
			props.put("mail.smtp.auth", "true");    
			props.put("mail.smtp.port", "465");    
			
			Properties properties = new Properties() ;
			try {
				properties.load( new FileInputStream("Config.properties") );
			} catch (FileNotFoundException e1) {
				return ;
			} catch (IOException e1) {
			}
			
			String email_username = properties.getProperty( "email.username" ) ;
			String email_password = properties.getProperty( "email.password" ) ;
			
			//get Session   
			Session session = Session.getDefaultInstance(props,    
					new javax.mail.Authenticator() {    
				protected PasswordAuthentication getPasswordAuthentication() {    
					return new PasswordAuthentication( email_username , email_password );  
				}    
			});  
			
			if ( pourEnseignant ) {
				declaration = connexion.createStatement() ;
				ResultSet adminsEnseignant = declaration.executeQuery(  "SELECT * FROM `enseignant` ; "  ) ;
				
				while ( adminsEnseignant.next() ) {
					
					
					
					//compose message    
			        try {    
			         MimeMessage message = new MimeMessage(session);    
			         message.addRecipient(Message.RecipientType.TO,new InternetAddress( adminsEnseignant.getString( "email") ));    
			         message.setSubject("Distribution des mots de passe des coordonnateurs des filieres");    
			         message.setText("Bonjour " + adminsEnseignant.getString( "nom" ) + " " + adminsEnseignant.getString( "prenom" ) + " voici votre mot de passe de la plateforme d examen  :  " + adminsEnseignant.getString( "mot_de_passe" ));    
			         //send message  
			         Transport.send(message);    
			         System.out.println(" messages sent successfully");    
			         } catch (MessagingException e) {System.out.println( " message not sent " );}
				      
				}
			
				return ;
			}
			
			while ( adminsCDepartement.next() ) {
				
			

		        //compose message    
		        try {    
		         MimeMessage message = new MimeMessage(session);    
		         message.addRecipient(Message.RecipientType.TO,new InternetAddress( adminsCDepartement.getString("email") ));    
		         message.setSubject("Distribution des mots de passe des coordonnateurs des departements");    
		         message.setText("Bonjour " + adminsCDepartement.getString( "nom" ) + " " + adminsCDepartement.getString( "prenom" ) + " voici votre mot de passe de la plateforme d examen  :  " + adminsCDepartement.getString( "mot_de_passe" ));    
		         //send message  
		         Transport.send(message);    
		         System.out.println(" messages sent successfully");    
		         } catch (MessagingException e) {System.out.println( " message not sent " );}
			      
			}
			
			while ( adminsCFiliere.next() ) {
				
				//compose message    
		        try {    
			         MimeMessage message = new MimeMessage(session);    
			         message.addRecipient(Message.RecipientType.TO,new InternetAddress( adminsCFiliere.getString( "email") ));    
			         message.setSubject("Distribution des mots de passe des coordonnateurs des filieres");    
			         message.setText("Bonjour " + adminsCFiliere.getString( "nom" ) + " " + adminsCFiliere.getString( "prenom" ) + " voici votre mot de passe de la plateforme d examen  :  " + adminsCFiliere.getString( "mot_de_passe" ));    
			         //send message  
			         Transport.send(message);    
			         System.out.println(" messages sent successfully");    
		         } catch (MessagingException e) {System.out.println( " message not sent " );}
			      
			}
		sent=true;	
		} catch ( SQLException e ) {
		sent=true;
			return ;
			
		}
		
	}
	
/*----------------------------------------------------------------------------------------------------------------------------------------------------*/
	/********* recuperer les informations d'un etudiant a partir de base de donnees
	 * @throws SQLException **************/
	
	
	
	public static ResultSet  recupererInformationsEtudiant(Connection connection,String massar) throws SQLException {
		
		 PreparedStatement  preparedStatement=connection.prepareStatement("SELECT * FROM etudiant WHERE CNE=?");
	                preparedStatement.setString(1, massar);
        
        return preparedStatement.executeQuery();
        
   
	}
	public static ResultSet  recupererInformationsAdmin(Connection connexion,String username) throws SQLException {
	Statement declaration;
		try {
			declaration = connexion.createStatement();
			ResultSet utilisateur_cdepartement    = declaration.executeQuery( "SELECT * FROM `coordonnateurdepartement` WHERE `username` = '" + username + "' ; " ) ;
			if (  !utilisateur_cdepartement.next()  ) {
				/*------------------------------------*/
				declaration = connexion.createStatement() ;
				ResultSet utilisateur_cfiliere    = declaration.executeQuery( "SELECT * FROM `coordonnateurfiliere` WHERE `username` = '" + username + "' ; " ) ;
				if (  !utilisateur_cfiliere.next()  ) {
					/*------------------------------------*/
					declaration = connexion.createStatement() ;
					ResultSet utilisateur_enseignant  = declaration.executeQuery( "SELECT * FROM `enseignant` WHERE `username` = '" + username + "'; " ) ;
					if (  !utilisateur_enseignant.next()  ) {
						
						return null;
					} else {
						//System.out.println( " vous etes verifie, enseignant " );
						return utilisateur_enseignant ;
					}	
				} else {
					//System.out.println( " vous etes verifie, chef de filiere " );
					return utilisateur_cfiliere  ;
				}
			}
			//System.out.println( " vous etes verifie, chef de departement " );
			return  utilisateur_cdepartement;
		} catch (SQLException e) {
			return null ;
		}
       
  
	}
	
	
	
	public static boolean verifyAccountForAddperiodExamen(Connection connexion, String username,String password) {
		
		  Statement  declaration;
		
		try {
			declaration = connexion.createStatement() ;
			ResultSet admin  =  declaration.executeQuery(  "SELECT * FROM `coordonnateurdepartement` WHERE `username` = '" + username + "' AND `mot_de_passe` = '" + password + "' ;"   )  ;
			if (  !admin.next()  ) {
				throw new SQLException() ;
			}
		} catch (SQLException e) {
			try {
				declaration = connexion.createStatement() ;
				ResultSet admin  =  declaration.executeQuery(  "SELECT * FROM `coordonnateurfiliere` WHERE `username` = '" + username + "' AND `mot_de_passe` = '" + password + "' ;"   )  ;
				if (  !admin.next()  ){
					throw new SQLException() ; 
				}
			} catch (SQLException e1) {
				System.out.println(  "vous n'etes pas coordonnateur departement ! "  ) ;
				System.out.println(  "vous n'etes pas coordonnateur de filiere ! "  ) ;
				System.out.println(  "vous douvez etre soit chef de departement soit chef de filiere pour que vous pouvez effectuer cette tache... !"  );
				return false ;
			}
		}
		return true;
		
		
		
	}
	
	
	/***************Inscription de l'etudiant*****************/
	 
	public static boolean inscriptionEtudiant( Connection connexion , String cne , String password ) {
		
		Statement declaration;
		try {
		
			declaration = connexion.createStatement();
			ResultSet utilisateur_etudiant  = declaration.executeQuery( "SELECT * FROM `etudiant` WHERE `CNE` = '" + cne + "' ; " ) ;
			if (  !utilisateur_etudiant.next()  ) {									

				return false ;
			}else {
				declaration = connexion.createStatement() ;
				declaration.executeUpdate( "UPDATE `etudiant` SET `mot_de_passe` = '" + password + "' WHERE CNE = '" + cne + "'  ; "  ) ;
				
				return true ;
			}
			
		} catch (SQLException e) {
			return false ;
		}
	}
	
	/*****************************************/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*----------------------------------------------------------------------------------------------------------------------------------------------------*/	
/******* envoie d'email au controleurs, etudiants, chefs de departement, chefs de filiere ***********************************************
  * 
   * 
    */
	private static void sendEmailsStudents ( Examen exam ) {
			
		//Get properties object    
		Properties props = new Properties();    
		props.put("mail.smtp.host", "smtp.gmail.com");    
		props.put("mail.smtp.socketFactory.port", "465");    
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");    
		props.put("mail.smtp.auth", "true");    
		props.put("mail.smtp.port", "465");    
	
		Properties properties = new Properties() ;
		try {
			properties.load( new FileInputStream("Config.properties") );
		} catch (FileNotFoundException e1) {
			return ;
		} catch (IOException e1) {
		}
		
		String email_username = properties.getProperty( "email.username" ) ;
		String email_password = properties.getProperty( "email.password" ) ;
		
		//get Session   
		Session session = Session.getDefaultInstance(props,    
			new javax.mail.Authenticator() {    
			protected PasswordAuthentication getPasswordAuthentication() {    
				return new PasswordAuthentication( email_username , email_password );  
			}    
		});  
		
	    //compose message    
	    try {    
	    	Iterator<Etudiant> iteratorEtudiants = exam.getAnneeFiliere().getEtudiants().iterator() ;
	    	while ( iteratorEtudiants.hasNext() ) {
	    		Etudiant etudiant = iteratorEtudiants.next() ; 
	    		MimeMessage message = new MimeMessage(session);    
	    		message.addRecipient(Message.RecipientType.TO,new InternetAddress( etudiant.getEmail() ));    
	    		message.setSubject("Nouveau examen ");   
	    		
	    		String _salles =  String.join( ", " ,  exam.getSalles().stream().map( salle -> salle.getNom_Salle() )
																			   .collect( Collectors.toList() )           ); 
	    		System.out.println( _salles );
	    		if( _salles.trim() != "" ) {
	    			_salles = " au " + _salles ;
	    		}
	    		message.setText("Bonjour " + etudiant.getNom() + " " + etudiant.getPrenom() + " vous avez un examen de " + exam.getDuree() + " minutes du module " + exam.getModule().getModule() + " le " + exam.getDate() + " à partir de " + exam.getTempsDebut() + " jusqu'à " + exam.getTempsFin() + _salles );
	    	    //send message  
	    		Transport.send(message);    
	    		System.out.println(" messages sent successfully");    
	    	}
	    } catch (MessagingException e) {System.out.println( " message not sent " );}
	    //compose message
	    try {    
	    	Iterator<Enseignant> iteratorControleursExamen = exam.getEnseignantsControleurs().iterator() ;
	    	while ( iteratorControleursExamen.hasNext() ) {
	    		Enseignant controleur = iteratorControleursExamen.next() ; 
	    		MimeMessage message = new MimeMessage(session);    
	    		message.addRecipient(Message.RecipientType.TO,new InternetAddress( controleur.getEmail() ));    
	    		message.setSubject("surveillance d'examen ");   
	    		
	    		String _salles =  String.join( " " ,  exam.getSalles().stream().map( salle -> salle.getNom_Salle() )
	    																						.collect( Collectors.toList() )           ); 
	    		if( _salles.trim() != "" ) {
	    			_salles = " au " + _salles ;
	    		}
	    		
	    		message.setText("Bonjour " + controleur.getNom() + " " + controleur.getPrenom() + " on est a votre besoin de surveiller un examen de " + exam.getDuree() + " minutes du module " + exam.getModule().getModule() + " le " + exam.getDate() + " à partir de " + exam.getTempsDebut() + " jusqu'à " + exam.getTempsFin() + _salles );
	    	    //send message  
	    		Transport.send(message);
	    		//System.out.println(" messages sent successfully");    
	    	}
	    	
	    } catch (MessagingException e) {System.out.println( " message not sent " );}
		
	    try {
	    	MimeMessage message = new MimeMessage(session);    
    		message.addRecipient(Message.RecipientType.TO,new InternetAddress( exam.getAnneeFiliere().getFiliere().getCoordonnateur().getEmail() ));    
    		message.setSubject( "nouveau examen à la filière" );   
    		message.setText( "Bonjour " + exam.getAnneeFiliere().getFiliere().getCoordonnateur().getNom() + " " +
    									  exam.getAnneeFiliere().getFiliere().getCoordonnateur().getPrenom() +
    									  ", on vous informe sur l'ajout d'un examen le " + exam.getDate() + 
    								 	  " à " + exam.getTempsDebut() + " au module " + exam.getModule().getModule() ) ;
    		Transport.send(message); 
    		System.out.println(" messages sent successfully");   
    		
	    } catch ( MessagingException e ) { System.out.println( "message not sent" ); }
	    
	    try {
	    	
	    	MimeMessage message = new MimeMessage(session);    
    		message.addRecipient(Message.RecipientType.TO,new InternetAddress( exam.getAnneeFiliere().getFiliere().getDepartement().getCoordonnateur().getEmail() ));    
    		message.setSubject( "nouveau examen au département" );   
    		message.setText( "Bonjour " + exam.getAnneeFiliere().getFiliere().getDepartement().getCoordonnateur().getNom() + " " +
    									  exam.getAnneeFiliere().getFiliere().getDepartement().getCoordonnateur().getPrenom() +
    									  ", on vous informe sur l'ajout d'un examen le " + exam.getDate() + 
    								 	  " à " + exam.getTempsDebut() + " au module " + exam.getModule().getModule() ) ;
    		Transport.send(message); 
    		//System.out.println(" messages sent successfully");
	    	
	    } catch ( MessagingException e ) {System.out.println( "message not sent" );}
	    
		return ;
				
	}
	
	public static boolean verifierTempsExamen(Label messageLabel, PeriodeExamen periode , LocalTime debut , int duree , LocalDate date ) {
		 // temps autorisé ici on verifie si on satifait les contrainte du temps ou pas... ?
if(  date.isBefore( periode.getDateDebutExamen() )   ||   date.isAfter( periode.getDateFinExamen() )  ) {
			
			/*
			 * CETTE CONDITION NOUS ASSURE QUE NOUS SOMME DANS L'INTERVALE DE PERIODE D'EXAMEN !
			 */
			System.out.println( "la date que vous avez choisi et hors periode d'examen !" );
			messageLabel.setText("la date que vous avez choisi et hors periode d'examen !");
			return false ;
		}
if(  date.getDayOfWeek()   ==   DayOfWeek.SUNDAY   )	{
			
			/*
			 * CETTE CONDITION POUR ELIMINER LE DIMANCHE DE JOURS AUTORISES ! 
			 */
			System.out.println( "le dimanche n'est pas autorisé à y passer l'examen " );	
			messageLabel.setText("le dimanche n'est pas autorisé à y passer l'examen ");
			return false ;
		}
		LocalTime fin = debut.plusMinutes( duree ) ;
		
		if(    	debut.isBefore(LocalTime.of(8, 30)) || debut.isAfter(LocalTime.of(18, 30)) 
													||
				fin.isBefore(LocalTime.of(8, 30)) 	|| fin.isAfter(LocalTime.of(18, 30))
												    ||
			   ( fin.isAfter(LocalTime.of(12, 30))  && fin.isBefore(LocalTime.of(14, 30)) )
			   										||
			  ( debut.isAfter(LocalTime.of(12, 30)) && debut.isBefore(LocalTime.of(14, 30)) )      )
         {
			
			/*
			 * CETTE CONTDITION VERIFIE SI ON EST DANS L'INTERVALE DU TEMPS AUTORISE OU NON !
			 */			
			System.out.println( "veuillez verifier l'heure que vous avez choisi ! ( NB : 08:30 --> 12:30 | 14:30 --> 18:30 ) ");
			messageLabel.setText("veuillez verifier l'heure que vous avez choisi ! \n NB : 08:30 --> 12:30 | 14:30 --> 18:30  ");
			return false ;
		}else
			
			/*
			 * SI ON SE DISPOSE AUX TOUTES EXIGENCE !
			 */
			
			return true ;
		
	   }
  public static boolean existsIntersectionTime(Label label_message,Ecole ecole, Examen examen ) {
		
		Iterator < Examen > iterateurExamen  =  ecole.getPeriodeExamen().getExamens()
			
		.stream()
		
		/*
		 * FILTRER LA LISTE DES EXAMENS PAR LEURS AnneeFiliere !
		 */
		
		.filter(
			exam  ->  exam.getAnneeFiliere().equals(  examen.getAnneeFiliere()  ) 
		)								
		
		/*
		 * FILTRER LA LISTE DES EXAMEN DE MEME CANDIDATS (  MEME PROMO  ) PAR DATE !
		 */
		
		.filter(examAnneeFiliere   ->   examAnneeFiliere.getDate().equals( examen.getDate() ))
		
		/*
		 * CHERCHER LES PROMO FILTRES QU'ONT EXAMEN DANS LA MEME HEURE QU'ON VEUT ! CECI POUR EVITER D'AVOIR DEUX EXAMEN POUR LA MEME ANNEE SCOLAIRE DE MEME FILIERE AYANT MEME HEURE D'EXAMEN C-A-D MEME CONDIDAT AYANT DEUX EXAMENS AU MEME TEMPS (IMPOSSIBLE).
		 */
		
		.iterator() ;
		
		/*
		 * ON A CREE CET ITERATEUR POUR PARCOURIR LES EXAMENS DE PROMO SELON LE FILTRE AFIN DE VERIFIER SI ON A UNE COINCIDENCE EN TEMPS.
		 */
		
		while( iterateurExamen.hasNext() ) {
			
			Examen examenIterateur = iterateurExamen.next() ; 
			
		    if(   ( examenIterateur.getTempsDebut().equals(  examen.getTempsDebut()  )                                      )
				   																		 ||
				  (  (examenIterateur.getTempsDebut().isBefore( examen.getTempsDebut())  &&  examenIterateur.getTempsFin().isAfter( examen.getTempsDebut() ))
																				         ||
				     (examenIterateur.getTempsDebut().isBefore( examen.getTempsFin()  )  &&  examenIterateur.getTempsFin().isAfter(  examen.getTempsFin()  ))  )
																						 || 
				  (   examenIterateur.getTempsDebut().isAfter( examen.getTempsDebut() )  &&  examenIterateur.getTempsFin().isBefore(   examen.getTempsFin() )  )    ) {
				
		    	label_message.setText("veuillez choisir un autre temps parce que cette classe est indisponible à cette heure");
				return true ;			 }
		}
		
		/*
		 * RETURNER FALSE C-A-D QU'IL Y A PAS DE COINCIDENCE .
		 */
		
		return false ;
	}
   public static ArrayList<Examen> recupererExamensDeAdmin( Ecole ecole , String username ){
		if(  
				ecole.getDepartements().stream().map( departement -> departement.getCoordonnateur() )
												.filter( departement -> departement.getUsername().equalsIgnoreCase(username) )  
												.collect( Collectors.toList() ).size()  >  0
		) 
			return ServiceProvider.recupererExamenCoordonnateurDepartement( ecole , username ) ;
		
				
		for (   ArrayList<Filiere> filieres :
				ecole.getDepartements().stream().map( departement->departement.getFilieres() )								
												.collect( Collectors.toList() ) 
		) {
			if( filieres.stream().anyMatch( filiere->filiere.getCoordonnateur().getUsername().equals(username) ) ) 
				return ServiceProvider.recupererExamenCoordonnateurFiliere(ecole , username) ;
		}
		return ServiceProvider.recupererExamenEnseignant(ecole, username) ;
		
	}
   public static boolean sentAllMessage() {
	    return sent;
   }
	

	
	
}
