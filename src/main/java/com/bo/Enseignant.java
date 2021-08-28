package com.bo;

public class Enseignant {
    private String username;
    private String nom;
    private String prenom;
    private String email;

    public Enseignant( String username , String nom , String prenom , String email ){
    	this.username = username ;
    	this.prenom = prenom ;
    	this.nom = nom ;
    	this.email = email ;
    }

    public String getUsername(){
        return this.username ;
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
    /*
     *  on va redefinir equals et hashCode car on leur aura besoin...
     */
    @Override
    public boolean equals(Object obj) {
    	return this.username.equals(    ((Enseignant)obj).username    ) ;
    }
    @Override 
    public int hashCode() {
    	return this.username.hashCode() ;
    }
    
}
