package fr.em.Entities;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Data
@Table(name = "compte", schema = "dbo", catalog = "CUIB")
public class CompteEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "num_adherent")
    private int numAdherent;
    @Basic
    @Column(name = "nom")
    private String nom;
    @Basic
    @Column(name = "prenom")
    private String prenom;
    @Basic
    @Column(name = "adresse")
    private String adresse;
    @Basic
    @Column(name = "codepostal")
    private String codepostal;
    @Id
    @Column(name = "email")
    private String email;
    @Basic
    @Column(name = "passwordhash")
    private String passwordhash;
    @Basic
    @Column(name = "date_debut_adhesion")
    private Date dateDebutAdhesion;
    @Basic
    @Column(name = "date_fin_adhesion")
    private Date dateFinAdhesion;
    @Basic
    @Column(name = "role")
    private String role;


}
