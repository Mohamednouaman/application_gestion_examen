package com.bo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class ExamenTP extends Examen {   
	
	public ExamenTP(  AnneeFiliere candidats , Module module , LocalDate date , LocalTime tempsDebut , int dureeMinute  ) {
		this.candidats = candidats ;
		this.module = module ;
		this.date = date ;
		this.tempsDebut = tempsDebut ;
		this.dureeMinute = dureeMinute ;
		this.tempsFin = this.tempsDebut.plusMinutes(dureeMinute) ;
		this.salles = new ArrayList <Salle> (  ) ;
		this.controleurs = new ArrayList <Enseignant> () ;
	}
		
	@Override
	public boolean reserverSalleExamen(  ArrayList<Salle> salles  ) {
		return this.salles.addAll( salles ) ;
	}

	@Override
	public LocalDate getDate() {
		return this.date ;
	}

	@Override
	public LocalTime getTempsDebut() {
		return this.tempsDebut ;
	}

	@Override
	public LocalTime getTempsFin() {
		return this.tempsFin ;
	}

	@Override
	public int getDuree() {
		return this.dureeMinute ;
	}

	@Override
	public Module getModule() {
		return this.module ;
	}

	@Override
	public AnneeFiliere getAnneeFiliere() {
		return this.candidats ;
	}

	@Override
	public ArrayList<Salle> getSalles() {
		return this.salles ;
	}
	
	@Override
	public ArrayList<Enseignant> getEnseignantsControleurs() {
		return this.controleurs;
	}
	
	@Override
	public boolean pourTP() {
		return true ; 
	}
	
	@Override
	public boolean ajouterControleurs(ArrayList<Enseignant> controleur) {
		return this.controleurs.addAll( controleur ) ;
	}
	
}


