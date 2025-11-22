package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;

/**
 * @author cedric.baudet
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Evaluation implements IBusinessObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "seq_eval")
    @SequenceGenerator(name="seq_eval", sequenceName = "seq_evaluations",
            initialValue = 1, allocationSize = 1)
    @Column(name="numero", length=10)
    private Integer id;
    @Column(name="date_eval", nullable = false)
    private Date visitDate;
    @Transient
    private Restaurant restaurant;

    public Evaluation() {
        this(null, null, null);
    }

    public Evaluation(Integer id, Date visitDate, Restaurant restaurant) {
        this.id = id;
        this.visitDate = visitDate;
        this.restaurant = restaurant;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

}