package com.gcplot.triggers;

import com.gcplot.Identifier;

import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/1/17
 */
public abstract class AbstractTrigger<T> implements Trigger<T> {
    @Id
    private Object id;
    @Transient
    private transient Identifier identifier;
    @Transient
    private transient Identifier accountIdentifier;
    @Transient
    private transient TriggerType triggerType;
    private String accountId;
    private int typeId;
    private long lastTimeTrigger;
    private Map<String, String> properties;
    protected Object state;
    protected Object previousState;

    public Identifier id() {
        if (identifier == null) {
            identifier = Identifier.fromStr(id.toString());
        }
        return identifier;
    }

    public Identifier accountId() {
        if (accountIdentifier == null) {
            accountIdentifier = Identifier.fromStr(accountId);
        }
        return accountIdentifier;
    }

    public TriggerType type() {
        if (triggerType == null) {
            triggerType = TriggerType.by(typeId);
        }
        return triggerType;
    }

    public long lastTimeTrigger() {
        return lastTimeTrigger;
    }

    public Map<String, String> properties() {
        return properties;
    }

    public void setAccountId(Identifier accountId) {
        this.accountId = accountId.toString();
        this.accountIdentifier = accountId;
    }

    public void setType(TriggerType type) {
        this.typeId = type.getId();
        this.triggerType = type;
    }

    public void setLastTimeTrigger(long lastTimeTrigger) {
        this.lastTimeTrigger = lastTimeTrigger;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setState(Object state) {
        this.state = state;
    }

    public void setPreviousState(Object previousState) {
        this.previousState = previousState;
    }
}
