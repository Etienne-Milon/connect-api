package fr.em.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MailDto {
    private String to;
    private String subject;
    private String text;
}
