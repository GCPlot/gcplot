package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.role.Restriction;
import com.gcplot.model.role.RestrictionType;
import com.gcplot.model.role.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/18/16
 */
public class RoleResponse {
    @JsonProperty("id")
    public String id;
    @JsonProperty("title")
    public String title;
    @JsonProperty("default")
    public boolean isDefault;
    @JsonProperty("restrictions")
    public List<RestrictionResponse> restrictions;

    public RoleResponse(@JsonProperty("id") String id,
                        @JsonProperty("title") String title,
                        @JsonProperty("default") boolean isDefault,
                        @JsonProperty("restrictions") List<RestrictionResponse> restrictions) {
        this.id = id;
        this.title = title;
        this.isDefault = isDefault;
        this.restrictions = restrictions;
    }

    public static RoleResponse from(Role role) {
        List<RestrictionResponse> restrictions = role.restrictions().stream()
                .map(RestrictionResponse::from).collect(Collectors.toList());
        return new RoleResponse(role.id().toString(), role.title(), role.isDefault(), restrictions);
    }

    public static class RestrictionResponse {
        @JsonProperty("type")
        public RestrictionType type;
        @JsonProperty("action")
        public String action;
        @JsonProperty("restricted")
        public boolean restricted;
        @JsonProperty("amount")
        public long amount;
        @JsonProperty("properties")
        public Map<String, String> properties;

        public RestrictionResponse(@JsonProperty("type") RestrictionType type,
                                   @JsonProperty("action") String action,
                                   @JsonProperty("restricted") boolean restricted,
                                   @JsonProperty("amount") long amount,
                                   @JsonProperty("properties") Map<String, String> properties) {
            this.type = type;
            this.action = action;
            this.restricted = restricted;
            this.amount = amount;
            this.properties = properties;
        }

        public static RestrictionResponse from(Restriction restriction) {
            return new RestrictionResponse(restriction.type(), restriction.action(), restriction.restricted(),
                    restriction.amount(), restriction.properties());
        }
    }

}
