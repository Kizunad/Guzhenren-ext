package com.Kizunad.guzhenrenext.plan2;

import com.google.gson.JsonArray;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class Plan2ContentMatrixDeterminismTest {

    @Test
    void shouldBeByteStableUnderCanonicalSerialization() throws IOException {
        JsonArray array = Plan2ContentMatrixTestSupport.readMatrixArray();
        String expected = Plan2ContentMatrixTestSupport.readMatrixText();

        String canonicalFirst = Plan2ContentMatrixTestSupport.canonicalize(array);
        String canonicalSecond = Plan2ContentMatrixTestSupport.canonicalize(array);

        assertEquals(canonicalFirst, canonicalSecond);
        assertEquals(expected, canonicalFirst);
    }
}
