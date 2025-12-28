package ch.hearc.ig.guideresto.services;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.*;
import ch.hearc.ig.guideresto.persistence.jpa.JpaUtils;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
}
