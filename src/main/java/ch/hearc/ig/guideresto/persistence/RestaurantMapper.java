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
            String searchByCity = "SELECT r FROM Restaurant r WHERE r.address.city = :city";
            TypedQuery<Restaurant> query = em.createQuery(searchByCity, Restaurant.class);
            query.setParameter("city", city);
            return new HashSet<>(query.getResultList());
        }catch (Exception e) {
            logger.error("Error while fetching restaurants by city: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurants by city: " + e.getMessage());
        }
    }
    public Set<Restaurant> findByCityName(EntityManager em, String partialCityName) {
        try {
            String searchByCity = "SELECT r FROM Restaurant r WHERE LOWER(r.address.city.cityName) LIKE '%' + LOWER(:name) + '%'";
            TypedQuery<Restaurant> query = em.createQuery(searchByCity, Restaurant.class);
            query.setParameter("name", partialCityName);
            return new HashSet<>(query.getResultList());
        }catch (Exception e) {
            logger.error("Error while fetching restaurants by city: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurants by city: " + e.getMessage());
        }
    }
    public Set<Restaurant> findByType(EntityManager em, RestaurantType type) {
        try {

            String searchByType = "SELECT r FROM Restaurant r WHERE r.type.id = :typeId";
            TypedQuery<Restaurant> query = em.createQuery(searchByType, Restaurant.class);
            query.setParameter("typeId", type.getId());
            return query.getResultStream().collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error("Error while fetching restaurants by type: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurants by type: " + e.getMessage());
        }
    }

    public Set<Restaurant> findByName(EntityManager em, String searchedName) {
        try {
        String searchByName = "SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE :searchedName";
        TypedQuery<Restaurant> query = em.createQuery(searchByName, Restaurant.class);
        query.setParameter("searchedName", "%" + searchedName.toLowerCase() + "%");
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
        em.remove(resto);
        // TODO fix this. it's stupid. merci Matthieu.
        return true;
    }

    @Override
    protected String getCountQuery() {
        return "SELECT Count(res) FROM Restaurant res";
    }


}
