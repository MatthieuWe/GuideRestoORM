package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public abstract class AbstractMapper<T extends IBusinessObject> {

    protected static final Logger logger = LogManager.getLogger();
    public abstract Set<T> findAll(EntityManager em);
    public abstract boolean delete(EntityManager em, T object);
}
