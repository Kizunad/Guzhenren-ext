package com.Kizunad.tinyUI.state;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Bindings {

    private Bindings() {
    }

    public static <T> Subscription bind(final ObservableValue<T> source,
                                        final Consumer<T> consumer) {
        return bindWithInvalidate(source, consumer, null);
    }

    public static <T> Subscription bindWithInvalidate(final ObservableValue<T> source,
                                                      final Consumer<T> consumer,
                                                      final Runnable invalidator) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(consumer, "consumer");
        final Consumer<T> wrapped = value -> {
            consumer.accept(value);
            if (invalidator != null) {
                invalidator.run();
            }
        };
        wrapped.accept(source.get());
        final Subscription sub = source.subscribe(wrapped);
        return () -> {
            sub.cancel();
            if (invalidator != null) {
                invalidator.run();
            }
        };
    }

    public static <S, T> Subscription map(final ObservableValue<S> source,
                                          final Function<S, T> mapper,
                                          final Consumer<T> consumer) {
        Objects.requireNonNull(mapper, "mapper");
        return bind(source, value -> consumer.accept(mapper.apply(value)));
    }
}
