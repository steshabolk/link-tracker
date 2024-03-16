/*
 * This file is generated by jOOQ.
 */
package edu.java.model.jooq.tables.pojos;


import jakarta.validation.constraints.Size;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.time.OffsetDateTime;

import javax.annotation.processing.Generated;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.18.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Links implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Short linkType;
    private String url;
    private OffsetDateTime checkedAt;
    private Short status;

    public Links() {}

    public Links(Links value) {
        this.id = value.id;
        this.linkType = value.linkType;
        this.url = value.url;
        this.checkedAt = value.checkedAt;
        this.status = value.status;
    }

    @ConstructorProperties({ "id", "linkType", "url", "checkedAt", "status" })
    public Links(
        @Nullable Long id,
        @NotNull Short linkType,
        @NotNull String url,
        @Nullable OffsetDateTime checkedAt,
        @NotNull Short status
    ) {
        this.id = id;
        this.linkType = linkType;
        this.url = url;
        this.checkedAt = checkedAt;
        this.status = status;
    }

    /**
     * Getter for <code>LINKS.ID</code>.
     */
    @Nullable
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for <code>LINKS.ID</code>.
     */
    public void setId(@Nullable Long id) {
        this.id = id;
    }

    /**
     * Getter for <code>LINKS.LINK_TYPE</code>.
     */
    @jakarta.validation.constraints.NotNull
    @NotNull
    public Short getLinkType() {
        return this.linkType;
    }

    /**
     * Setter for <code>LINKS.LINK_TYPE</code>.
     */
    public void setLinkType(@NotNull Short linkType) {
        this.linkType = linkType;
    }

    /**
     * Getter for <code>LINKS.URL</code>.
     */
    @jakarta.validation.constraints.NotNull
    @Size(max = 255)
    @NotNull
    public String getUrl() {
        return this.url;
    }

    /**
     * Setter for <code>LINKS.URL</code>.
     */
    public void setUrl(@NotNull String url) {
        this.url = url;
    }

    /**
     * Getter for <code>LINKS.CHECKED_AT</code>.
     */
    @Nullable
    public OffsetDateTime getCheckedAt() {
        return this.checkedAt;
    }

    /**
     * Setter for <code>LINKS.CHECKED_AT</code>.
     */
    public void setCheckedAt(@Nullable OffsetDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    /**
     * Getter for <code>LINKS.STATUS</code>.
     */
    @jakarta.validation.constraints.NotNull
    @NotNull
    public Short getStatus() {
        return this.status;
    }

    /**
     * Setter for <code>LINKS.STATUS</code>.
     */
    public void setStatus(@NotNull Short status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Links other = (Links) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.linkType == null) {
            if (other.linkType != null)
                return false;
        }
        else if (!this.linkType.equals(other.linkType))
            return false;
        if (this.url == null) {
            if (other.url != null)
                return false;
        }
        else if (!this.url.equals(other.url))
            return false;
        if (this.checkedAt == null) {
            if (other.checkedAt != null)
                return false;
        }
        else if (!this.checkedAt.equals(other.checkedAt))
            return false;
        if (this.status == null) {
            if (other.status != null)
                return false;
        }
        else if (!this.status.equals(other.status))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.linkType == null) ? 0 : this.linkType.hashCode());
        result = prime * result + ((this.url == null) ? 0 : this.url.hashCode());
        result = prime * result + ((this.checkedAt == null) ? 0 : this.checkedAt.hashCode());
        result = prime * result + ((this.status == null) ? 0 : this.status.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Links (");

        sb.append(id);
        sb.append(", ").append(linkType);
        sb.append(", ").append(url);
        sb.append(", ").append(checkedAt);
        sb.append(", ").append(status);

        sb.append(")");
        return sb.toString();
    }
}
