package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */
@Entity
@Table(name="RESTAURANTS")
public class Restaurant implements IBusinessObject {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE,
        generator="SEQ_RESTAURANTS")
    @SequenceGenerator(name="SEQ_RESTAURANTS", sequenceName="SEQ_RESTAURANTS",
        initialValue=1, allocationSize=1)
    @Column(name="numero", length=10)
    private Integer id;
    @Column(name="nom", length=100)
    private String name;

    @Lob //je crois que c'est ça pour Lob, en ttout cas c'est ce que me dit stackoverflow
    @Column(name="description", length=500)
    private String description;
    @Column(name="site_web", length=100)
    private String website;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Evaluation> evaluations;


    @Embedded
    private Localisation address;

    @ManyToOne
    @JoinColumn(name = "fk_type")
    private RestaurantType type;

    public Restaurant() {
        this(null, null, null, null, null, null);
    }


    public Restaurant(Integer id, String name, String description, String website, Localisation address, RestaurantType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.evaluations = new HashSet();
        this.address = address;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Set<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(Set<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public Localisation getAddress() {
        return address;
    }

    public void setAddress(Localisation address) {
        this.address = address;
    }

    public RestaurantType getType() {
        return type;
    }

    public void setType(RestaurantType type) {
        this.type = type;
    }

    public boolean hasEvaluations() {
        return CollectionUtils.isNotEmpty(evaluations);
    }
}