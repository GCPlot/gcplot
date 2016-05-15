package com.gcplot.commons.enums;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.EnumSet;

public class EnumSetUtilsTest {

    @Test
    public void test() {
        EnumSet<TestEnum> es = EnumSet.of(TestEnum.ONE, TestEnum.TWO);
        int o = EnumSetUtils.encode(es);
        es = EnumSetUtils.decode(o, TestEnum.class);

        Assert.assertTrue(es.contains(TestEnum.ONE));
        Assert.assertTrue(es.contains(TestEnum.TWO));
        Assert.assertFalse(es.contains(TestEnum.THREE));
    }

    public enum TestEnum implements TypedEnum {
        ONE(1), TWO(2), THREE(3);

        private int type;
        private static TIntObjectMap<Enum> types = new TIntObjectHashMap<>();
        @Override
        public int type() {
            return type;
        }

        public static TIntObjectMap<Enum> types() {
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
