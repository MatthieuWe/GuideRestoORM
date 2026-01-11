package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.EntityManager;

import java.util.Set;

public abstract class AbstractMapper<T extends IBusinessObject> {

    public abstract Set<T> findAll(EntityManager em);
    public abstract boolean delete(EntityManager em, T object);
}
