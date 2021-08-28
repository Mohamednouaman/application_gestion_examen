package com.bo;

public class Amphi extends Salle{

/**** premier type de salle est l'amphi : ******/
	public Amphi(String nomAmphi) {
		this.nomSalle = nomAmphi ;
	}
	
/**** getter : *********************************/
	public String getNomAmphi() {
		return this.nomSalle ;
	}
	@Override
	public String getIdSalle() {
		return this.getNomAmphi() ;
	}

	@Override
	public String getNom_Salle() {
		return getNomAmphi();
	}
}

