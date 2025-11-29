package com.Kizunad.tinyUI.state;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BindingsTest {

    private static final int TEST_VALUE = 3;

    @Test
    void bindCallsConsumerImmediatelyAndOnUpdate() {
        final ObservableValue<String> value = new ObservableValue<>("a");
        final AtomicReference<String> last = new AtomicReference<>();
        final Subscription sub = Bindings.bind(value, last::set);

        assertEquals("a", last.get());
        value.set("b");
        assertEquals("b", last.get());

        sub.cancel();
        value.set("c");
        assertEquals("b", last.get(), "consumer should not be called after cancel");
    }

    @Test
    void bindWithInvalidateTriggersInvalidator() {
        final ObservableValue<Integer> value = new ObservableValue<>(1);
        final AtomicBoolean invalidated = new AtomicBoolean(false);
        final AtomicReference<Integer> last = new AtomicReference<>();

        final Subscription sub = Bindings.bindWithInvalidate(value, last::set,
                () -> invalidated.set(true));

        assertTrue(invalidated.get(), "invalidator should run initially");
        assertEquals(Integer.valueOf(1), last.get());
        invalidated.set(false);

        value.set(2);
        assertTrue(invalidated.get(), "invalidator should run on change");
        assertEquals(Integer.valueOf(2), last.get());

        sub.cancel();
        assertTrue(invalidated.get(), "cancel should trigger invalidation");

        invalidated.set(false);
        value.set(TEST_VALUE);
        assertTrue(!invalidated.get(), "no invalidation after cancel on further updates");
        assertEquals(Integer.valueOf(2), last.get(), "consumer should not update after cancel");
    }
}
