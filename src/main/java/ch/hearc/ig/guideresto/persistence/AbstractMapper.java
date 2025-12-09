package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public abstract class AbstractMapper<T extends IBusinessObject> {

    protected static final Logger logger = LogManager.getLogger();
    public abstract Set<T> findAll(EntityManager em);
    public abstract boolean delete(EntityManager em, T object);

    protected abstract String getCountQuery();

    /**
     * Compte le nombre d'objets en base de données.
     * @return
     */
    public int count() {
        Connection connection = ConnectionUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement(getCountQuery());
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
            return 0;
        }
    }
}
