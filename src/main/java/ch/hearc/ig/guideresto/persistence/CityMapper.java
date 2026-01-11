package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Grade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Set;

public class CityMapper extends AbstractMapper<City> {

    @Override
    public Set<City> findAll(EntityManager em) {
        String findAllCity = "SELECT c FROM City c";
        TypedQuery<City> query = em.createQuery(findAllCity, City.class);
        return new HashSet<City>(query.getResultList());
    }

    public Set<City> findByName(EntityManager em, String partialName) {
        TypedQuery<City> cityQuery = em.createNamedQuery("City.findByName", City.class);
        cityQuery.setParameter("name", "%" + partialName.toLowerCase() + "%");
        return new HashSet<>(cityQuery.getResultList());
    }
    public City findByZipAndName(EntityManager em,String zip, String name) {
        /*
        **La Base de Données ne garantit pas l'unicité zip et du nom pour une ville, on le gère ici.
        * On prends également le premier de la liste pour rester alignés avec le code fournit par M. Baudet.
        * Voir aussi Application > searchCityByZipCode (env. l. 490-500)
         */
        TypedQuery<City> cityQuery = em.createNamedQuery("City.findByExactZipAndName", City.class);
        cityQuery.setParameter("zip", zip);
        cityQuery.setParameter("name", name);
        return cityQuery.getResultList().getFirst();
    }

    @Override
    public boolean delete(EntityManager em, City city) {
        int deletedCount = em.createQuery("DELETE FROM City cy WHERE cy = :city")
                .setParameter("city", city)
                .executeUpdate();
        return deletedCount > 0;
    }
}
