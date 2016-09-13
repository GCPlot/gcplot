package com.gcplot.commons;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Not thread safe!
 *
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/12/16
 */
public class LazyVal<T> {
    private static final Object OBTAINED = new Object();
    private T value;
    private Supplier<T> supplier;
    private Supplier<Optional<T>> optSupplier;

    private LazyVal(Supplier<T> supplier, Supplier<Optional<T>> optSupplier) {
        this.supplier = supplier;
        this.optSupplier = optSupplier;
    }

    public T get() {
        if (value == null && value != OBTAINED) {
            if (supplier != null) {
                value = supplier.get();
            } else if (optSupplier != null) {
                value = optSupplier.get().orElse(null);
            }
            if (value == null) {
                value = (T) OBTAINED;
            }
        }
        return value == OBTAINED ? null : value;
    }

    public static <T> LazyVal<T> of(Supplier<T> supplier) {
        return new LazyVal<>(supplier, null);
    }

    public static <T> LazyVal<T> ofOpt(Supplier<Optional<T>> optSupplier) {
        return new LazyVal<>(null, optSupplier);
    }
}
