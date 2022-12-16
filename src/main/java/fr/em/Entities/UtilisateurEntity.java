package fr.em.Entities;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;

@Data
@Entity
@Table(name = "utilisateur", schema = "dbo", catalog = "CUIB")
public class UtilisateurEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "login")
    private String login;
    @Basic
    @Column(name = "nom")
    private String nom;
    @Basic
    @Column(name = "prenom")
    private String prenom;
    @Basic
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
