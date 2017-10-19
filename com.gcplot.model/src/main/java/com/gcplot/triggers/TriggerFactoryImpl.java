package com.gcplot.triggers;

import com.gcplot.Identifier;
import com.gcplot.triggers.binary.BinaryTrigger;
import com.gcplot.triggers.binary.State;

import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 9/7/17
 */
public class TriggerFactoryImpl implements TriggerFactory {

    @Override
    public BinaryTrigger createBinary(Identifier accountId, State initialState, TriggerType type, Map<String, String> properties) {
        BinaryTriggerImpl bt = new BinaryTriggerImpl();
        bt.setAccountId(accountId);
        bt.setState(initialState);
        bt.setProperties(properties);
        bt.setType(type);
        return bt;
    }
}
