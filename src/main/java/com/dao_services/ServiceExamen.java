package com.dao_services;

import java.time.LocalDate;
import java.time.LocalTime;

public class ServiceExamen {
	private String module;
	private LocalDate date;
	private LocalTime heure;
	private String duree;
	private String salle;
	public ServiceExamen(String module, LocalDate date, LocalTime heure, String duree, String salle) {
		this.module = module;
		this.date = date;
		this.heure = heure;
		this.duree = duree;
		this.salle = salle;
	}
	public LocalDate getDate() {
		return date;
	}
	public LocalTime getHeure() {
		return heure;
	}
	public String getModule() {
		return module;
	}
	public String getSalle() {
		return salle;
	}
	public String getDurre() {
		return duree;
	}
	

	
	
	
	
	
	
	

}
