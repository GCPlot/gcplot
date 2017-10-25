package com.gcplot.model.config;

import java.util.Map;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/3/17
 */
public interface Configuration<T extends ConfigProperty> {

    long asLong(T cp);

    double asDouble(T cp);

    boolean asBoolean(T cp);

    String asString(T cp);

    static <T extends ConfigProperty> Configuration<T> create(Map<String, String> configs) {
        return new Configuration<T>() {
            @Override
            public long asLong(T cp) {
                String v = configs.get(cp.getId());
                return (Long) (v == null ? cp.getDefaultValue() : Long.parseLong(v));
            }

            @Override
            public double asDouble(T cp) {
                String v = configs.get(cp.getId());
                return (Double) (v == null ? cp.getDefaultValue() : Double.parseDouble(v));
            }

            @Override
            public boolean asBoolean(T cp) {
                String v = configs.get(cp.getId());
                return (Boolean) (v == null ? cp.getDefaultValue() : Boolean.parseBoolean(v));
            }

            @Override
            public String asString(T cp) {
                String s = configs.get(cp.getId());
                return s == null ? (String) cp.getDefaultValue() : s;
            }
        };
    }

}
