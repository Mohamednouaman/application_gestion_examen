package com.bo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Ecole {
	private String nomEcole ;
	private ArrayList<Departement> departements ;
	private TreeSet<Bloc> blocs ;
	private TreeSet<Amphi> amphis ;
	private PeriodeExamen periodeExamen ;
	
/**** construction ecole ***************************************************************/
	public Ecole( String nomEcole ) {
		this.nomEcole = nomEcole ;											
		this.departements = new ArrayList<>() ;
		this.blocs = new TreeSet<>( 										
			new Comparator<>() {
				@Override
				public int compare(Bloc o1, Bloc o2) {
					return o1.getNomBloc().compareTo(o2.getNomBloc()) ;
				}
			}
		) ;
		this.amphis = new TreeSet<>(										
			new Comparator<>() {
				@Override
				public int compare(Amphi o1, Amphi o2) {
					return o1.getNomAmphi().compareTo(o2.getNomAmphi()) ;
				}
			}
		) ;
		
		//pour qu'ils soient ordonn�s comme : amphi1 amphi2 ... 
	}

/**** ajout du amphi dans l'ecole ******************************************************/
	public boolean ajouterAmphi( Amphi amphi ) {
		try {
			return this.amphis.add(amphi) ;
		}catch( NullPointerException ignore ) {
			return false ;
		}
	}
	
/**** ajout du bloc dans l'ecole *******************************************************/
	public boolean ajouterBloc( Bloc bloc ) {
		try {			
			return this.blocs.add(bloc) ;
		}catch( NullPointerException ignore ) {
			return false ;
		}
	}

/**** ajout du departement dans l ecole ************************************************/
	public boolean ajouterDepartement( Departement departement ) {
		if( departement == null )
			return false ;
		if( !existsDepartement(departement.getDepartement()) ) 
			return this.departements.add(departement) ;
		return false ;		
	}
		
/**** il faux verifier l existance du departement par la methode exists ****************/
	private boolean existsDepartement( String nomDepartement ) {
		try {
			nomDepartement = nomDepartement.trim().toLowerCase() ;
		}catch(NullPointerException ignore) {
		}
		Iterator <Departement> iterateur = this.departements.iterator() ;
		while( iterateur.hasNext() ) {
			if( iterateur.next().getDepartement() == nomDepartement )
				return true ;
		}
		return false ;
	}
	//private car elle ne sert que nous determiner si l'ecole a deja un departement par ce nom
	
/**** l'ajout d une periode d'examen ***************************************************/
	public boolean ajouterPeriodeExamen( PeriodeExamen periodeExamen ) {
		
		if (  /*debut avant fin*/  periodeExamen.getDateDebutExamen().isBefore(periodeExamen.getDateFinExamen())  /*debut apres aujord hui*/
																		      &&
			   ( periodeExamen.getDateDebutExamen().isAfter(LocalDate.now())  ||   periodeExamen.getDateDebutExamen().isEqual(LocalDate.now()))     ) {
					
					this.periodeExamen = periodeExamen  ;	
					
		/* ====> */	return true ;		
		}
		
		return false ;
	}
	
	
/**** getters **************************************************************************/	
	public String getNomEcole() {
		return this.nomEcole ;
	}
	public ArrayList<Departement> getDepartements(){
		return this.departements ;
	}
	public ArrayList<Bloc> getBlocs(){
		return new ArrayList<> (this.blocs) ;
	}
	public ArrayList<Amphi> getAmphis(){
		return new ArrayList<> (this.amphis) ;
	}
	public PeriodeExamen getPeriodeExamen(){
		return this.periodeExamen ;
	}
	public ArrayList<Enseignant> getEnseignantEcole ( ) {
		ArrayList<Enseignant> enseignants = new ArrayList <> ( ) ;
		this.getDepartements().forEach(  departement -> enseignants.addAll(  departement.getEnseignants()  )  ) ;
		ArrayList<Enseignant> enseignantDistingus = new ArrayList <> (
				enseignants.stream().distinct().collect(  Collectors.toList()  )
		) ;
		return enseignantDistingus ;
	}
	public ArrayList<Salle> getSalles(){
		ArrayList<Salle> arraySalle = new ArrayList<>() ;
		arraySalle.addAll( this.getAmphis() ) ;
		
		this.getBlocs().stream().map( bloc->bloc.getSalles() ).collect( Collectors.toList() ).forEach( salles_ -> arraySalle.addAll( salles_ )  ) ;
		
		return arraySalle ;
	}
}
