package ch.hearc.ig.guideresto.persistence;

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
        int deletedCount = em.createQuery("DELETE FROM CompleteEvaluation ce WHERE ce = :completeEvaluation")
                .setParameter("completeEvaluation", completeEvaluation)
                .executeUpdate();
        return deletedCount > 0;
    }
}
