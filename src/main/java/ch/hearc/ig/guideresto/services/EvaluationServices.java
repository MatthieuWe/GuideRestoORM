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
    public Long countLikes(Restaurant resto, Boolean likeRestaurant) {
        return beMapper.countForRestaurant(em, resto, likeRestaurant);
    }

    public Set<EvaluationCriteria> findAllEvaluationCriteria() {
        try {
            String findAllEvalCriteria = "SELECT ec FROM EvaluationCriteria ec";
            TypedQuery<EvaluationCriteria> query = em.createQuery(findAllEvalCriteria, EvaluationCriteria.class);
            return new HashSet<EvaluationCriteria>(query.getResultList());
        } catch (Exception e) {
            logger.error("Error while fetching all evaluation criteria: " + e.getMessage());
            throw new RuntimeException("Error while fetching all evaluation criteria: " + e.getMessage());
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
    public void addBasicEvaluation(Restaurant restaurant, Boolean like) {
        String ipAddress;
        try {
            ipAddress = Inet4Address.getLocalHost().toString(); // Permet de retrouver l'adresse IP locale de l'utilisateur.
        } catch (UnknownHostException ex) {
            logger.error("Error - Couldn't retreive host IP address");
            ipAddress = "Indisponible";
        }
        BasicEvaluation eval = new BasicEvaluation(new Date(), restaurant, like, ipAddress);
        JpaUtils.inTransaction(em -> {
            em.persist(eval);
        });
    }

    public Boolean addCompleteEvaluation(Restaurant restaurant, String comment, String username, Map<EvaluationCriteria, Integer> grades) {
        try {
            JpaUtils.inTransaction(em-> {
                CompleteEvaluation eval = new CompleteEvaluation(new Date(), restaurant, comment, username);
                em.persist(eval);
                eval.setGrades(new HashSet<>());

                for (EvaluationCriteria ec : grades.keySet()) {
                    Integer note = grades.get(ec);
                    Grade grade = new Grade(note, eval, ec);
                    em.persist(grade);
                    eval.getGrades().add(grade);
                }
                restaurant.getEvaluations().add(eval);
            });
        } catch (Exception e) {
            logger.error("Error while adding complete evaluation: " + e.getMessage());
            return false;
        }
        return true;
    }

    /*
    * On ne gère pas les transactions dans cette méthode, elle est utile dans le cadre de l'effacement d'un resto
    * complet -> on gère plus haut, tout est effacé sinon rien
     */
    public void deleteByRestaurant(Restaurant restaurant) {
        beMapper.deleteByRestaurant(em, restaurant);
        for(Evaluation eval : restaurant.getEvaluations()) {
            if (eval instanceof CompleteEvaluation) {
                gMapper.deleteByEvaluation(em, (CompleteEvaluation) eval);
                // on pourrait mais c'est pas opti, je prefere une seule requete en bloc à la fin
                // ceMapper.delete(em, (CompleteEvaluation) eval);
            }
        }
        ceMapper.deleteByRestaurant(em, restaurant);

    }
    public void shutdown() {
        em.close();
    }

}
