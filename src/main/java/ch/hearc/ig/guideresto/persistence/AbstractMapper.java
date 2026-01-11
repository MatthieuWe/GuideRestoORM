package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.EntityManager;

import java.util.Set;

public abstract class AbstractMapper<T extends IBusinessObject> {

    public abstract Set<T> findAll(EntityManager em);

    /*
    * Les méthodes delete suppriment un enregistrement avec une requête jpql du genre (DELETE FROM...)
    * et pas avec l'EntityManager (em.remove())
    * Nous préférons gérer tout ce qui touche à l'EM et son cache (détach, merge, persist, remove), etc
    * dans la couche de service pour éviter des modifications de ce qui est en cache sur deux niveaux.
    *
    * > lors du rendu final, nous constatons que nous ne les utilisons plus depuis que nous avons implémenté
    * la gestion des transactions, mais on les laisse car leur implémentation était demandée lors de l'exercice 5.
     */
    public abstract boolean delete(EntityManager em, T object);
}
