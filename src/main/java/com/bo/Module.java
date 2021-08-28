package com.bo;

import java.util.ArrayList;

public class Module {
	private String module ;
	private Enseignant enseignantModule ;
	private int semestre ;
	private ArrayList <SalleBlocTP> salleTP = null ;
	
/**** construction d'un module *********************************************/	
	public Module(String module , Enseignant enseignantModule , int semestre) {

		this.module = module ;
		
        this.enseignantModule = enseignantModule  ;
				
		this.semestre = semestre ; 
		
		this.salleTP = new ArrayList <SalleBlocTP> ();
	}
	
	public void ajouterSalleTPModule( SalleBlocTP salleTP ) {
		this.salleTP.add( salleTP ) ;  
	}   //il y'a des modules qui exigent d'avoir leurs propre salle de tp 
	
	public void ajouterSalleTPModule( String bloc , String salle  ) {
		
	}
	
/**** getters **************************************************************/
	public int getSemestre() {
		return this.semestre ;
	}
	public Enseignant getEnseignantModule() {
		return this.enseignantModule  ;
	}
	public ArrayList <SalleBlocTP> getSalleTP(){
		return salleTP ;
	}
	public String getModule() {
		return this.module;
	}
}

