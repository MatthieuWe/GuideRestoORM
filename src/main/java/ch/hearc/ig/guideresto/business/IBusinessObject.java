package ch.hearc.ig.guideresto.business;

import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public interface IBusinessObject {
    Integer getId();
}
