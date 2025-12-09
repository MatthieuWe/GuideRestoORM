package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Set;
import java.sql.*;
import java.util.stream.Collectors;


public class RestaurantMapper extends AbstractMapper<Restaurant> {


    public RestaurantMapper() {
    }

    public Restaurant findById(int id, EntityManager em) {
        return em.find(Restaurant.class, id);
    }
    public Set<Restaurant> findForCity(City city, EntityManager em) {
        try {
            String searchByCity = "SELECT r FROM Restaurant r WHERE r.address.city.id = :cityId";
            TypedQuery<Restaurant> query = em.createQuery(searchByCity, Restaurant.class); // C'est faux je sais c'est juste pour la réflection
            query.setParameter("cityId", city.getId());
            return new HashSet<>(query.getResultList());
        }catch (Exception e) {
            logger.error("Error while fetching restaurants by city: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurants by city: " + e.getMessage());
        }
    }
    public Set<Restaurant> findForType(RestaurantType type, EntityManager em) {
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
    public Set<Restaurant> findByName(String searchedName, EntityManager em) {
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


}
