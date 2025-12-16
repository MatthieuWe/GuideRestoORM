package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Restaurant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EvaluationCriteriaMapper extends AbstractMapper<EvaluationCriteria> {

    // je ne vois actuellement pas de methode findByXXX qui puisse être utile ici -MW

    @Override
    public Set<EvaluationCriteria> findAll(EntityManager em) {
        String jpqlQuery = "SELECT ec FROM EvaluationCriteria ec";
        Set<EvaluationCriteria> evaluationCriterias = em.createQuery(jpqlQuery, EvaluationCriteria.class)
                .getResultStream()
                .collect(Collectors.toSet());

        return evaluationCriterias;
    }

    @Override
    public boolean delete(EntityManager em, EvaluationCriteria evaluationCriteria) {
        em.remove(evaluationCriteria);
        return true;
    }
}
