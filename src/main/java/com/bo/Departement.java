package com.bo;

import java.util.ArrayList;
import java.util.HashSet;

public class Departement {
	private String departement ;
	private CoordonnateurDepartement coordonnateur ;
	private ArrayList<Filiere> filieres ;
	private HashSet<Enseignant> enseignants ;
	
/**** Construction de departement ***********************************************************/
	public Departement( String departement , CoordonnateurDepartement coordonnateur , ArrayList<Filiere> filieres  ) {
		this.departement = departement ;	
		this.coordonnateur = coordonnateur ;
		this.filieres = new ArrayList<>( filieres ) ;
		this.filieres.forEach( filiere -> filiere.setDepartement(this) );
		this.enseignants = new HashSet<>() ;
		filieres.forEach(      filiere   ->   this.enseignants.addAll( filiere.getEnseignants() )      ) ;
	}
	
/**** ajouter tous les professeur du departement ********************************************/
	public void enseignantsDeDepartement() {		
		filieres.forEach( filiere   ->   this.enseignants.addAll( filiere.getEnseignants() )) ;
	}

/**** getters *******************************************************************************/
	public String getDepartement() {
		return this.departement ;   
	}
	public CoordonnateurDepartement getCoordonnateur() {
		return this.coordonnateur ;
	}
	public ArrayList<Filiere> getFilieres(){
		return this.filieres ;
	}
	public ArrayList<Enseignant> getEnseignants(){
		return new ArrayList<Enseignant>( this.enseignants )  ;
	}
}
