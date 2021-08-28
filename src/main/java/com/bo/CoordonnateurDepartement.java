package com.bo;

public class CoordonnateurDepartement {
    private String username;
    private String nom;
    private String prenom;
    private String email;

    public CoordonnateurDepartement( String username , String nom , String prenom , String email ){
        this.username = username ;
        this.nom = nom ;
        this.prenom = prenom ;
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
}
