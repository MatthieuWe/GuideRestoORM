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
        try {
            String findAllRestaurantType = "SELECT t FROM RestaurantType t ";
            TypedQuery<RestaurantType> query = em.createQuery(findAllRestaurantType, RestaurantType.class);
            return new HashSet<RestaurantType>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all restaurant types: " + e.getMessage());
            throw new RuntimeException("Error while fetching all restaurant types: " + e.getMessage());
        }
    }

    public boolean delete(EntityManager em, RestaurantType restaurantType) {
        try {
            em.remove(restaurantType);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

