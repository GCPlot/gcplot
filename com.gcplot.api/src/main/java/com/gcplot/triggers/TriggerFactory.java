package com.gcplot.triggers;

import com.gcplot.Identifier;
import com.gcplot.triggers.binary.BinaryTrigger;
import com.gcplot.triggers.binary.State;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 9/7/17
 */
public interface TriggerFactory {

    BinaryTrigger createBinary(Identifier accountId, State initialState);

}
