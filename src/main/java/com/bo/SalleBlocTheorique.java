package com.bo;

public class SalleBlocTheorique extends SalleBloc {

/******* deuxiem type de salle bloc : salle de bloc pour theorique *****/
	SalleBlocTheorique(String nomSalle , Bloc bloc){
		this.nomSalle = nomSalle ; 
		this.bloc = bloc ;		// sans besoin de verifier si bloc est null  car il est initialis�e dans la methode ajoutersalle du class bloc.
	}
	
/******* getters *******************************************************/
	@Override
	public String getBloc() {
		return this.bloc.getNomBloc() ;
	}
	@Override
	public String getSalle() {
		return this.nomSalle ;
	}
	@Override
	public String getIdSalle() {
		return this.getSalle() + this.getBloc() ;
	}

	@Override
	public String getNom_Salle() {
		return this.getSalle() + " " + this.getBloc() ;
	}
}