package ch.hearc.ig.guideresto.services;


import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.BasicEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.CityMapper;
import ch.hearc.ig.guideresto.persistence.RestaurantMapper;
import ch.hearc.ig.guideresto.persistence.RestaurantTypeMapper;
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

    //mappers
    private RestaurantMapper restaurantMapper ;
    private CityMapper cityMapper ;
    private RestaurantTypeMapper typeMapper ;

    public RestaurantServices() {
        em = JpaUtils.getEntityManager();
        restaurantMapper = new RestaurantMapper();
        cityMapper = new CityMapper();
        typeMapper = new RestaurantTypeMapper();
    }

    public Set<Restaurant> findAllRestaurant() {
        return restaurantMapper.findAll(em);
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

    public Set<Restaurant> searchByName(String search){
        return restaurantMapper.findByName(em, search);
    }
    /*
    Cette méthode recherche toutes les villes contenant la chaine fournie (nom de ville n'est pas unique en DB
    et le programme accepte une partie du nom) puis retourne tous les restos de chaque ville correspondante à la recherche
    Note:
    Solution A - c'est une opportunité d'utiliser notre cityMapper MAIS
    Solution B -  c'est plus efficace d'adapter notre méthode findByCity Dans le restaurantMapper pour qu'elle fasse
    tout ça directement en JPQL avec une jointure. Quand on boucle sur un resultset pour refaire des select, ya un problème
     */
    public Set<Restaurant> searchByCity(String search){
        // Solution A
        Set<City> cities = cityMapper.findByName(em, search);
        Set<Restaurant> restos = new HashSet<>();
        for (City city : cities) {
            restos.addAll(restaurantMapper.findByCity(em, city));
        }
        return restos;
        // Solution B - meilleur
        /*
        return restaurantMapper.findByCityName(em, search);
        */
    }
    public Set<Restaurant> searchByType(RestaurantType type){
        return restaurantMapper.findByType(em, type);
    }

    public City createCity(String zipCode, String cityName) {
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        City city = new City(zipCode, cityName);
        em.persist(city);
        tx.commit();
        return city; //à tester la persistance
    }


    public Restaurant createRestaurant(String name, String description, String website, String street, City city, RestaurantType restaurantType) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setWebsite(website);
        restaurant.setAddress(new Localisation(street, city));
        restaurant.setType(restaurantType);
        em.persist(restaurant);
        tx.commit();

        return restaurant;
    }

    public void updateRestaurant(Restaurant restaurant, RestaurantType newType, City newCity) {
        EntityTransaction tx = em.getTransaction();
        try{
            restaurant = em.find(Restaurant.class, restaurant.getId());
            if (restaurant == null) {
                logger.warn("Restaurant with ID " + restaurant.getId() + " not found for update.");
                return;
            }
            tx.begin();
            em.detach(restaurant); // why ??
            restaurant.setType(newType);
            restaurant.setAddress(new Localisation (restaurant.getAddress().getStreet(), newCity));
            em.merge(restaurant);

            tx.commit();
        } catch (Exception e) {
            logger.error("Error while updating restaurant: " + e.getMessage());
            tx.rollback();
        }
    }

    public boolean deleteRestaurant(Restaurant restaurant){
        EntityTransaction tx = em.getTransaction();
        try {
            restaurant = em.find(Restaurant.class, restaurant.getId());
            if (restaurant == null) {
                logger.warn("Restaurant with ID " + restaurant.getId() + " not found for deletion.");
                return false;
            }
            tx.begin();
            // TODO c'est pas mieux mais on peut utiliser un de nos mappers ici
            for (Evaluation eval : new HashSet<>(restaurant.getEvaluations())) {
                em.remove(eval);
            }
            em.remove(restaurant);
            // TODO vérifier si la ville est encore utilisée et sinon l'effacer aussi
            tx.commit();
            return true;

        } catch (Exception e) {
            logger.error("Error while deleting associated evaluations: " + e.getMessage());
            tx.rollback();
            return false;
        }
    }

    public void shutdown() {
        em.close();
    }

    public String test(Restaurant r) {
        Set<Evaluation> be = new BasicEvaluationMapper().findByRestaurant(em, r);
        StringBuilder sb = new StringBuilder();
        for (Evaluation eval : be) {
            sb.append(eval.toString());
        }
        return sb.toString();
    }

}
