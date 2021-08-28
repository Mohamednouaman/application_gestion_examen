package com.dao_services;

import java.time.LocalDate;
import java.time.LocalTime;

public class ServiceInfoExamen {
	
	private String classe;
	private String module;
	private LocalDate date;
	private LocalTime  heure;
	private String duree;
	private String  type;
	
	
	
	
	public ServiceInfoExamen(String classe, String module, LocalDate date, LocalTime heure, String duree, String type) {

		this.classe = classe;
		this.module = module;
		this.date = date;
		this.heure = heure;
		this.duree = duree;
		this.type = type;
	}

	
	public String getClasse() {
		return classe;
	}
	public LocalDate getDate() {
		return date;
	}
	public LocalTime getHeure() {
		return heure;
	}
	public String getType() {
		return type;
	}
	public String getDuree() {
		return duree;
	}
	public String getModule() {
		return module;
	}

}
