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
   
   public RestaurantType findById(int id, EntityManager em) {
      RestaurantType type=null;
      type=em.find(RestaurantType.class, id);
      return type;
   }
    
    public RestaurantType findByLabel(String label, EntityManager em) {
       String query = "SELECT r FROM TYPES_GASTRONOMIQUES r WHERE r.libelle LIKE '%"+label+"%'";
       TypedQuery<RestaurantType> typeQuery = em.createQuery(query, RestaurantType.class);
       return (RestaurantType) typeQuery.getResultList();
    }

    public Set<RestaurantType> findAll(EntityManager em) {
       String query = "SELECT r FROM TYPES_GASTRONOMIQUES r";
       TypedQuery<RestaurantType> typeQuery = em.createQuery(query, RestaurantType.class);
       return new HashSet<>(typeQuery.getResultList());
    }
    
    public RestaurantType create(RestaurantType type) {
        try {
            String generatedColumns[] = { "numero" };
            PreparedStatement s = connection.prepareStatement(
                    "INSERT INTO types_gastronomiques (libelle, description)" +
                    "VALUES (?, ?)",
                    generatedColumns);
            s.setString(1, type.getLabel());
            s.setString(2, type.getDescription());
            s.executeUpdate();
            ResultSet rs = s.getGeneratedKeys();
            if (rs.next()) {
                type.setId(rs.getInt(1));
                super.addToCache(type);
            } else {
                logger.warn("Failed to insert type into the table: ", type.getLabel() + ". Continuing..." );
            }
            rs.close();
            connection.commit();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                // le type existe deja: violation de contrainte unique sur le libelle
                // -> on gère. retourne l'id comme si tout s'était bien passé.
                type = this.findByLabel(type.getLabel());
                logger.warn("Type already exists: " + type.getLabel() + ". Continuing...");
            } else {
            logger.error("SQLException: {}", e.getMessage());
            }
        }
        return type;
    }
    public boolean update(RestaurantType type) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "UPDATE types_gastronomiques SET libelle = ?, description = ? WHERE numero = ?");
            s.setString(1, type.getLabel());
            s.setString(2, type.getDescription());
            s.setInt(3, type.getId());
            affectedRows = s.executeUpdate();
            super.addToCache(type);
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        return affectedRows > 0;
    }
    public boolean delete(RestaurantType type) {
        return this.deleteById(type.getId());
    }
    public boolean deleteById(int id) {
        int affectedRows = 0;
        try {
            PreparedStatement s = connection.prepareStatement(
                    "DELETE types_gastronomiques WHERE numero = ?");
            s.setInt(1, id);
            affectedRows = s.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException: {}", e.getMessage());
        }
        if(affectedRows > 0) {
            super.removeFromCache(id);
            return true;
        } else {
            return false;
        }
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

