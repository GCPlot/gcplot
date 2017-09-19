package com.gcplot.triggers;

import com.gcplot.Identifier;
import com.gcplot.triggers.binary.BinaryTrigger;
import com.gcplot.triggers.binary.State;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 9/7/17
 */
public interface TriggerFactory {

    BinaryTrigger createBinary(Identifier accountId, State initialState, Map<String, String> properties);

    default BinaryTrigger createBinary(Identifier accountId, State initialState) {
        return createBinary(accountId, initialState, Collections.emptyMap());
    }

}
