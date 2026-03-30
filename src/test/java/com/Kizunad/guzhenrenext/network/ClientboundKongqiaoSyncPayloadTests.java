package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.testsupport.Task4RuntimeHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClientboundKongqiaoSyncPayloadTests {

    @Test
    void payloadRetainsRawAndProjectionTagsAndNormalizesNulls() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object raw = api.newCompoundTag();
        api.putDouble(raw, "burstPressure", 7.0D);
        final Object projection = api.newCompoundTag();
        api.putDouble(projection, "effectivePressure", 7.0D);
        api.putInt(projection, "forcedDisabledCount", 2);

        final Object payload = api.newPayload(raw, projection);

        assertEquals(7.0D, api.getDouble(api.payloadData(payload), "burstPressure"));
        assertEquals(
            7.0D,
            api.getDouble(api.payloadProjection(payload), "effectivePressure")
        );
        assertEquals(
            2,
            api.getInt(api.payloadProjection(payload), "forcedDisabledCount")
        );

        final Object emptyPayload = api.newPayload(null, null);
        assertTrue(api.getAllKeys(api.payloadData(emptyPayload)).isEmpty());
        assertTrue(api.getAllKeys(api.payloadProjection(emptyPayload)).isEmpty());
    }
}
