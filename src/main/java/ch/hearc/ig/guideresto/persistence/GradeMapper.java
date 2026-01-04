
package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Set;
import java.util.stream.Collectors;

public class GradeMapper extends AbstractMapper<Grade> {
    // on pourrait faire la meme avec les criteres d'évaluation mais c'est un peu inutile
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
        try {
            em.remove(grade);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean deleteByEvaluation(EntityManager em, CompleteEvaluation eval) {
        try {
            String jpqlQuery = "DELETE FROM Grade gr WHERE evaluation.id = :evaluationId";
            em.createQuery(jpqlQuery)
                    .setParameter("evaluationId", eval.getId())
                    .executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
