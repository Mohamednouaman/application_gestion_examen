package com.bo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PeriodeExamen {
	private LocalDate dateDebutExamen ; // debut de semaine d'examen 
	private LocalDate dateFinExamen ;	// la date ou la periode d'examen doit se fini
	private ArrayList <Examen> examens ;	// liste des examens.
	private Ecole ecole ;
	
	public PeriodeExamen( LocalDate dateDebutExamen , LocalDate dateFinExamen , Ecole ecole ) {
		
			this.dateDebutExamen = dateDebutExamen ; 
			this.dateFinExamen = dateFinExamen ; 
			this.examens = new ArrayList <> ( ) ; 
			this.ecole = ecole ;
	}										

/***** ajouter un examen au periode ************************************************************************/
	public boolean ajouterExamen( Examen examen , boolean pourTP ,boolean sansAjouter) {
		
		/*
		 * ON DOIT VERIFIER LA DUREE D'EXAMEN QU'ELLE NE DEPASSE PAS 4 HEURES ET QU'ELLE EST POSITIVE.
		 */
		
		if( examen.getDuree() <= 0 || examen.getDuree() > 4*60 ) {
			System.out.println(  " l examen ne peut pas depasser 4h !"  );
			return false ;
		}
		
		/*
		 * ON TESTE D'ABORD SI CE TEMPS EST AUTORISE... PAR EXEMPLE ON PEUT PAS PASSER UN EXAMEN LE DIMANCHE
		 */
		
		if( ! this.tempsAutorise(examen.getTempsDebut() , examen.getTempsFin() , examen.getDate()) ) {
			System.out.println(  " ce temps n est pas autorise !"  );
			return false ;
		}
		
		/*
		 * SI CE TEMPS EST BIEN AUTORISE A PASSER L'EXAMEN ALORS ON VA TESTER SI IL N'Y A PAS UN EXAMEN A CE TEMPS POUR LES MEME CONDIDATS.
		 */

		if( this.existsIntersectionTime(examen) ) {
			System.out.println(  " veuillez choisir une autre periode !"  );
			return false ;
		}
		
		/*
		 * APRES D'AVOIR LES SALLES SUSIPTIBLES A PASSER L'EXAMEN A CE TEMPS, ON AJOUTE CET EXAMEN AU LISTE DES EXAMENS. 
		 */
		if ( this.examens.add(examen) ) {			
			System.out.println(  " examen bien construit veuillez choisir les salles qui vont heberger set examen..."  );
			this.examens.remove( examen ) ; 
			return true ;
		}
		return false ;
	}
	
/**** methode static pour verifier si on respecte la contrante de temps autoris�s � passer les examens *****/
	private boolean tempsAutorise ( LocalTime debut , LocalTime fin , LocalDate date ) {
			 // temps autoris� ici on verifie si on satifait les contrainte du temps ou pas... ?
		if(  date.isBefore( this.dateDebutExamen )   ||   date.isAfter( this.dateFinExamen )  )
			
			/*
			 * CETTE CONDITION NOUS ASSURE QUE NOUS SOMME DANS L'INTERVALE DE PERIODE D'EXAMEN !
			 */

			return false ;
			
		if(  date.getDayOfWeek()   ==   DayOfWeek.SUNDAY   )	
			
			/*
			 * CETTE CONDITION POUR ELIMINER LE DIMANCHE DE JOURS AUTORISES ! 
			 */
			
			return false ;
		
		if(    	debut.isBefore(LocalTime.of(8, 30)) || debut.isAfter(LocalTime.of(18, 30)) 
													||
				fin.isBefore(LocalTime.of(8, 30)) 	|| fin.isAfter(LocalTime.of(18, 30))
												    ||
			   ( fin.isAfter(LocalTime.of(12, 30))  && fin.isBefore(LocalTime.of(14, 30)) )
			   										||
			  ( debut.isAfter(LocalTime.of(12, 30)) && debut.isBefore(LocalTime.of(14, 30)) )      )
			
			/*
			 * CETTE CONTDITION VERIFIE SI ON EST DANS L'INTERVALE DU TEMPS AUTORISE OU NON !
			 */			
			
			return false ;
		
		else
			
			/*
			 * SI ON SE DISPOSE AUX TOUTES EXIGENCE !
			 */
			
			return true ;
		
	}
	
/**** cette methode verifie si on on une intersection d'intervale du temps entre deux periodes d'examen ****/
	private boolean existsIntersectionTime( Examen examen ) {
		
		Iterator < Examen > iterateurExamen  =  this.examens
			
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
		
		.filter(
			examAnneeFiliere   ->   examAnneeFiliere.getDate().equals( examen.getDate() )
		)
		
		/*
		 * CHERCHER LES PROMO FILTRES QU'ONT EXAMEN DANS LA MEME HEURE QU'ON VEUT ! CECI POUR EVITER D'AVOIR DEUX EXAMEN POUR LA MEME ANNEE SCOLAIRE DE MEME FILIERE AYANT MEME HEURE D'EXAMEN C-A-D MEME CONDIDAT AYANT DEUX EXAMENS AU MEME TEMPS (IMPOSSIBLE).
		 */
		
		.iterator() ;
		
		/*
		 * ON A CREE CET ITERATEUR POUR PARCOURIR LES EXAMENS DE PROMO SELON LE FILTRE AFIN DE VERIFIER SI ON A UNE COINCIDENCE EN TEMPS.
		 */
		
		while( iterateurExamen.hasNext() ) {
			
			Examen examenIterateur = iterateurExamen.next() ; 
			
		    if(   (                                    examenIterateur.getTempsDebut().equals(  examen.getTempsDebut()  )                                      )
				   																		 ||
				  (  (examenIterateur.getTempsDebut().isBefore( examen.getTempsDebut())  &&  examenIterateur.getTempsFin().isAfter( examen.getTempsDebut() ))
																				         ||
				     (examenIterateur.getTempsDebut().isBefore( examen.getTempsFin()  )  &&  examenIterateur.getTempsFin().isAfter(  examen.getTempsFin()  ))  )
																						 || 
				  (   examenIterateur.getTempsDebut().isAfter( examen.getTempsDebut() )  &&  examenIterateur.getTempsFin().isBefore(   examen.getTempsFin() )  )    )
				
				return true ;			 
		}
		
		/*
		 * RETURNER FALSE C-A-D QU'IL Y A PAS DE COINCIDENCE .
		 */
		
		return false ;
	}

/**** methode verifie si cet examen de tp est planifie au meme temps d'un autre examen *********************/	
	public List<Salle> sallesExamenTP ( Examen examen ) {
		
		/*
		 * ON INITIALISE UNE LISTE PAR LES SALLES DE TP DE CE MODULE ET ON LEURS REDUIRE PAR LES SALLE QUI SONT PAS DISPONIBLE A CE MOMENT
		 */
		
		List<Salle> sallesDispoExamenTP = new ArrayList <>() ; 
		sallesDispoExamenTP.addAll( examen.getModule().getSalleTP() ) ;
		
		this.examens
		
	   /*
		* OBTENONS D'ABORD STREAM POUR PROFITER DE QEULQUE METHODE QUI NOUS AIDE.
		*/
		
		.stream()
		
	   /*
		* ON UTILISE LA METHODE FILTER POUR FILTRER NOTRE FLUX, POUR QU'ON N'OBTIENT QUE LES EXAMS AYANT LA MEME DATE QUE L'EXAMEN VOULU A AJOUTER.
		*/
		
		.filter( exam  ->  exam.getDate().equals(examen.getDate()) )
		
	   /*
		* DEUXIEM FILTER POUR OBTENIR SEULEMENT LES EXAMENS QUI SE COINCIDENCE DU TEMPS AVEC NOTRE EXAMEN VOULU AJOUTER.
		*/
		
		.filter( exam  ->  { 
							if(    (                                         exam.getTempsDebut().equals(  examen.getTempsDebut()  )                          )
																								   ||
								   (     (exam.getTempsFin().isAfter(  examen.getTempsDebut() )    &&  exam.getTempsFin().isBefore( examen.getTempsFin()))	
					        			    												       ||
		                                 (exam.getTempsDebut().isAfter(  examen.getTempsDebut() )  &&  exam.getTempsDebut().isBefore( examen.getTempsFin()))  )
																								   ||
			    				   (	  exam.getTempsDebut().isBefore(  examen.getTempsDebut())  &&  exam.getTempsFin().isAfter(  examen.getTempsFin() )    )    ) 
											
	    	   					 return true ;
								   
	  					    return false ;
						    }
		)
		
		/*
		 * MAINTENANT ON VA RETOURNER LES SALLES DE CES EXAMENS FILTRES.
		 */
		
		.map(  exam  ->  exam.getSalles() ) 
		
		/*
		 * MAINTENANT ON VA CHERCHER LES SALLES QU'ON VEUT RESERVER ET QUI SONT DEJA RESERVEES.
		 */
		
		.forEach( sallesExamenTP  ->  {
											
										/*
										 * SI ON TROUVE UNE SALLE DEJA RESERVEE ALORS ON VA LA EFFACER. POUR QU'ON NE CONSERVE QUE LES SALLES LIBRE.
										 */			
			
										PeriodeExamen.supprimerIntersectionSalle( sallesExamenTP , sallesDispoExamenTP );
				  					  }
		) ;
		
		return sallesDispoExamenTP ;	
	}
	
/**** verifier si cet examen de theorique est a des salles deja reservees **********************************/
	public ArrayList < Salle > sallesExamenTheorique( Examen examen ) {
		
		/*
		 * ON INITIALISE UNE LISTE PAR LES SALLES DE TP DE CE MODULE ET ON LEURS REDUIRE PAR LES SALLE QUI SONT PAS DISPONIBLE A CE MOMENT
		 */
		
		ArrayList <Salle> sallesDispoExamenTheorique = new ArrayList <>() ; 
		sallesDispoExamenTheorique.addAll( this.ecole.getAmphis() ) ;
		
		this.ecole.getBlocs().stream()
				  .map( bloc -> bloc.getSallesTheorique() )
				  .collect( Collectors.toList() )
				  .forEach( salles -> sallesDispoExamenTheorique.addAll( salles ) );
		
		this.examens
		
	   /*
		* OBTENONS D'ABORD STREAM POUR PROFITER DE QEULQUE METHODE QUI NOUS AIDE.
		*/
		
		.stream()
		
	   /*
		* ON UTILISE LA METHODE FILTER POUR FILTRER NOTRE FLUX, POUR QU'ON N'OBTIENT QUE LES EXAMS AYANT LA MEME DATE QUE L'EXAMEN VOULU A AJOUTER.
		*/
		
		.filter( exam  ->  exam.getDate().equals(examen.getDate()) )
		
	   /*
		* DEUXIEM FILTER POUR OBTENIR SEULEMENT LES EXAMENS QUI SE COINCIDENT DU TEMPS AVEC NOTRE EXAMEN VOULU AJOUTER.
		*/
		
		.filter( exam  ->  { 
							if(    (                                  exam.getTempsDebut().equals(  examen.getTempsDebut()  )                                 )
																								   ||
								   (     (exam.getTempsFin().isAfter(  examen.getTempsDebut() )    &&  exam.getTempsFin().isBefore( examen.getTempsFin()))	
					        			    												       ||
				                         (exam.getTempsDebut().isAfter(  examen.getTempsDebut() )  &&  exam.getTempsDebut().isBefore( examen.getTempsFin()))  )
																								   ||
								   (	  exam.getTempsDebut().isBefore(  examen.getTempsDebut())  &&  exam.getTempsFin().isAfter(  examen.getTempsFin() )    )    ) 
											
								   return true ;
								   
							return false ;
				}
		)
		
		/*
		 * MAINTENANT ON VA RETOURNER LES SALLES DE CES EXAMENS FILTRES.
		 */
		
		.map(  exam  ->  exam.getSalles() ) 
		
		/*
		 * MAINTENANT ON VA CHERCHER LES SALLES QU'ON VEUT RESERVER ET QU'ON DEJA RESERVEES.
		 */
		
		.forEach( sallesExamenTheorique  ->  {
			
												 /*
												  * SI ON TROUVE UNE SALLE DEJA RESERVEE ALORS ON VA LA EFFACER. POUR QU'ON NE CONSERVE QUE LES SALLES LIBRE.
												  */	
			
												PeriodeExamen.supprimerIntersectionSalle(sallesExamenTheorique, sallesDispoExamenTheorique);
				  					         }
		) ; 
		
		return sallesDispoExamenTheorique ;
	}
	
/**** getters **********************************************************************************************/
	public ArrayList<Examen> getExamens() {
		
		/*
		 * ON PREFERE DE RETOURNER UN ArrayList... POUR UNIFIER LES TYPES DE RETOURNE ... 
		 */
		
		ArrayList<Examen> listeExamens = new ArrayList<>() ;
		listeExamens.addAll(this.examens) ;
		return listeExamens ;
	}
	
	public LocalDate getDateDebutExamen() {
		return this.dateDebutExamen ;
	}
	
	public LocalDate getDateFinExamen() {
		return this.dateFinExamen ;
	}	
	
	public Ecole getInfoEcole() {
		return this.ecole ;
	}

/**** supprimer les intersection entre salles **************************************************************/
	private static void supprimerIntersectionSalle( List<Salle> salles1 , List<Salle> salles2 ) {
		
		Iterator <Salle> iterateurSalle = salles2.iterator() ;
		
		while( iterateurSalle.hasNext() ) {
			if( salles1.contains(iterateurSalle.next()) ) {
				iterateurSalle.remove(); 	
			}
			
		}
	}
	
/****** on cherche les professeurs disponibles a controler l examen ****************************************/
	public ArrayList<Enseignant> enseignantsDisponiblsToControl( Examen examen ) {
		
		/*
		 * on ensemble les ensignantde l'ecole dans un objet...
		 */
		
		ArrayList<Enseignant> enseignantsControleurs = this.ecole.getEnseignantEcole() ;
		
		/*
		 * on stream la list des examens 
		 */
		
		this.examens.stream()

		/*
		 * ce qu'on veut maintenant c'est de filtrer les examens qui se passeront dans la meme date ...
		 */
		
		.filter( exam -> exam.getDate().equals( examen.getDate() ) )
		
		/*
		 * on va filtrer le resultat en des examen qu'on s'intersectent en temps... pour qu'on elemine les profs qui controlent cet examen...
		 */
		
		.filter(  exam  ->  { 
				  
			  	  if(/*******/     			(/*ont le meme debut*/   exam.getTempsDebut().equals(examen.getTempsDebut())   /*ont le meme debut*/ )   
																 					   					 ||
						 ( /*s'intersectent*/  (exam.getTempsFin()  .isAfter(examen.getTempsDebut())     &&  exam.getTempsFin()  .isBefore( examen.getTempsFin())) 	
																			           				     ||
											   (exam.getTempsDebut().isAfter(examen.getTempsDebut())     &&  exam.getTempsDebut().isBefore( examen.getTempsFin()))  /*s'intersectent*/ )
						 																				 ||
						 (/*coverture de temps*/exam.getTempsDebut().isBefore(examen.getTempsDebut())    &&  exam.getTempsFin().isAfter(  examen.getTempsFin() )      /*coverture*/    )/*******/) 
				
								return true ;
				
			  	  return false ;
				
				  }
		)
		
		/*
		 * on mappe ce resultat pour obtenir une liste des controleurs de ces examen qui s'intersectent en terme de temps de passassion d'examens...
		 */
		
		.map( 	 exam    ->    exam.getEnseignantsControleurs()	  ) 
		
		/*
		 * on parcourt cette liste afin de supprimer (filtrer) es enseignant qui ne peuvent pas surveiller notre examen...
		 */
		
		.forEach(      enseignants    ->   enseignants.forEach(enseignant -> enseignantsControleurs.remove( enseignant ))      ) ;
		
		/*
		 * apres qu'on a obtenu le resultat qu'on cherche on le retourne alors... 
		 */
		
		return enseignantsControleurs ;
	}
	public boolean ajouterExamen( Examen examen , boolean pourTP ) {
		if ( this.examens.add(examen) ) {	
			System.out.println(  " examen bien construit veuillez choisir les salles qui vont heberger set examen..."  );
			return true ;
		}
		return false ;
	}
}