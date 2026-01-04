package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Set;
import java.util.stream.Collectors;

public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {

    public Set<Evaluation> findByRestaurant(EntityManager em, Restaurant resto) {
        TypedQuery<CompleteEvaluation> query = em.createNamedQuery("CompleteEvaluation.findByRestaurant", CompleteEvaluation.class);
        query.setParameter("restaurant", resto);
        Set<Evaluation> completeEvaluations = query.getResultStream()
                .collect(Collectors.toSet());

        return completeEvaluations;
    }

    @Override
    public Set<CompleteEvaluation> findAll(EntityManager em) {
        String jpqlQuery = "SELECT ce FROM CompleteEvaluation ce";
        Set<CompleteEvaluation> completeEvaluations = em.createQuery(jpqlQuery, CompleteEvaluation.class)
                .getResultStream()
                .collect(Collectors.toSet());

        return completeEvaluations;
    }

    @Override
    public boolean delete(EntityManager em, CompleteEvaluation completeEvaluation) {
        try {
            em.remove(completeEvaluation);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteByRestaurant(EntityManager em, Restaurant resto) {
        try {
            String jpqlQuery = "DELETE FROM CompleteEvaluation ce WHERE restaurant.id = :restaurantId";
            em.createQuery(jpqlQuery)
                    .setParameter("restaurantId", resto.getId())
                    .executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
