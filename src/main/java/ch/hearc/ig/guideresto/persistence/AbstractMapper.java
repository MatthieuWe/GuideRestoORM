package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMapper<T extends IBusinessObject> {

    protected static final Logger logger = LogManager.getLogger();
    protected Map<Integer, IBusinessObject> cache = new HashMap<>();

    public abstract T findById(int id, EntityManager em);
    public abstract Set<T> findAll(EntityManager em);
    //public abstract T create(T object);
    //public abstract boolean update(T object);
    //public abstract boolean delete(T object);
    //public abstract boolean deleteById(int id);

    //protected abstract String getSequenceQuery();
    //protected abstract String getExistsQuery();
    //protected abstract String getCountQuery();

    /**
     * Vérifie si un objet avec l'ID donné existe.
     * @param id the ID to check
     * @return true si l'objet existe, false sinon
     */
    public boolean exists(int id) {
        //TODO hihihi
        return false;
    }

    /**
     * Compte le nombre d'objets en base de données.
     * @return
     */
    public int count() {
        Connection connection = ConnectionUtils.getConnection();
//TO DO ?
        return 0;
    }

    /**
     * Obtient la valeur de la séquence actuelle en base de données
     * @return Le nombre de villes
     * @En cas d'erreur SQL
     */
    protected Integer getSequenceValue() {
//TO DO ???
            return 0;

    }

    /**
     * Vérifie si le cache est actuellement vide
     * @return true si le cache ne contient aucun objet, false sinon
     */
    protected boolean isCacheEmpty() {
        return this.cache.isEmpty();
    }

    /**
     * Vide le cache
     */
    protected void resetCache() {
        this.cache.clear();
    }

    /**
     * Ajoute un objet au cache
     * @param objet l'objet à ajouter
     */
    protected void addToCache(T objet) {
        int id = objet.getId();
        this.cache.put(id, objet);
        /* Selon le cours:
        / Notez que la présence en cache est vérifiée 2 fois (findById et addToCache)
        / – On s’assure ainsi de ne pas créer de doublon en mémoire; mieux vaut prévenir que guérir
        / -> non je suis pas d'accord. Une map ne peut pas avoir de doublon de clé K, un put remplace la V existante
         */
    }

    /**
     * Retire un objet du cache
     * @param id l'ID de l'objet à retirer du cache
     */
    protected void removeFromCache(int id) {
        this.cache.remove(id);
    }


    // Méthodes pour charger un restaurant sont utilisées par plusieurs mappers: eager load depuis les évaluations
    protected Restaurant loadRestaurant(ResultSet rs) throws SQLException {
        City city = new City(rs.getInt("num_ville"),
                rs.getString("code_postal"),
                rs.getString("nom_ville"));
        return this.loadRestaurant(rs, city);
    }

    protected Restaurant loadRestaurant(ResultSet rs, City city) throws SQLException {
        RestaurantType type = new RestaurantType(rs.getInt("num_type"),
                rs.getString("libelle"),
                rs.getString("desc_type"));
        return this.loadRestaurant(rs, city, type);
    }
    protected Restaurant loadRestaurant(ResultSet rs, RestaurantType type) throws SQLException {
        City city = new City(rs.getInt("num_ville"),
                rs.getString("code_postal"),
                rs.getString("nom_ville"));
        return this.loadRestaurant(rs, city, type);
    }
    protected Restaurant loadRestaurant(ResultSet rs, City city, RestaurantType type) throws SQLException {
        Localisation address = new Localisation(rs.getString("adresse"), city);
        Restaurant resto = new Restaurant(
                rs.getInt("num_resto"),
                rs.getString("nom"),
                rs.getString("desc_resto"),
                rs.getString("site_web"),
                address,
                type);
        return resto;
    }

}
