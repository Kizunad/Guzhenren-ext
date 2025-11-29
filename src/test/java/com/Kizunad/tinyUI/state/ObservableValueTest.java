package com.Kizunad.tinyUI.state;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ObservableValueTest {

    @Test
    void notifiesListenersInOrder() {
        final ObservableValue<String> value = new ObservableValue<>("a");
        final List<String> calls = new ArrayList<>();
        value.subscribe(v -> calls.add("first-" + v));
        value.subscribe(v -> calls.add("second-" + v));

        value.set("b");
        assertEquals(List.of("first-b", "second-b"), calls);
    }

    @Test
    void cancelPreventsFurtherNotifications() {
        final ObservableValue<Integer> value = new ObservableValue<>(1);
        final AtomicBoolean called = new AtomicBoolean(false);
        final Subscription sub = value.subscribe(v -> called.set(true));

        sub.cancel();
        value.set(2);
        assertTrue(!called.get(), "listener should not be called after cancel");
    }
}
