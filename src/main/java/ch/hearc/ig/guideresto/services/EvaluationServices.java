package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.*;
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
import java.util.Map;
import java.util.Set;

public class EvaluationServices {

    private static final Logger logger = LogManager.getLogger(EvaluationServices.class);
    private final EntityManager em ;

    //mappers
    private BasicEvaluationMapper beMapper ;
    private CompleteEvaluationMapper ceMapper ;
    private GradeMapper gMapper ;

    public EvaluationServices() {
        em = JpaUtils.getEntityManager();
        beMapper = new BasicEvaluationMapper();
        ceMapper = new CompleteEvaluationMapper();
        gMapper = new GradeMapper();
    }

    /**
     * Compte en DB le nombre d'évaluations basiques positives ou négatives en fonction du paramètre likeRestaurant
     *
     * @param resto    le restaurant pour lequel on compte les évaluations
     * @param likeRestaurant Veut-on le nombre d'évaluations positives ou négatives ?
     * @return Le nombre d'évaluations positives ou négatives trouvées
     */
    public Long countLikes(Restaurant resto, Boolean likeRestaurant) throws Exception {
        try {
            return beMapper.countForRestaurant(em, resto, likeRestaurant);
        }catch (Exception e) {
            logger.error("Error while counting likes : " + e.getMessage());
            throw new Exception("Erreur lors du comptage des likes, veuillez réessayer plus tard.");
        }
    }

    public Set<EvaluationCriteria> findAllEvaluationCriteria() throws Exception {
        try {
            String findAllEvalCriteria = "SELECT ec FROM EvaluationCriteria ec";
            TypedQuery<EvaluationCriteria> query = em.createQuery(findAllEvalCriteria, EvaluationCriteria.class);
            return new HashSet<EvaluationCriteria>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all evaluation criteria: " + e.getMessage());
            throw new Exception("Erreur lors de la récupération des évaluations, veuillez réessayer plus tard.");
        }
    }
    /**
     * Les trois méthodes ci-dessous servent à créer des notes et des evaluations. ce serait plus simple et propre de
     * simplement faire confiance à Hibernate et son système de cascade, mais on aime les mappers.
     */
    /**
     * Ajoute au restaurant passé en paramètre un like ou un dislike, en fonction du second paramètre.
     * L'IP locale de l'utilisateur est enregistrée. S'il s'agissait d'une application web, il serait préférable de récupérer l'adresse IP publique de l'utilisateur.
     *
     * @param restaurant Le restaurant qui est évalué
     * @param like       Est-ce un like ou un dislike ?
     */
    public void addBasicEvaluation(Restaurant restaurant, Boolean like) throws Exception {
        try {
            String tempIpAddress;
            try {
                tempIpAddress = Inet4Address.getLocalHost().toString(); // Permet de retrouver l'adresse IP locale de l'utilisateur.
            } catch (UnknownHostException ex) {
                logger.warn("Warning - Couldn't retreive host IP address");
                tempIpAddress = "Indisponible";
            }
            final String ipAddress = tempIpAddress;
            JpaUtils.inTransaction(em -> {
                Restaurant managedRestaurant = (Restaurant) em.getReference(Restaurant.class, restaurant.getId());
                BasicEvaluation eval = new BasicEvaluation(new Date(), managedRestaurant, like, ipAddress);
                em.persist(eval);
            });
        } catch (Exception e) {
            logger.error("Error while adding evaluation : " + e.getMessage());
            throw new Exception("Erreur lors de l'ajout de l'évaluation, veuillez réessayer plus tard.");
        }
    }

    /*
    * Ajoute et persiste une évaluation complète a un restaurant
    * parametre "grades" : fournir une Map avec le critère comme clé et la note comme valeur
     */
    public void addCompleteEvaluation(Restaurant restaurant, String comment, String username, Map<EvaluationCriteria, Integer> grades) throws Exception {
        try {
            JpaUtils.inTransaction(em-> {
                Restaurant managedRestaurant = (Restaurant) em.getReference(Restaurant.class, restaurant.getId());
                CompleteEvaluation eval = new CompleteEvaluation(new Date(), managedRestaurant, comment, username);
                em.persist(eval);
                eval.setGrades(new HashSet<>());

                for (EvaluationCriteria ec : grades.keySet()) {
                    Integer note = grades.get(ec);
                    Grade grade = new Grade(note, eval, ec);
                    em.persist(grade);
                    eval.getGrades().add(grade);
                }
                managedRestaurant.getEvaluations().add(eval);
            });
        } catch (Exception e) {
            logger.error("Error while adding complete evaluation: " + e.getMessage());
            throw new Exception("Erreur lors de l'ajout de l'évaluation, veuillez réessayer plus tard.");
        }
    }

    public void shutdown() {
        em.close();
    }

}
