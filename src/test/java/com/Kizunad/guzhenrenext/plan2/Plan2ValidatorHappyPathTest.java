package com.Kizunad.guzhenrenext.plan2;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2ValidatorHappyPathTest {

    @Test
    void shouldPassTask2ValidatorOnCurrentMatrix() throws IOException {
        List<String> errors = Plan2ContentMatrixValidator.validateCurrentMatrix();
        assertTrue(
            errors.isEmpty(),
            "合法内容矩阵应通过 Task2 校验器，实际错误: " + errors
        );
    }
}
