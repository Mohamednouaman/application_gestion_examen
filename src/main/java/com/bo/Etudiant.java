package com.bo;

public class Etudiant {
    private String CNE ;
    private String nom ;
    private String prenom ;
    private String email ;

    public Etudiant( String CNE , String nom , String prenom , String email ){
    	this.nom = nom ;
    	this.prenom = prenom ;
        this.CNE = CNE ;
        this.email = email ;
    }

    public String getCNE(){
        return this.CNE ;
    }
    public String getEmail(){
        return this.email ;
    }
    public String getNom(){
        return this.nom ;
    }
    public String getPrenom(){
    	return this.prenom ;
    }
}

