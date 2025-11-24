package ch.hearc.ig.guideresto.business;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * @author cedric.baudet
 */
@Embeddable
public class Localisation {

    @Column(name = "adresse", length = 100)
    private String street;
    @ManyToOne
    @JoinColumn(name = "fk_vill", nullable = false)
    private City city;

    public Localisation() {
        this(null, null);
    }

    public Localisation(String street, City city) {
        this.street = street;
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
}