package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;

/**
 * @author cedric.baudet
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
/*
NOTES sur l'héritage dans ce projet:
Nous avons choisi TABLE_PER_CLASS == Concrete Table Inheritance, soit une table par classe concrète
et pas de table pour les classes abstraites, car c'est exactement ce qui correspond au modèle de données.

-> il n'existe pas de table qui représente "Evaluation", donc on n'est pas en mode "Class Table Inheritance"
-> les tables correspondantes aux classes concrètes "likes" et "commentaires" existent et contiennent chacune
    les attributs partagés, définis ci-dessous (date_eval, fk_rest)
    -> attributs communs à double dans la DB
    -> C'est bien la définition de "Concrete Table Inheritance"
-> ce sont deux tables distinctes, donc on n'est heureusement pas non plus en "Single Table Inheritance"
 */
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
    @ManyToOne
    @JoinColumn(name = "fk_rest", nullable = false)
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