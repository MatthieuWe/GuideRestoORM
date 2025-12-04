package ch.hearc.ig.guideresto.services;


import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class RestaurantServices {
    private static final Logger logger = LogManager.getLogger(RestaurantServices.class);
    private final EntityManager em ;

    public RestaurantServices() {
        em = JpaUtils.getEntityManager();
    }

    public Set<Restaurant> findAllRestaurant() {
        try {
            String findAllRestaurant = "SELECT r FROM Restaurant r";
            TypedQuery<Restaurant> query = em.createQuery(findAllRestaurant, Restaurant.class);
            return new HashSet<Restaurant>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all restaurants: " + e.getMessage());
            throw new RuntimeException("Error while fetching all restaurants: " + e.getMessage());
        }

    }

    public Set<RestaurantType> findAllRestaurantType() {
        try {
            String findAllRestaurantType = "SELECT t FROM RestaurantType t ";
            TypedQuery<RestaurantType> query = em.createQuery(findAllRestaurantType, RestaurantType.class);
            return new HashSet<RestaurantType>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all restaurant types: " + e.getMessage());
            throw new RuntimeException("Error while fetching all restaurant types: " + e.getMessage());
        }
    }

    public Set<City> findAllCities(){
        try {
            String findAllCity = "SELECT c FROM City c";
            TypedQuery<City> query = em.createQuery(findAllCity, City.class);
            return new HashSet<City>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all cities: " + e.getMessage());
            throw new RuntimeException("Error while fetching all cities: " + e.getMessage());
        }


    }
    public Set<EvaluationCriteria> findAllEvaluationCriteria() {
        EntityManager em = JpaUtils.getEntityManager();
        try {
            String findAllEvalCriteria = "SELECT ec FROM EvaluationCriteria ec";
            TypedQuery<EvaluationCriteria> query = em.createQuery(findAllEvalCriteria, EvaluationCriteria.class);
            return new HashSet<EvaluationCriteria>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all evaluation criteria: " + e.getMessage());
            throw new RuntimeException("Error while fetching all evaluation criteria: " + e.getMessage());
        }
    }

    public Set<Restaurant> searchByName(String search){
        try {
            String searchByName = "SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE :searchedName";
            TypedQuery<Restaurant> query = em.createQuery(searchByName, Restaurant.class);
            query.setParameter("searchedName", "%" + search.toLowerCase() + "%");
            return new HashSet<Restaurant>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching restaurant by name: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurant by name: " + e.getMessage());
        }
    }
    public Set<Restaurant> searchByCity(String search){
        EntityManager em = JpaUtils.getEntityManager();
        try {
            String searchByCity = "SELECT r FROM Restaurant r WHERE LOWER(r.address.city.cityName) LIKE :searchedCity ";
            TypedQuery<Restaurant> query = em.createQuery(searchByCity, Restaurant.class);
            query.setParameter("searchedCity", "%" + search.toLowerCase() + "%");
            return new HashSet<Restaurant>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching restaurant by name: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurant by name: " + e.getMessage());
        }
    }
    public Set<Restaurant> searchByType(RestaurantType type){
        //ici il faut trouver un moyen de transformer le type en quelque chose qui peut ensuite être cherché
        //retrouver son ID dans notre base de donnée ?
        Integer typeId = type.getId(); // ??? j'y crois zero - moi j'y crois *1000 (MW)
        try {
            String searchByType = "SELECT r FROM Restaurant r WHERE r.type.id = :searchedType";
            // par contre c'est pas opti de chercher l'ID après une jointure. On pourrait chercher directement sur la fk...
            // d'après Gemini, JPQL est assez malin pour optimiser la requete et éviter la jointure. on ne doit se préoccuper
            // que de la navigation en mode objet. a tester avec showSQL = true pour voir si c'est vrai
            TypedQuery<Restaurant> query = em.createQuery(searchByType, Restaurant.class);
            query.setParameter("searchedType", typeId);
            return new HashSet<Restaurant>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching restaurants by type: " + e.getMessage());
            throw new RuntimeException("Error while fetching restaurant by types: " + e.getMessage());
        }
    }

    public City createCity(String zipCode, String cityName) {
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        City city = new City(zipCode, cityName);
        em.persist(city);
        tx.commit();
        return city; //à tester la persistance
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
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        BasicEvaluation eval = new BasicEvaluation(new Date(), restaurant, like, ipAddress);
        restaurant.getEvaluations().add(eval);//ici je dois faire quelque chose pour la FK non ?

        em.persist(eval);
        tx.commit();

        return eval; //basicEvaluationMapper.create(eval); à voir à nouveau si c'est tout bon comme ça ou pas ?
    }

    public CompleteEvaluation createCompleteEvaluation(Restaurant restaurant, String comment, String username) {
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        CompleteEvaluation eval = new CompleteEvaluation(new Date(), restaurant, comment, username);
        //eval = completeEvaluationMapper.create(eval);
        restaurant.getEvaluations().add(eval); //ici je dois faire quelque chose pour la FK non ?

        em.persist(eval);
        tx.commit();

        return eval;
    }

    public Grade createGrade(Integer note, CompleteEvaluation eval, EvaluationCriteria currentCriteria) {
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        Grade grade = new Grade(note, eval, currentCriteria);
        eval.getGrades().add(grade);//ici je dois faire quelque chose pour la FK non ?

        em.persist(grade);
        tx.commit();

        return grade;
    }

    public void updateRestaurant(Restaurant restaurant, RestaurantType newType, City newCity) { //je pense que ça marche pas
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.detach(restaurant);
        restaurant.setType(newType);
        restaurant.setAddress(new Localisation (restaurant.getAddress().getStreet(), newCity));
        em.merge(restaurant);

        tx.commit();

    }

    public boolean deleteRestaurant(Restaurant restaurant){
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        em.remove(restaurant);

        //restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
        //restaurant.getType().getRestaurants().remove(restaurant);

        tx.commit();

        return true; //je dois réfléchir
    }

    public void shutdown() {
        em.close();
    }

}
