package fr.em.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({"login","email","password","nom","prenom"})
public class CreateUserDto {

    private String login;
    private String email;
    private String password;
    private String nom;
    private String prenom;



}
