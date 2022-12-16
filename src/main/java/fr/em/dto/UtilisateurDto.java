package fr.em.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UtilisateurDto {
    private String login;
    private String email;
    private String password;
}
