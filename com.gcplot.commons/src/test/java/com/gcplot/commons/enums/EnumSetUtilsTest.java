package com.gcplot.commons.enums;

import com.gcplot.utils.enums.EnumSetUtils;
import com.gcplot.utils.enums.TypedEnum;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.EnumSet;

public class EnumSetUtilsTest {

    @Test
    public void test() {
        EnumSet<TestEnum> es = EnumSet.of(TestEnum.ONE, TestEnum.TWO);
        long o = EnumSetUtils.encode(es);
        es = EnumSetUtils.decode(o, TestEnum.class);

        Assert.assertTrue(es.contains(TestEnum.ONE));
        Assert.assertTrue(es.contains(TestEnum.TWO));
        Assert.assertFalse(es.contains(TestEnum.THREE));
    }

    public enum TestEnum implements TypedEnum {
        ONE(1), TWO(2), THREE(3);

        private int type;
        private static Int2ObjectMap<Enum> types = new Int2ObjectOpenHashMap<>();
        @Override
        public int type() {
            return type;
        }

        public static Int2ObjectMap<Enum> types() {
            return types;
        }

        TestEnum(int type) {
            this.type = type;
        }

        static {
            for (TestEnum te : values()) {
                types.put(te.type, te);
            }
        }
    }

}
