package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.RestaurantType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.*;
import java.sql.*;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {

    public RestaurantType findByLabel(EntityManager em, String label) {
       TypedQuery<RestaurantType> typeQuery = em.createNamedQuery("RestaurantType.findByLabel", RestaurantType.class);
       typeQuery.setParameter("label", label);
       return typeQuery.getSingleResult();// TODO a tester mais on peut, libelle est UNIQUE
    }

    public Set<RestaurantType> findAll(EntityManager em) {
       String query = "SELECT ty FROM RestaurantType ty";
       TypedQuery<RestaurantType> typeQuery = em.createQuery(query, RestaurantType.class);
       return new HashSet<>(typeQuery.getResultList());
    }
    
    public boolean delete(EntityManager em, RestaurantType restaurantType) {
        em.remove(restaurantType);
        return true;

    }
}

