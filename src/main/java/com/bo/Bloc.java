package com.bo;

import java.util.Comparator;
import java.util.Collection;
import java.util.TreeSet;
import java.util.ArrayList;

public class Bloc {

/**** permet de definir les bloc et leurs classes dans l'ecole ***********/
	
	private String nomBloc ;    // identifiant de Bloc
	private Collection<SalleBloc> salles ;
	private Collection<SalleBlocTP> sallesTP ;
	private Collection<SalleBlocTheorique> sallesTheorique ;
	
/**** construction par nom seulement : ***********************************/
	public Bloc(String nomBloc) {
		this.nomBloc = nomBloc ; // retirer les espace   ;  null pointer exception!
		this.sallesTP = new TreeSet <> (
			new Comparator<SalleBlocTP>() {
				@Override
				public int compare(SalleBlocTP o1, SalleBlocTP o2) {
					return o1.nomSalle.compareTo(o2.nomSalle);
				}				
			}
		) ;
		this.sallesTheorique = new TreeSet <> (
			new Comparator<SalleBlocTheorique>() {
				@Override
				public int compare(SalleBlocTheorique o1, SalleBlocTheorique o2) {
					return o1.nomSalle.compareTo(o2.nomSalle);
				}				
			}
		) ;
		this.salles = new TreeSet <> (
			new Comparator<SalleBloc>() {
				@Override
				public int compare(SalleBloc o1, SalleBloc o2) {
					return o1.nomSalle.compareTo(o2.nomSalle);
				}				
			}
		) ;
		// TreeSet pour que les salle soit tri�es par nom.
	}
	
/**** ajout d'une salle dans un bloc**************************************/
	public void ajouterSalleBloc(String salle , boolean pourTP) {
		if( salle == null ) return ;
		try {
			if( pourTP == true ) {			
				salles.add(new SalleBlocTP(salle , this)) ;
				sallesTP.add(new SalleBlocTP(salle , this)) ;
			}
			if( pourTP == false ) {
				salles.add(new SalleBlocTheorique(salle , this)) ;
				sallesTheorique.add(new SalleBlocTheorique(salle , this)) ;
			}
		}catch(NullPointerException ignore) {
		}catch(Exception ignore) {
		}
	}
		
/**** Recuperer le nom de bloc ou les salles existant dans le bloc ********/
	public String getNomBloc() {
		return this.nomBloc ;
	}
	
/**** recuperer les salles du bloc ***************************************/
	public ArrayList<SalleBloc> getSalles(){
		return new ArrayList<>( this.salles ) ;
	}
	
/**** recuperer les salles du tp *****************************************/
	public ArrayList< SalleBlocTP > getSallesTP(){
		return new ArrayList<>( this.sallesTP ) ;		
	}
	
/**** recuperer les salles theorique *************************************/
	public ArrayList < SalleBlocTheorique > getSallesTheorique(){
		return new ArrayList<>( this.sallesTheorique ) ;
	}	
}
