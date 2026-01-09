package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Grade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Set;

public class CityMapper extends AbstractMapper<City> {

    public Set<City> findAll(EntityManager em) {
        try {
            String findAllCity = "SELECT c FROM City c";
            TypedQuery<City> query = em.createQuery(findAllCity, City.class);
            return new HashSet<City>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all cities: " + e.getMessage());
            throw new RuntimeException("Error while fetching all cities: " + e.getMessage());
        }
    }
    
    public Set<City> findByName(EntityManager em, String partialName) {
        TypedQuery<City> cityQuery = em.createNamedQuery("City.findByName", City.class);
        cityQuery.setParameter("name", "%" + partialName.toLowerCase() + "%");
        return new HashSet<>(cityQuery.getResultList());
    }
    public boolean purgeCity(EntityManager em, City city) throws Exception {
        int deletedCount = em.createQuery("DELETE FROM City ci WHERE ci = :city" +
                " AND NOT EXISTS (SELECT r FROM Restaurant r WHERE r.address.city = :city)")
                .setParameter("city", city)
                .executeUpdate();
        return deletedCount > 0;
    }
    public boolean delete(EntityManager em, City city) {
        try {
            em.remove(city);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
