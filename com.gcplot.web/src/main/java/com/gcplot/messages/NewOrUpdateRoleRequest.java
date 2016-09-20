package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/19/16
 */
public class NewOrUpdateRoleRequest {
    @JsonProperty("id")
    public String id;
    @JsonProperty(value = "title", required = true)
    public String title;
    @JsonProperty("default")
    public boolean isDefault;
    @JsonProperty(value = "restrictions", required = true)
    public List<RestrictionMessage> restrictions;

    public NewOrUpdateRoleRequest(@JsonProperty("id") String id,
                                  @JsonProperty(value = "title", required = true) String title,
                                  @JsonProperty("default") boolean isDefault,
                                  @JsonProperty(value = "restrictions", required = true) List<RestrictionMessage> restrictions) {
        this.id = id;
        this.title = title;
        this.isDefault = isDefault;
        this.restrictions = restrictions;
    }
}
