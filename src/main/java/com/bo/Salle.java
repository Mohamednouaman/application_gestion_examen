package com.bo;

public abstract class Salle {
	protected String nomSalle ; // identifiant de salle dans un bloc et meme pour amphi dans l'ecole.
	public abstract String getIdSalle() ;
	public abstract String getNom_Salle() ;
/**** une salle peut etre soit amphi soit une salle dans un bloc.****/
	
}

