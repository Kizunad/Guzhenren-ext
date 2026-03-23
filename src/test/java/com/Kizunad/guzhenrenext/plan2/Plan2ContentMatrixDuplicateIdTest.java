package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class Plan2ContentMatrixDuplicateIdTest {

    @Test
    void shouldRejectDuplicateIdSample() throws IOException {
        JsonArray array = Plan2ContentMatrixTestSupport.readMatrixArray();
        assertDoesNotThrow(() -> Plan2ContentMatrixTestSupport.assertNoDuplicateIds(array));

        JsonArray duplicate = new JsonArray();
        for (JsonElement element : array) {
            duplicate.add(element.deepCopy());
        }
        JsonObject clone = array.get(0).getAsJsonObject().deepCopy();
        duplicate.add(clone);

        assertThrows(IllegalStateException.class, () ->
            Plan2ContentMatrixTestSupport.assertNoDuplicateIds(duplicate)
        );
    }
}
