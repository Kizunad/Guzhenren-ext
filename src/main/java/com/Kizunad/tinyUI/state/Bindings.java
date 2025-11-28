package com.Kizunad.tinyUI.state;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Bindings {

    private Bindings() {
    }

    public static <T> Subscription bind(final ObservableValue<T> source,
                                        final Consumer<T> consumer) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(consumer, "consumer");
        consumer.accept(source.get());
        final Subscription sub = source.subscribe(consumer);
        return sub::cancel;
    }

    public static <S, T> Subscription map(final ObservableValue<S> source,
                                          final Function<S, T> mapper,
                                          final Consumer<T> consumer) {
        Objects.requireNonNull(mapper, "mapper");
        return bind(source, value -> consumer.accept(mapper.apply(value)));
    }
}
