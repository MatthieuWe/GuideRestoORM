package ch.hearc.ig.guideresto.services;


import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.BasicEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.CityMapper;
import ch.hearc.ig.guideresto.persistence.RestaurantMapper;
import ch.hearc.ig.guideresto.persistence.RestaurantTypeMapper;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.*;
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

    private static RestaurantServices instance;

    //mappers
    private RestaurantMapper restaurantMapper ;
    private CityMapper cityMapper ;
    private RestaurantTypeMapper typeMapper ;

    private RestaurantServices() {
        em = JpaUtils.getEntityManager();
        restaurantMapper = new RestaurantMapper();
        cityMapper = new CityMapper();
        typeMapper = new RestaurantTypeMapper();
    }

    public static RestaurantServices getInstance() {
        if (instance == null) {
            instance = new RestaurantServices();
        }
        return instance;
    }

    public Set<Restaurant> findAllRestaurant() throws Exception {
        try {
            return restaurantMapper.findAll(em);
        } catch (Exception e) {
            logger.error("Error while fetching all restaurants: " + e.getMessage());
            throw new Exception("Erreur lors de la récupération des restaurants, veuillez réessayer plus tard.");
        }
    }

    public Set<RestaurantType> findAllRestaurantType() throws Exception {
        try {
            return typeMapper.findAll(em);
        } catch (Exception e) {
            logger.error("Error while fetching all restaurant types: " + e.getMessage());
            throw new Exception("Erreur lors de la récupération des types, veuillez réessayer plus tard.");
        }
    }

    public Set<City> findAllCities() throws Exception {
        try {
           return cityMapper.findAll(em);
        } catch (Exception e) {
            logger.error("Error while fetching all cities: " + e.getMessage());
            throw new Exception("Erreur lors de la récupération des villes, veuillez réessayer plus tard.");
        }
    }

    public Set<Restaurant> searchByName(String search) throws Exception {
        try {
            return restaurantMapper.findByName(em, search);
        } catch (Exception e) {
            logger.error("Error while searching restaurant: " + e.getMessage());
            throw new Exception("Erreur lors de la recherche de restaurant, veuillez réessayer plus tard.");
        }
    }
    /*
    Cette méthode recherche toutes les villes contenant la chaine fournie (nom de ville n'est pas unique en DB
    et le programme accepte une partie du nom) puis retourne tous les restos de chaque ville correspondante à la recherche
    Note:
    Solution A - c'est une opportunité d'utiliser notre cityMapper MAIS
    Solution B -  c'est plus efficace d'adapter notre méthode findByCity Dans le restaurantMapper pour qu'elle fasse
    tout ça directement en JPQL avec une jointure. Boucler sur un resultset pour refaire des select, c'est pas beau.
     */
    public Set<Restaurant> searchByCity(String search) throws Exception {
        try {
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
        } catch (Exception e) {
            logger.error("Error while searching restaurant: " + e.getMessage());
            throw new Exception("Erreur lors de la recherche de restaurant, veuillez réessayer plus tard.");
        }
    }
    public Set<Restaurant> searchByType(RestaurantType type) throws Exception{
        try{
            return restaurantMapper.findByType(em, type);
        } catch (Exception e) {
            logger.error("Error while searching restaurant: " + e.getMessage());
            throw new Exception("Erreur lors de la recherche de restaurant, veuillez réessayer plus tard.");
        }
    }

    /*
    * Cette méthode crée une nouvelle ville en mémoire mais ne la persiste pas !
    * Elle sera persisté dans une seule et même transaction lors de la création du restaurant,
    * Si cette transaction échoue, on n'a pas besoin de cette nouvelle ville dans la DB
     */
    public City createCity(String zipCode, String cityName) throws Exception {
        try {
            // check si la ville existe deja et la retourne à la place - peu probable
            return cityMapper.findByZipAndName(em, zipCode, cityName);
        }catch (NoResultException nre){
            // si on n'a rien trouvé - le cas le plus courant
            return new City(zipCode, cityName);
        }catch (Exception e){
            logger.error("Error while creating city: " + e.getMessage());
            throw new Exception("Erreur lors de la creation de la ville, veuillez réessayer plus tard.");
        }
    }


    public Restaurant createRestaurant(String name, String description, String website, String street, City city, RestaurantType restaurantType) throws Exception {
        Restaurant restaurant = new Restaurant();
        try {
            JpaUtils.inTransaction(em -> {
            restaurant.setName(name);
            restaurant.setDescription(description);
            restaurant.setWebsite(website);

            // on gère la bidirectionalité du type en vérifiant qu'il existe
            RestaurantType managedType = em.find(RestaurantType.class, restaurantType.getId());
            if (managedType != null) {
                restaurant.setType(managedType);
                managedType.getRestaurants().add(restaurant);
            } else {
                throw new EntityNotFoundException("Erreur lors de la creation du restaurant: le type sélectionné n'existe pas en base de données.");
            }

            // on gère la persistance de la ville si nouvelle ou non
            City managedCity;
            if (city.getId() == null) {
                // c'est une nouvelle ville créée par l'utilisateur pour ce nouveau resto
                em.persist(city);
                managedCity = city;
            } else {
                // Si d'un coup elle était detached. oui, "si d'un coup" ça se dit chez nous.
                // Note: merge ne modifie pas l'objet passé en param, donc on a besoin de cette variable managedCity
                managedCity = em.merge(city);
            }
            // on gère la bidirectionalité de la ville
            restaurant.setAddress(new Localisation(street, managedCity));
            managedCity.getRestaurants().add(restaurant);

            // enfin on persiste le resto
            // les eval osef: il n'en a logiquement pas encore car on vient de le créer
            em.persist(restaurant);
            });
            return restaurant;
        } catch (EntityNotFoundException enf) {
            logger.error("Error creating restaurant: " + enf.getMessage());
            throw enf;
        } catch (Exception e) {
            logger.error("Error creating restaurant: " + e.getMessage());
            throw new Exception("Erreur lors de la creation du restaurant, veuillez réessayer plus tard.");
        }
    }

    public void updateRestaurant(Restaurant restaurant, String newAddress, City newCity) throws Exception{
        try{
            JpaUtils.inTransaction(em -> {
                Restaurant managedRestaurant = (Restaurant) em.find(Restaurant.class, restaurant.getId());

                City oldCity = managedRestaurant.getAddress().getCity();
                City managedCity;
                // 3 possibilités
                if (oldCity.getId().equals(newCity.getId())) { // 1) c'est la meme ville
                    managedCity = oldCity;
                } else { // C'en est une autre, on enlève le resto le l'ancienne ville
                    oldCity.getRestaurants().remove(managedRestaurant);
                    if (oldCity.getRestaurants().isEmpty()) {
                        em.remove(oldCity);
                    }
                    if (newCity.getId() == null) { // 2) c'est une nouvelle ville, on la persiste
                        em.persist(newCity);
                        managedCity = newCity;
                    } else { // 3) c'est une ville existante, on s'assure qu'elle est managed
                        managedCity = em.merge(newCity);
                    }
                    // on gère la bidirectionalité de la nouvelle ville
                    managedCity.getRestaurants().add(managedRestaurant);
                }

                // Localisation est un embedded > doit ête immutable > on fait un new dans tous les cas
                managedRestaurant.setAddress(new Localisation(newAddress, managedCity));
            });
        } catch (OptimisticLockException e) {
            logger.error("Optimistic lock error while updating restaurant: " + e.getMessage());
            throw new Exception("Le restaurant a été modifié par un autre utilisateur. Veuillez recharger les données et réessayer.");
        } catch (Exception e) {
            logger.error("Error while updating restaurant: " + e.getMessage());
            throw new Exception("Erreur lors de la mise à jour du restaurant, veuillez réessayer plus tard.");
        }
    }

    public void updateRestaurant(Restaurant restaurant, String newName, String newDescription, String newWebsite, RestaurantType newType) throws Exception {
        try {
            JpaUtils.inTransaction(em -> {
                Restaurant managedRestaurant = (Restaurant) em.find(Restaurant.class, restaurant.getId());
                managedRestaurant.setName(newName);
                managedRestaurant.setDescription(newDescription);
                managedRestaurant.setWebsite(newWebsite);

                // on gère la bidirectionalité du type en vérifiant qu'il existe
                RestaurantType oldType = managedRestaurant.getType();
                RestaurantType managedType = em.find(RestaurantType.class, newType.getId());
                if (managedType == null) {
                    throw new EntityNotFoundException("Erreur lors de la creation du restaurant: le type sélectionné n'existe pas en base de données.");
                }else if(!oldType.getId().equals(managedType.getId())) { // c'est un nouveau type, sinon on ne change rien (pas de else)
                    oldType.getRestaurants().remove(managedRestaurant);
                    managedRestaurant.setType(managedType);
                    managedType.getRestaurants().add(restaurant);
                }
            });
        } catch (OptimisticLockException e) {
            logger.error("Optimistic lock error while updating restaurant: " + e.getMessage());
            throw new Exception("Le restaurant a été modifié par un autre utilisateur. Veuillez recharger les données et réessayer.");
        }catch (Exception e) {
            logger.error("Error while updating restaurant: " + e.getMessage());
            throw new Exception("Erreur lors de la mise à jour du restaurant, veuillez réessayer plus tard.");
        }
    }

    /*
    * Efface un restaurant de la DB avec tous ses objets dépendants (evaluations) ainsi que la ville si elle
    * n'est pas utilisée par un autre restaurant
    * On efface les evaluations et les notes grâce au cascade delete défini dans le mapping des objets
     */
    public boolean deleteRestaurant(Restaurant restaurant) throws Exception {
        try {
           // on garde une ref sur la ville pour vérifier si un autre resto s'y trouve après effacement
           // le type osef on le laisse car il n'y a pas de méthode pour en ajouter dans l'interface
           // TODO supprimer toutes les méthodes qui ne sont plus appelées depuis ici - cleanup à la fin
           JpaUtils.inTransaction(em -> {
               Restaurant managedRestaurant = (Restaurant) em.find(Restaurant.class, restaurant.getId());
               City city = managedRestaurant.getAddress().getCity();
               city.getRestaurants().remove(managedRestaurant);
               managedRestaurant.getType().getRestaurants().remove(managedRestaurant);
               em.remove(managedRestaurant);
               if (city.getRestaurants().isEmpty()) {
                  em.remove(city);
               }
           });
           return true;
        } catch (OptimisticLockException e) {
            logger.error("Optimistic lock error while updating restaurant: " + e.getMessage());
            throw new Exception("Le restaurant a été modifié par un autre utilisateur. Veuillez réessayer.");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while deleting restaurant: " + e.getMessage());
            throw new Exception("Erreur lors de l'effacement du restaurant, veuillez réessayer plus tard.");
        }
    }

    public void shutdown() {
        em.close();
    }
}
