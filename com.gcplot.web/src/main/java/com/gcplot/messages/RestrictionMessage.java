package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gcplot.model.role.Restriction;
import com.gcplot.model.role.RestrictionType;

import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/19/16
 */
public class RestrictionMessage {
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

    public RestrictionMessage(@JsonProperty("type") RestrictionType type,
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

    public static RestrictionMessage from(Restriction restriction) {
        return new RestrictionMessage(restriction.type(), restriction.action(), restriction.restricted(),
                restriction.amount(), restriction.properties());
    }
}
