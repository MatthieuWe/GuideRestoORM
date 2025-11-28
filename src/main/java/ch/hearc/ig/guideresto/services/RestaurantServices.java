package ch.hearc.ig.guideresto.services;


import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import ch.hearc.ig.guideresto.presentation.Application;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.JarURLConnection;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class RestaurantServices {
    private final Connection connection;
    private static final Logger logger = LogManager.getLogger(RestaurantServices.class);
    private EntityManager em ;



    public RestaurantServices() {
        connection = ConnectionUtils.getConnection();
    }

    public Set<Restaurant> findAllRestaurant() {
        EntityManager em = JpaUtils.getEntityManager();
        try {
            String findAllRestaurant = "SELECT r FROM RESTAURANTS r";
            TypedQuery<Restaurant> query = em.createNamedQuery(findAllRestaurant, Restaurant.class);
            return new HashSet<Restaurant>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all restaurants: " + e.getMessage());
            throw new RuntimeException("Error while fetching all restaurants: " + e.getMessage());
        } finally {
          if(em != null && em.isOpen()) {
              em.close();
          }
        }

    }

    public Set<RestaurantType> findAllRestaurantType() {
        EntityManager em = JpaUtils.getEntityManager();
        try {
            String findAllRestaurantType = "SELECT r FROM TYPES_GASTRONOMIQUES r";
            TypedQuery<RestaurantType> query = em.createNamedQuery(findAllRestaurantType, RestaurantType.class);
            return new HashSet<RestaurantType>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all restaurant types: " + e.getMessage());
            throw new RuntimeException("Error while fetching all restaurant types: " + e.getMessage());
        } finally {
            if(em != null && em.isOpen()) {
                em.close();
            }
        }

    }

    public Set<City> findAllCities(){
        EntityManager em = JpaUtils.getEntityManager();
        try {
            String findAllCity = "SELECT r FROM VILLES r";
            TypedQuery<City> query = em.createNamedQuery(findAllCity, City.class);
            return new HashSet<City>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all cities: " + e.getMessage());
            throw new RuntimeException("Error while fetching all cities: " + e.getMessage());
        } finally {
            if(em != null && em.isOpen()) {
                em.close();
            }
        }


    }
    public Set<EvaluationCriteria> findAllEvaluationCriteria() {
        EntityManager em = JpaUtils.getEntityManager();
        try {
            String findAllEvalCriteria = "SELECT r FROM CRITERES_EVALUATION r";
            TypedQuery<EvaluationCriteria> query = em.createNamedQuery(findAllEvalCriteria, EvaluationCriteria.class);
            return new HashSet<EvaluationCriteria>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all evaluation criteria: " + e.getMessage());
            throw new RuntimeException("Error while fetching all evaluation criteria: " + e.getMessage());
        } finally {
            if(em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public Set<Restaurant> searchByName(String search){
        EntityManager em = JpaUtils.getEntityManager();
        try {
            String searchByName = "SELECT FROM RESTAURANTS r WHERE LOWER(r.nom) LIKE :searchedName ";
            TypedQuery<Restaurant> query = em.createQuery(searchByName, Restaurant.class);
            query.setParameter("searchedName", search.toLowerCase());
            return new HashSet<Restaurant>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching restaurant by name: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurant by name: " + e.getMessage());
        } finally {
            if(em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    public Set<Restaurant> searchByCity(String search){
        EntityManager em = JpaUtils.getEntityManager();
        try {
            String searchByCity = "SELECT FROM RESTAURANTS r INNER JOIN VILLES v ON v.numero = r.fk_vill WHERE LOWER(v.nom) LIKE :searchedCity ";
            TypedQuery<Restaurant> query = em.createQuery(searchByCity, Restaurant.class);
            query.setParameter("searchByCity", search.toLowerCase());
            return new HashSet<Restaurant>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching restaurant by name: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurant by name: " + e.getMessage());
        } finally {
            if(em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    public Set<Restaurant> searchByType(RestaurantType type){
        //ici il faut trouver un moyen de transformer le type en quelque chose qui peut ensuite être cherché
        //retrouver son ID dans notre base de donnée ?
        Integer typeId = type.getId(); // ??? j'y crois zero
        EntityManager em = JpaUtils.getEntityManager();
        try {
            String searchByType = "SELECT FROM RESTAURANTS r WHERE v.fk_type LIKE :searchedType ";
            TypedQuery<Restaurant> query = em.createQuery(searchByType, Restaurant.class);
            query.setParameter("searchedType", typeId);
            return new HashSet<Restaurant>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching restaurants by type: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurant by types: " + e.getMessage());
        } finally {
            if(em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public City createCity(String zipCode, String cityName) {
        City city = new City(zipCode, cityName);
        return cityMapper.create(city);
    }

    /*
    public Restaurant createRestaurant(String name, String description, String website, String street, City city, RestaurantType restaurantType) {
        Restaurant restaurant = new Restaurant(name, description, website, street, city, restaurantType);
        city.getRestaurants().add(restaurant);
        restaurantType.getRestaurants().add(restaurant);
        return restaurantMapper.create(restaurant);
    }
     */

    public BasicEvaluation createBasicEvaluation(Restaurant restaurant, Boolean like) {
        String ipAddress;
        try {
            ipAddress = Inet4Address.getLocalHost().toString(); // Permet de retrouver l'adresse IP locale de l'utilisateur.
        } catch (UnknownHostException ex) {
            logger.error("Error - Couldn't retreive host IP address");
            ipAddress = "Indisponible";
        }
        BasicEvaluation eval = new BasicEvaluation(new Date(), restaurant, like, ipAddress);
        restaurant.getEvaluations().add(eval);
        return basicEvaluationMapper.create(eval);
    }

    public CompleteEvaluation createCompleteEvaluation(Restaurant restaurant, String comment, String username) {
        CompleteEvaluation eval = new CompleteEvaluation(new Date(), restaurant, comment, username);
        eval = completeEvaluationMapper.create(eval);
        restaurant.getEvaluations().add(eval);
        return eval;
    }

    public Grade createGrade(Integer note, CompleteEvaluation eval, EvaluationCriteria currentCriteria) {
        Grade grade = new Grade(note, eval, currentCriteria);
        eval.getGrades().add(grade);
        return gradeMapper.create(grade);
    }

    public void updateRestaurant(Restaurant restaurant, RestaurantType newType, City newCity) {
        if (newType != null && newType != restaurant.getType()) {
            restaurant.getType().getRestaurants().remove(restaurant); // Il faut d'abord supprimer notre restaurant puisque le type va peut-être changer
            restaurant.setType(newType);
            newType.getRestaurants().add(restaurant);
        }
        if (newCity != null && newCity != restaurant.getAddress().getCity()) {
            restaurant.getAddress().getCity().getRestaurants().remove(restaurant); // On supprime l'adresse de la ville
            restaurant.getAddress().setCity(newCity);
            newCity.getRestaurants().add(restaurant);
        }
            restaurantMapper.update(restaurant);
    }

    public boolean deleteRestaurant(Restaurant restaurant){
        restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
        restaurant.getType().getRestaurants().remove(restaurant);
        return restaurantMapper.delete(restaurant);
    }

    public void shutdown() {
        ConnectionUtils.closeConnection();
    }

}
