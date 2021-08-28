package com.bo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public abstract class Examen {
	protected AnneeFiliere candidats ;
	protected Module module ;
	protected LocalDate date ; //= dateTempsDebut.plusMinutes(dureeMinute) 
	protected LocalTime tempsDebut ;	
	protected int dureeMinute ; //    >0    &&   <4h
	protected LocalTime tempsFin; //= dateTempsDebut.plusMinutes(dureeMinute) 
	protected ArrayList< Salle > salles ;
	protected ArrayList< Enseignant > controleurs ;
	 
	abstract public boolean ajouterControleurs( ArrayList<Enseignant> controleur ) ;
	abstract public boolean reserverSalleExamen( ArrayList<Salle> salle ) ;
	abstract public LocalDate getDate() ;
	abstract public LocalTime getTempsDebut() ;
	abstract public LocalTime getTempsFin() ;
	abstract public int getDuree() ;
	abstract public Module getModule() ;
	abstract public AnneeFiliere getAnneeFiliere() ;
	abstract public ArrayList<Salle> getSalles() ;
	abstract public ArrayList<Enseignant> getEnseignantsControleurs() ; 
	abstract public boolean pourTP() ;
	/* 
	 * 	cette classe abstraite va nous deriver deux classe :
	 * 
	 *	car un examen soit Examen de theorique ou un Examen de TP 
	 */	
}