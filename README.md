-----------*****Base de données*****-------------------


CREATE TABLE `CoordonnateurFiliere` (

    username VARCHAR(30) PRIMARY KEY ,
    
    nom VARCHAR(20) ,
    
    prenom VARCHAR(20) ,
    
    email VARCHAR(50) ,
    
    CHECK ( TRIM(email) != "" ) ,
    
    mot_de_passe CHAR(6) ,
    
    CHECK ( TRIM(mot_de_passe) != "" )
    
) ;


CREATE TABLE `CoordonnateurDepartement` (

    username VARCHAR(30) PRIMARY KEY ,
    
    nom VARCHAR(20) ,
    
    prenom VARCHAR(20) ,
    
    email VARCHAR(50) ,
    
    CHECK ( TRIM(email) != "" ) ,
    
    mot_de_passe CHAR(6) ,
    
    CHECK ( TRIM(mot_de_passe) != "" ) 
    
) ;


CREATE TABLE `Departement` (

    nomDepartement VARCHAR(50) PRIMARY KEY ,
    
    chefDepartement VARCHAR(30) ,
    
    FOREIGN KEY (chefDepartement) REFERENCES CoordonnateurDepartement(username) 
    
) ;


CREATE TABLE `Enseignant` (

    username VARCHAR(30) PRIMARY KEY ,
    
    nom VARCHAR(20) ,
    
    prenom VARCHAR(20) ,
    
    email VARCHAR(50) ,
    
    mot_de_passe CHAR(6) ,
    
    CHECK ( TRIM(mot_de_passe) != "" ) ,
    
    departement VARCHAR(50) ,
    
    FOREIGN KEY (departement) REFERENCES Departement(nomDepartement)  ,
    
    CHECK ( TRIM(email) != "" )
    
) ;


CREATE TABLE `Filiere` (

    nomFiliere VARCHAR(50) PRIMARY KEY,
    
    usernameCoordonnateur VARCHAR(30) ,
    
    departementContenant VARCHAR(50) ,
    
    FOREIGN KEY (usernameCoordonnateur) REFERENCES CoordonnateurFiliere (username) ,
    
    FOREIGN KEY (departementContenant)  REFERENCES Departement (nomDepartement)  
    
) ;


CREATE TABLE `AnneeFiliere` (

    id VARCHAR(60) PRIMARY KEY ,
    
    niveau INTEGER ,
    
    optionfiliere VARCHAR(50) ,
    
    filiere VARCHAR(50) , 
    
    FOREIGN KEY (filiere) REFERENCES Filiere(nomFiliere)  ,
    
    CHECK (niveau>=1 AND niveau<=3)
    
) ;


CREATE TABLE Module (

    nomModule VARCHAR(50) PRIMARY KEY ,
    
    semestre integer NOT NULL ,
    
    enseignantmodule VARCHAR(30) ,
    
    anneefiliere VARCHAR(60) ,
    
    FOREIGN KEY (enseignantmodule) REFERENCES Enseignant(username) ,
    
    FOREIGN KEY (anneefiliere) REFERENCES AnneeFiliere(id) ,
    
    CHECK(semestre IN (1,2)) 
    
) ;


CREATE TABLE `Etudiant` (

    CNE VARCHAR(20) PRIMARY KEY ,
    
    nom VARCHAR(20) ,
    
    prenom VARCHAR(20) ,
    
    email VARCHAR(50) ,
    
    mot_de_passe VARCHAR(20) ,
    
    CHECK (  LENGTH(TRIM(mot_de_passe))   >=  4   ) ,
    
    anneefiliere VARCHAR(60) ,
    
    FOREIGN KEY (anneefiliere) REFERENCES AnneeFiliere(id) ,
    
    CHECK (  TRIM(email)  !=  ""  )
    
) ;


CREATE TABLE `Bloc` (

    nomBloc VARCHAR(10) PRIMARY KEY ,
    
    CHECK( TRIM(nomBloc) != "" )
    
) ;


CREATE TABLE `Salle` (

    id VARCHAR(20) PRIMARY KEY ,
    
    nomSalle VARCHAR(10) NOT NULL ,
    
    pourTP BOOLEAN NOT NULL ,
    
    bloc VARCHAR(10) ,
    
    FOREIGN KEY (bloc) REFERENCES Bloc(nomBloc) ,
    
    isAmphi BOOLEAN NOT NULL ,
    
    CHECK ( TRIM(nomSalle) != "" )
    
) ;


CREATE TABLE SalleExamenTPModule (

    id VARCHAR(50) PRIMARY KEY,
    
    module VARCHAR(20) ,
    
    id_salle VARCHAR(20) ,
    
    FOREIGN KEY (module) REFERENCES Module( nomModule ) ,
    
    FOREIGN KEY (id_salle) REFERENCES Salle ( id ) 
    
) ;


CREATE TABLE Examen (

    id integer AUTO_INCREMENT PRIMARY key,
    
    anneeFiliere VARCHAR(60) ,
    
    module VARCHAR(50) ,
    
    dateExamen DATE ,
    
    timeExamen TIME , 
    
    pourTP BOOLEAN   ,
    
    FOREIGN KEY (anneeFiliere) REFERENCES AnneeFiliere(id) ,
    
    FOREIGN KEY (module) REFERENCES Module(  nomModule )  ,
    
    duree INTEGER CHECK(duree < 240 AND duree > 0 ) 
    
) ;


CREATE TABLE SalleExamen (

   examen INTEGER , 
   
   salle VARCHAR(20) ,
   
   enseignantcontroleur VARCHAR(30) ,
   
   FOREIGN KEY (examen) REFERENCES Examen( id ) ,
   
   FOREIGN KEY (salle) REFERENCES Salle( id )  ,
   
   FOREIGN KEY (enseignantcontroleur) REFERENCES Enseignant( username ) 
   
) ;      



CREATE TABLE PeriodeExamen (

    id_periode INTEGER AUTO_INCREMENT PRIMARY KEY ,
    
    date_debut DATE NOT NULL ,
    
    date_fin DATE NOT NULL 
    
) ;
