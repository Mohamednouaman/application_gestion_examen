package com.bo;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class AnneeFiliere {
	
	private int niveau ; 
	private String option ;
	private ArrayList<Module> modules ;
	private Filiere filiere ;
	private ArrayList<Etudiant> etudiants ;
	
/**** construction ********************************************/
	public AnneeFiliere( int niveau , String option , ArrayList<Module> modules ,  ArrayList< Etudiant > etudiants ) {	
		this.niveau    = niveau ;
		this.option    = option ;
		this.modules   = new ArrayList<>( modules ) ;
		this.etudiants = new ArrayList<>( etudiants ) ;
	}
	
/**** les getters :********************************************/
	public int getNiveau() {
		return this.niveau ;
	}
	public String getOption() {
		return this.option ;
	}
	public ArrayList<Module> getModules(){	
		return this.modules ;
	}
	public Filiere getFiliere() {
		return this.filiere ;
	}
	public ArrayList<Etudiant> getEtudiants(){ 
		return this.etudiants ;
	}
	public ArrayList <Enseignant> getEnseignants( ){
		return new ArrayList<>(   this.modules.stream()
											  .map(module -> module.getEnseignantModule())
											  .distinct()
											  .collect(Collectors.toList())								);	
	}
	public String getAnneeFiliere( ) {
		return this.getFiliere().getNomFiliere() + "-" +  this.getNiveau() + " " + this.getOption() ;
	}
	public void setFiliere( Filiere filiere ) {
		this.filiere = filiere ;
	}
}
