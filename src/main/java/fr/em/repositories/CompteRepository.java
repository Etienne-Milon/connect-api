package fr.em.repositories;

import fr.em.Entities.CompteEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class CompteRepository implements PanacheRepositoryBase<CompteEntity,String> {
}
