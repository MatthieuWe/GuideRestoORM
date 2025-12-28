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
import java.util.Set;

public class EvaluationServices {

    private static final Logger logger = LogManager.getLogger(EvaluationServices.class);
    private final EntityManager em ;

    //mappers
    private BasicEvaluationMapper beMapper ;
    private CompleteEvaluationMapper ceMapper ;

    public EvaluationServices() {
        em = JpaUtils.getEntityManager();
        beMapper = new BasicEvaluationMapper();
        ceMapper = new CompleteEvaluationMapper();
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
    public void shutdown() {
        em.close();
    }

}
