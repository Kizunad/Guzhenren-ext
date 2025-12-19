package com.Kizunad.guzhenrenext.kongqiao.shazhao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * 杀招数据命名规范校验器。
 * <p>
 * 不符合规范的 JSON 会被记录错误并跳过加载，避免 UI 与解锁逻辑出现异常。
 * </p>
 */
public final class ShazhaoDataValidator {

    private static final String ID_PATTERN = "^[a-z0-9_]+$";
    private static final String METADATA_KEY_PATTERN = "^[a-z0-9_]+$";

    private ShazhaoDataValidator() {}

    public static List<String> validate(@Nullable final ShazhaoData data) {
        final List<String> errors = new ArrayList<>();
        if (data == null) {
            errors.add("ShazhaoData 为空");
            return errors;
        }

        validateShazhaoId(data, errors);
        validateRequiredItems(data, errors);
        validateDisplayFields(data, errors);
        validateCost(data, errors);
        validateMetadata(data, errors);
        return errors;
    }

    private static void validateShazhaoId(
        final ShazhaoData data,
        final List<String> errors
    ) {
        final String shazhaoId = data.shazhaoID();
        if (shazhaoId == null || shazhaoId.isBlank()) {
            errors.add("shazhaoID 不能为空");
            return;
        }
        if (!shazhaoId.equals(shazhaoId.toLowerCase(Locale.ROOT))) {
            errors.add("shazhaoID 必须全小写: " + shazhaoId);
        }

        final ResourceLocation id;
        try {
            id = ResourceLocation.parse(shazhaoId);
        } catch (Exception e) {
            errors.add("shazhaoID 不是合法的 ResourceLocation: " + shazhaoId);
            return;
        }

        final String path = id.getPath();
        if (!path.matches(ID_PATTERN)) {
            errors.add("shazhaoID 的 path 必须为 snake_case: " + shazhaoId);
        }
        if (!ShazhaoId.isValidPath(path)) {
            errors.add(
                "shazhaoID 的 path 必须以 shazhao_passive_ 或 shazhao_active_ 开头: " +
                shazhaoId
            );
        }
    }

    private static void validateRequiredItems(
        final ShazhaoData data,
        final List<String> errors
    ) {
        final List<String> required = data.requiredItems();
        if (required == null || required.isEmpty()) {
            errors.add("required_items 不能为空");
            return;
        }

        final Set<String> seen = new HashSet<>();
        for (int i = 0; i < required.size(); i++) {
            final String itemId = required.get(i);
            if (itemId == null || itemId.isBlank()) {
                errors.add("required_items[" + i + "] 不能为空");
                continue;
            }
            if (!itemId.equals(itemId.toLowerCase(Locale.ROOT))) {
                errors.add("required_items[" + i + "] 必须全小写: " + itemId);
            }
            if (!seen.add(itemId)) {
                errors.add("required_items[" + i + "] 重复: " + itemId);
            }

            final ResourceLocation id;
            try {
                id = ResourceLocation.parse(itemId);
            } catch (Exception e) {
                errors.add("required_items[" + i + "] 非法: " + itemId);
                continue;
            }
            if (!id.getPath().matches(ID_PATTERN)) {
                errors.add("required_items[" + i + "] 必须为 snake_case: " + itemId);
            }

            final Item resolved = BuiltInRegistries.ITEM
                .getOptional(id)
                .orElse(Items.AIR);
            if (resolved == Items.AIR) {
                errors.add("required_items[" + i + "] 对应物品不存在: " + itemId);
            }
        }
    }

    private static void validateDisplayFields(
        final ShazhaoData data,
        final List<String> errors
    ) {
        if (data.title() == null || data.title().isBlank()) {
            errors.add("title 不能为空");
        }
        if (data.desc() == null || data.desc().isBlank()) {
            errors.add("desc 不能为空");
        }
        if (data.info() == null || data.info().isBlank()) {
            errors.add("info 不能为空");
        }
    }

    private static void validateCost(
        final ShazhaoData data,
        final List<String> errors
    ) {
        if (data.costTotalNiantou() < 0) {
            errors.add("cost_total_niantou 不能小于 0");
        }
    }

    private static void validateMetadata(
        final ShazhaoData data,
        final List<String> errors
    ) {
        Map<String, String> metadata = data.metadata();
        if (metadata == null || metadata.isEmpty()) {
            return;
        }

        for (String key : metadata.keySet()) {
            if (key == null || key.isBlank()) {
                errors.add("metadata key 不能为空");
                continue;
            }
            if (!key.equals(key.toLowerCase(Locale.ROOT))) {
                errors.add("metadata key 必须全小写: " + key);
            }
            if (!key.matches(METADATA_KEY_PATTERN)) {
                errors.add("metadata key 必须为 snake_case: " + key);
            }
        }
    }
}
