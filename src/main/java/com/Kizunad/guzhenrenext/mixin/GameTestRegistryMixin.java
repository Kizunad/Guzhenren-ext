package com.Kizunad.guzhenrenext.mixin;

import java.lang.reflect.Method;
import java.util.Set;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GameTest 注册过滤：
 * 1) 默认场景（不传 neoforge.test.batch）下，跳过 ARCHIVED 批次；
 * 2) 指定 batch 时，仅保留目标 batch，其余全部取消注册。
 */
@Mixin(GameTestRegistry.class)
public abstract class GameTestRegistryMixin {

    private static final String TEST_BATCH_PROPERTY = "neoforge.test.batch";
    private static final String ARCHIVED_BATCH_FALLBACK = "guzhenrenext_archived";
    private static final String ARCHIVED_BATCH = resolveArchivedBatchValue();

    @Inject(method = "register(Ljava/lang/reflect/Method;Ljava/util/Set;)V", at = @At("HEAD"), cancellable = true)
    private static void filterGameTestsByBatch(Method method, Set<String> ignoredAllowedNamespaces,
            CallbackInfo ci) {
        final GameTest gameTest = method.getAnnotation(GameTest.class);
        if (gameTest == null) {
            // 安全放行：不是 @GameTest 注册路径时，不做拦截，避免误杀。
            return;
        }

        final String currentBatch = gameTest.batch();
        if (currentBatch == null || currentBatch.isBlank()) {
            // 安全放行：批次为空或不可读时，不做过滤，避免影响异常/兼容路径。
            return;
        }

        final String expectedBatch = System.getProperty(TEST_BATCH_PROPERTY);
        if (expectedBatch == null || expectedBatch.isBlank()) {
            // 默认模式：仅过滤归档批次，保证日常执行不跑已归档用例。
            if (ARCHIVED_BATCH.equals(currentBatch)) {
                ci.cancel();
            }
            return;
        }

        // 显式指定模式：只保留目标批次，确保 CI/本地可精准执行单批次。
        if (!expectedBatch.equals(currentBatch)) {
            ci.cancel();
        }
    }

    private static String resolveArchivedBatchValue() {
        try {
            final Class<?> testBatchesClass = Class.forName("com.Kizunad.customNPCs_test.utils.TestBatches");
            final Object archivedValue = testBatchesClass.getField("ARCHIVED").get(null);
            if (archivedValue instanceof String archivedBatch && !archivedBatch.isBlank()) {
                // 若 Task3 后常量存在，优先对齐该常量值，减少后续维护成本。
                return archivedBatch;
            }
        } catch (ReflectiveOperationException exception) {
            // 当前阶段允许 TestBatches.ARCHIVED 尚未引入；回落到约定字符串，保持兼容。
        }
        return ARCHIVED_BATCH_FALLBACK;
    }
}
