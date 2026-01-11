package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Set;
import java.util.stream.Collectors;

public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {

    public Set<Evaluation> findByRestaurant(EntityManager em, Restaurant resto) {
        TypedQuery<BasicEvaluation> query = em.createNamedQuery("BasicEvaluation.findByRestaurant", BasicEvaluation.class);
        query.setParameter("restaurant", resto);
        Set<Evaluation> basicEvaluations = query.getResultStream()
                .collect(Collectors.toSet());
        return basicEvaluations;
    }

    @Override
    public Set<BasicEvaluation> findAll(EntityManager em) {
        String jpqlQuery = "SELECT be FROM BasicEvaluation be";
        Set<BasicEvaluation> basicEvaluations = em.createQuery(jpqlQuery, BasicEvaluation.class)
                .getResultStream()
                .collect(Collectors.toSet());
        return basicEvaluations;
    }

    public Long countForRestaurant(EntityManager em, Restaurant resto, Boolean like) {
        return (Long) em.createNamedQuery("BasicEvaluation.countLikes", Long.class)
            .setParameter("restaurant", resto)
            .setParameter("like", like)
            .getSingleResult();
    }

    @Override
    public boolean delete(EntityManager em, BasicEvaluation basicEvaluation) {
        int deletedCount = em.createQuery("DELETE FROM BasicEvaluation be WHERE be = :basicEvaluation")
                .setParameter("basicEvaluation", basicEvaluation)
                .executeUpdate();
        return deletedCount > 0;
    }

}
