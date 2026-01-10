package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RestaurantMapper extends AbstractMapper<Restaurant> {

    public Set<Restaurant> findByCity(EntityManager em, City city) {
        TypedQuery<Restaurant> query = em.createNamedQuery("Restaurant.findByCity", Restaurant.class);
        query.setParameter("city", city);
        return new HashSet<>(query.getResultList());
    }
    public Set<Restaurant> findByCityName(EntityManager em, String partialCityName) {
        TypedQuery<Restaurant> query = em.createNamedQuery("Restaurant.findByCityName", Restaurant.class);
        query.setParameter("name", partialCityName);
        return new HashSet<>(query.getResultList());
    }
    public Set<Restaurant> findByType(EntityManager em, RestaurantType type) {
        TypedQuery<Restaurant> query = em.createNamedQuery("Restaurant.findByType", Restaurant.class);
        query.setParameter("type", type);
        return query.getResultStream().collect(Collectors.toSet());
    }

    public Set<Restaurant> findByName(EntityManager em, String searchedName) {
        TypedQuery<Restaurant> query = em.createNamedQuery("Restaurant.findByName", Restaurant.class);
        query.setParameter("name", searchedName);
        return new HashSet<>(query.getResultList());
    }

    public Set<Restaurant> findAll(EntityManager em) {
        String findAllRestaurant = "SELECT r FROM Restaurant r";
        TypedQuery<Restaurant> query = em.createQuery(findAllRestaurant, Restaurant.class);
        return new HashSet<Restaurant>(query.getResultList());
    }

    @Override
    public boolean delete(EntityManager em, Restaurant resto) {
        int deletedCount = em.createQuery("DELETE FROM Restaurant r WHERE r = :resto")
                .setParameter("resto", resto)
                .executeUpdate();
        return deletedCount > 0;
    }
}
