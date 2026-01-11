
package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Set;
import java.util.stream.Collectors;

public class GradeMapper extends AbstractMapper<Grade> {
    public Set<Grade> findByEvaluation(EntityManager em, CompleteEvaluation ce) {
        TypedQuery<Grade> query = em.createNamedQuery("Grade.findByEvaluation", Grade.class);
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
        int deletedCount = em.createQuery("DELETE FROM Grade g WHERE g = :grade")
                .setParameter("grade", grade)
                .executeUpdate();
        return deletedCount > 0;
    }
}
