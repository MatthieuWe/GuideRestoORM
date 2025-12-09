package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.RestaurantType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.*;
import java.sql.*;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {
   private final Connection connection;
   
   public RestaurantTypeMapper(Connection connection) {
      this.connection=connection;
   }
    
    public RestaurantType findByLabel(EntityManager em, String label) {
       String query = "SELECT r FROM TYPES_GASTRONOMIQUES r WHERE r.libelle LIKE '%"+label+"%'";
       TypedQuery<RestaurantType> typeQuery = em.createQuery(query, RestaurantType.class);
       return (RestaurantType) typeQuery.getResultList();
    }

    public Set<RestaurantType> findAll(EntityManager em) {
       String query = "SELECT r FROM TYPES_GASTRONOMIQUES r";
       TypedQuery<RestaurantType> typeQuery = em.createQuery(query, RestaurantType.class);
       return new HashSet<>(typeQuery.getResultList());
    }
    
    public boolean delete(EntityManager em, RestaurantType restaurantType) {
        em.remove(restaurantType);
        return true;

    }
    protected String getSequenceQuery(){
        return "SELECT seq_types_gastronomiques.NextVal FROM dual";
    }
    protected String getExistsQuery() {
        return "SELECT numero FROM types_gastronomiques WHERE numero = ?";
    }
    protected String getCountQuery() {
        return "SELECT Count(*) FROM types_gastronomiques";
    }
}

