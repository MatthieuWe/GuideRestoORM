package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Set;
import java.sql.*;

public class CityMapper extends AbstractMapper<City> {
    private final Connection connection;
    
    public CityMapper(Connection connection) {
        this.connection = connection;
    }

    public Set<City> findAll(EntityManager em) {
        String query =  "SELECT v FROM villes v";
        TypedQuery<City> cityQuery = em.createQuery(query, City.class);
        return new HashSet<>(cityQuery.getResultList());
    }
    
    public Set<City> findByName(EntityManager em, String partialName) {
        String query = "SELECT v FROM villes v WHERE v.nom_ville LIKE '%"+partialName+"%'";
        TypedQuery<City> cityQuery = em.createQuery(query, City.class);
        return new HashSet<>(cityQuery.getResultList());
    }
    
    public boolean delete(EntityManager em, City city) {
       em.remove(city);
       return true;
    }
    
    protected String getCountQuery() {
        return "SELECT Count(v) FROM villes v";
    }

}
