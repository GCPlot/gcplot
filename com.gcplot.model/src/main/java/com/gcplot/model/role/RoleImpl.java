package com.gcplot.model.role;

import com.gcplot.Identifier;
import com.google.common.base.MoreObjects;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/14/16
 */
@Table(name = "Role", uniqueConstraints =
        @UniqueConstraint(columnNames={"title"}))
@SuppressWarnings("all")
public class RoleImpl implements Role {

    @Override
    public Identifier id() {
        if (identifier == null) {
            identifier = Identifier.fromStr(id.toString());
        }
        return identifier;
    }
    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
        this.id = identifier.toString();
    }

    @Override
    public String title() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public List<Restriction> restrictions() {
        return (List<Restriction>) restrictions;
    }
    public void setRestrictions(List<RestrictionImpl> restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Object getOId() {
        return id;
    }

    public RoleImpl() {
    }

    public RoleImpl(String title) {
        this(title, Collections.emptyList(), false);
    }

    public RoleImpl(String title, List<RestrictionImpl> restrictions) {
        this(title, restrictions, false);
    }

    public RoleImpl(String title, List<RestrictionImpl> restrictions, boolean isDefault) {
        setTitle(title);
        setRestrictions(restrictions);
        setDefault(isDefault);
    }

    @Id
    protected Object id;
    @Transient
    protected transient Identifier identifier;
    protected String title;
    @OneToMany(orphanRemoval = true, targetEntity = RestrictionImpl.class)
    protected List<? super RestrictionImpl> restrictions = new ArrayList<>();
    protected boolean isDefault;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoleImpl role = (RoleImpl) o;

        if (isDefault != role.isDefault) return false;
        if (title != null ? !title.equals(role.title) : role.title != null) return false;
        if (restrictions != null ? !restrictions.equals(role.restrictions) : role.restrictions != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (restrictions != null ? restrictions.hashCode() : 0);
        result = 31 * result + (isDefault ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("identifier", identifier)
                .add("title", title)
                .add("restrictions", restrictions)
                .add("isDefault", isDefault)
                .toString();
    }

    public static class RestrictionImpl implements Restriction {

        @Override
        public RestrictionType type() {
            return type;
        }
        public void setType(RestrictionType type) {
            this.type = type;
        }

        @Override
        public String action() {
            return action;
        }
        public void setAction(String action) {
            this.action = action;
        }

        @Override
        public boolean restricted() {
            return restricted;
        }
        public void setRestricted(boolean restricted) {
            this.restricted = restricted;
        }

        @Override
        public long amount() {
            return amount;
        }
        public void setAmount(long amount) {
            this.amount = amount;
        }

        @Override
        public Map<String, String> properties() {
            return properties;
        }
        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }

        public RestrictionImpl() {
        }

        public RestrictionImpl(RestrictionType type, boolean restricted, String action, long amount,
                               Map<String, String> properties) {
            this.type = type;
            this.restricted = restricted;
            this.action = action;
            this.amount = amount;
            this.properties = properties;
        }

        protected RestrictionType type;
        protected String action;
        protected boolean restricted;
        protected long amount;
        protected Map<String, String> properties;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RestrictionImpl that = (RestrictionImpl) o;

            if (restricted != that.restricted) return false;
            if (amount != that.amount) return false;
            if (type != that.type) return false;
            if (action != null ? !action.equals(that.action) : that.action != null) return false;
            if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (action != null ? action.hashCode() : 0);
            result = 31 * result + (restricted ? 1 : 0);
            result = 31 * result + (int) (amount ^ (amount >>> 32));
            result = 31 * result + (properties != null ? properties.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("type", type)
                    .add("action", action)
                    .add("restricted", restricted)
                    .add("amount", amount)
                    .add("properties", properties)
                    .toString();
        }
    }
}
