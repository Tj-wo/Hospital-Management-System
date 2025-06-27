package org.pahappa.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * An abstract base class for all entity models to inherit from.
 * It provides common fields like id, creation/modification timestamps, and a soft-delete flag.
 * The @MappedSuperclass annotation ensures that these fields are mapped to the columns
 * of the subclass tables.
 */
@MappedSuperclass
public abstract class BaseModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", updatable = false)
    private Date dateCreated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_modified")
    private Date dateModified;

    /**
     * The soft-delete flag. When true, the record is considered deleted
     * and should not be retrieved in standard queries.
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    /**
     * This method is called by JPA/Hibernate before a new entity is saved (persisted).
     * It sets the initial creation and modification dates.
     */
    @PrePersist
    protected void onCreate() {
        this.dateCreated = new Date();
        this.dateModified = new Date();
    }

    /**
     * This method is called by JPA/Hibernate before an existing entity is updated.
     * It updates the modification date.
     */
    @PreUpdate
    protected void onUpdate() {
        this.dateModified = new Date();
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}