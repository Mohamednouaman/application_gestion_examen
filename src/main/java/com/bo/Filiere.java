package com.bo;

import java.util.stream.Collectors;
import java.util.ArrayList;

public class Filiere {
	private String filiere ;
	private ArrayList <AnneeFiliere> annees ;  
	private CoordonnateurFiliere coordonnateur ;
	private Departement departement ;
/**** construction *********************************************************************************************/
	public Filiere(String filiere , CoordonnateurFiliere coordonnateur , ArrayList<AnneeFiliere> annees ) {
		
		this.annees 	   = new ArrayList <AnneeFiliere>( annees ) ;
		this.annees.forEach( annee -> annee.setFiliere(  this  ) );
		this.filiere       = filiere ; 	
		this.coordonnateur = coordonnateur ;
	}

/**** getters **************************************************************************************************/
	public ArrayList <Enseignant> getEnseignants(){
		   ArrayList< ArrayList<Enseignant> > enseignants_ = new ArrayList<>( this.annees.stream()
		    																			 .map(annee -> annee.getEnseignants())
																						 .collect( Collectors.toList() )       );
		   enseignants_.add( 0 , new ArrayList<Enseignant>() ) ;
		   enseignants_.forEach(  enseignants  ->  enseignants_.get(0).addAll( enseignants )  ) ;
		   return ( ArrayList<Enseignant> )enseignants_.get(0).stream().distinct().collect( Collectors.toList() ) ; 
	}
	public String getNomFiliere() {
		return this.filiere ;
	}
	public CoordonnateurFiliere getCoordonnateur() {
		return this.coordonnateur ;
	}
	public ArrayList<AnneeFiliere> getAnnees() {
		return this.annees ;
	}	
	public void setDepartement( Departement departement ) {
		this.departement = departement ;
	}
	public Departement getDepartement() {
		return this.departement ;
	}
}


