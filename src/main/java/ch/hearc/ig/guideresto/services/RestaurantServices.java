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

    /*
    * Cette méthode crée une nouvelle ville en mémoire mais ne la persiste pas !
    * Elle sera persisté dans une seule et même transaction lors de la création du restaurant,
    * Si cette transaction échoue, on n'a pas besoin de cette nouvelle ville en mémoire.
     */
    public City createCity(String zipCode, String cityName) {
        return new City(zipCode, cityName);
    }


    public Restaurant createRestaurant(String name, String description, String website, String street, City city, RestaurantType restaurantType) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setWebsite(website);
        restaurant.setAddress(new Localisation(street, city));
        restaurant.setType(restaurantType);

        JpaUtils.inTransaction(em-> {
            if (!em.contains(city)) { // la ville n'est pas encore persistée, créée par l'utilisateur pour ce nouveau resto
                em.persist(city);
            }
            em.persist(restaurant);
        });

        return restaurant;
    }

    public void updateRestaurant(Restaurant restaurant, String newAddress, City newCity) {
        try{
            if(em.contains(restaurant)) {
                JpaUtils.inTransaction(em -> {
                    em.detach(restaurant);
                    restaurant.setAddress(new Localisation(newAddress, newCity));
                    em.merge(restaurant);
                });
            } else {
                throw new Exception("Restaurant " + restaurant.getName() + " n'existe pas dans la DB");
            }
        } catch (Exception e) {
            logger.error("Error while updating restaurant: " + e.getMessage());
        }
    }

    public void updateRestaurant(Restaurant restaurant, String newName, String newDescription, String newWebsite, RestaurantType newType) {
        try{
            if(em.contains(restaurant)) {
                JpaUtils.inTransaction(em -> {
                    em.detach(restaurant);
                    restaurant.setName(newName);
                    restaurant.setDescription(newDescription);
                    restaurant.setWebsite(newWebsite);
                    restaurant.setType(newType);
                    em.merge(restaurant);
                });
            } else {
                throw new Exception("Restaurant " + restaurant.getName() + " n'existe pas dans la DB");
            }
        } catch (Exception e) {
            logger.error("Error while updating restaurant: " + e.getMessage());
        }
    }



    /*
    * Efface un restaurant de la DB avec tous ses objets dépendants (evaluations) ainsi que la ville si elle
    * n'est pas utilisée par un autre restaurant
    * On efface les evaluations et les notes grâce au cascade delete défini dans le mapping des objets
     */
    public boolean deleteRestaurant(Restaurant restaurant){
        try {
           // on garde une ref sur la ville pour vérifier si un autre resto s'y trouve après effacement
           // le type osef on le laisse car il n'y a pas de méthode pour en ajouter dans l'interface
           // TODO supprimer toutes les méthodes qui ne sont plus appelées depuis ici - cleanup à la fin
           // TODO il y a encore un bug, si on essaie d'effacer un resto créé dans la meme session ça plante.
           JpaUtils.inTransaction(em -> {
              City city = restaurant.getAddress().getCity();
              city.getRestaurants().remove(restaurant);
              restaurant.getType().getRestaurants().remove(restaurant);
              em.remove(restaurant);
              if (city.getRestaurants().isEmpty()) {
                 em.remove(city);
              }
           });
           return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while deleting restaurant: " + e.getMessage());
            return false;
        }
    }

    public void shutdown() {
        em.close();
    }
}
