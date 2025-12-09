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
        String jpqlQuery = "SELECT be FROM BasicEvaluation be WHERE be.restaurant = :restaurant";
        TypedQuery<BasicEvaluation> query = em.createQuery(jpqlQuery, BasicEvaluation.class);
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

    @Override
    public boolean delete(EntityManager em, BasicEvaluation basicEvaluation) {
        em.remove(basicEvaluation);
        return true;
    }

    @Override
    protected String getCountQuery() {
        return "SELECT Count(be) FROM BasicEvaluation be";
    }
}
