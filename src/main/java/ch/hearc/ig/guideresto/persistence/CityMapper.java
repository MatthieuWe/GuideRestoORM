package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Grade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Set;

public class CityMapper extends AbstractMapper<City> {

    public Set<City> findAll(EntityManager em) {
        String query =  "SELECT ci FROM City ci";
        TypedQuery<City> cityQuery = em.createQuery(query, City.class);
        return new HashSet<>(cityQuery.getResultList());
    }
    
    public Set<City> findByName(EntityManager em, String partialName) {
        TypedQuery<City> cityQuery = em.createNamedQuery("City.findByName", City.class);
        cityQuery.setParameter("name", "%" + partialName.toLowerCase() + "%");
        return new HashSet<>(cityQuery.getResultList());
    }
    public void purgeCity(EntityManager em, City city) throws Exception {
        em.createQuery("DELETE FROM City ci WHERE ci = :city" +
                " AND (SELECT Count(*) FROM Restaurant r WHERE r.address.city = :city) = 0")
                .setParameter("city", city)
                .executeUpdate();
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
