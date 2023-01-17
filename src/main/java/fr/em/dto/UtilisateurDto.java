package fr.em.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import fr.em.Entities.UtilisateurEntity;
import fr.em.hateoas.HateOAS;

import lombok.Getter;

import java.sql.Date;


@Getter
@JsonPropertyOrder({"login","email","password","role","nom","prenom","debutAdhesion","finAdhesion"})
public class UtilisateurDto extends HateOAS {

    private String login;
    private String email;
    private String nom;
    private String prenom;
    private String role;
    private Date debutAdhesion;
    private Date finAdhesion;
    private String password;

    public UtilisateurDto(UtilisateurEntity user) {
        this.login = user.getLogin();
        this.email = user.getEmail();
        this.nom = user.getNom();
        this.prenom = user.getPrenom();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.debutAdhesion = user.getDateDebutAdhesion();
        this.finAdhesion = user.getDateFinAdhesion();
    }
}
