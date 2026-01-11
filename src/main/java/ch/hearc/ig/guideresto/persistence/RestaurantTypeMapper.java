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
       return typeQuery.getSingleResult();
    }

    @Override
    public Set<RestaurantType> findAll(EntityManager em) {
        String findAllRestaurantType = "SELECT t FROM RestaurantType t ";
        TypedQuery<RestaurantType> query = em.createQuery(findAllRestaurantType, RestaurantType.class);
        return new HashSet<RestaurantType>(query.getResultList());
    }

    @Override
    public boolean delete(EntityManager em, RestaurantType restaurantType) {
        int deletedCount = em.createQuery("DELETE FROM RestaurantType ty WHERE ty = :restaurantType")
                .setParameter("restaurantType", restaurantType)
                .executeUpdate();
        return deletedCount > 0;
    }
}

