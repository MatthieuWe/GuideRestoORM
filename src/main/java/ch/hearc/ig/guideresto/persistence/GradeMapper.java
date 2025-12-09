
package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Set;
import java.util.stream.Collectors;

public class GradeMapper extends AbstractMapper<Grade> {
    // on pourrait faire la meme avec les criteres d'évaluation mais c'est un peu inutile
    public Set<Grade> findByEvaluation(EntityManager em, CompleteEvaluation ce) {
        String jpqlQuery = "SELECT gra FROM Grade gra WHERE gra.evaluation = :evaluation";
        TypedQuery<Grade> query = em.createQuery(jpqlQuery, Grade.class);
        query.setParameter("evaluation", ce);
        Set<Grade> grades = query.getResultStream()
                .collect(Collectors.toSet());

        return grades;
    }

    @Override
    public Set<Grade> findAll(EntityManager em) {
        String jpqlQuery = "SELECT gra FROM Grade gra";
        Set<Grade> grades = em.createQuery(jpqlQuery, Grade.class)
                .getResultStream()
                .collect(Collectors.toSet());

        return grades;
    }

    @Override
    public boolean delete(EntityManager em, Grade grade) {
        em.remove(grade);
        return true;
    }

    @Override
    protected String getCountQuery() {
        return "SELECT Count(gra) FROM Grade gra";
    }
}
