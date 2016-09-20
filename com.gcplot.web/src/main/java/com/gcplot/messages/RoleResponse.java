package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.role.Role;

import java.util.List;
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
    public List<RestrictionMessage> restrictions;

    public RoleResponse(@JsonProperty("id") String id,
                        @JsonProperty("title") String title,
                        @JsonProperty("default") boolean isDefault,
                        @JsonProperty("restrictions") List<RestrictionMessage> restrictions) {
        this.id = id;
        this.title = title;
        this.isDefault = isDefault;
        this.restrictions = restrictions;
    }

    public static RoleResponse from(Role role) {
        List<RestrictionMessage> restrictions = role.restrictions().stream()
                .map(RestrictionMessage::from).collect(Collectors.toList());
        return new RoleResponse(role.id().toString(), role.title(), role.isDefault(), restrictions);
    }

}
