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
        try {
            TypedQuery<Restaurant> query = em.createNamedQuery("Restaurant.findByCity", Restaurant.class);
            query.setParameter("city", city);
            return new HashSet<>(query.getResultList());
        }catch (Exception e) {
            logger.error("Error while fetching restaurants by city: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurants by city: " + e.getMessage());
        }
    }
    public Set<Restaurant> findByCityName(EntityManager em, String partialCityName) {
        try {
            TypedQuery<Restaurant> query = em.createNamedQuery("Restaurant.findByCityName", Restaurant.class);
            query.setParameter("name", partialCityName);
            return new HashSet<>(query.getResultList());
        }catch (Exception e) {
            logger.error("Error while fetching restaurants by city: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurants by city: " + e.getMessage());
        }
    }
    public Set<Restaurant> findByType(EntityManager em, RestaurantType type) {
        try {
            TypedQuery<Restaurant> query = em.createNamedQuery("Restaurant.findByType", Restaurant.class);
            query.setParameter("type", type);
            return query.getResultStream().collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error("Error while fetching restaurants by type: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurants by type: " + e.getMessage());
        }
    }

    public Set<Restaurant> findByName(EntityManager em, String searchedName) {
        try {
        TypedQuery<Restaurant> query = em.createNamedQuery("Restaurant.findByName", Restaurant.class);
        query.setParameter("name", searchedName);
        return new HashSet<>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching restaurant by name: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurant by name: " + e.getMessage());
        }
    }

    public Set<Restaurant> findAll(EntityManager em) {
        try {
            String findAllRestaurant = "SELECT r FROM Restaurant r";
            TypedQuery<Restaurant> query = em.createQuery(findAllRestaurant, Restaurant.class);
            return new HashSet<Restaurant>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all restaurants: " + e.getMessage());
            throw new RuntimeException("Error while fetching all restaurants: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(EntityManager em, Restaurant resto) {
        try {
            em.remove(resto);
            return true;
        } catch (Exception e) {
            // if the DELETE goes awry, hibernate will throw an exception
            return false;
        }
    }
}
