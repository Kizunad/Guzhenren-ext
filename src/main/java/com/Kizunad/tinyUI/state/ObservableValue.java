package com.Kizunad.tinyUI.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 轻量可观察值，支持多监听与取消。
 */
public final class ObservableValue<T> {

    private final List<Consumer<T>> listeners = new ArrayList<>();
    private T value;

    public ObservableValue(final T initialValue) {
        this.value = initialValue;
    }

    public T get() {
        return value;
    }

    public void set(final T newValue) {
        if (Objects.equals(value, newValue)) {
            return;
        }
        value = newValue;
        notifyListeners(newValue);
    }

    public Subscription subscribe(final Consumer<T> listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    private void notifyListeners(final T newValue) {
        final List<Consumer<T>> snapshot = new ArrayList<>(listeners);
        for (final Consumer<T> listener : snapshot) {
            listener.accept(newValue);
        }
    }
}
