package com.Kizunad.guzhenrenext.kongqiao.niantou;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * 念头数据命名规范校验器。
 * <p>
 * 该校验器用于在资源加载阶段（{@link NianTouDataLoader}）强制命名规范：
 * 不符合规范的 JSON 会被记录错误并跳过加载，避免后续 UI/技能触发出现不确定行为。
 * </p>
 */
public final class NianTouDataValidator {

    private static final String ITEM_ID_FIELD = "itemID";
    private static final String USAGE_ID_FIELD = "usageID";

    private static final String ID_PATTERN = "^[a-z0-9_]+$";
    private static final String METADATA_KEY_PATTERN = "^[a-z0-9_]+$";

    private NianTouDataValidator() {}

    /**
     * 校验一份念头数据。
     *
     * @param data NianTouData
     * @return 为空表示校验通过；非空表示错误列表
     */
    public static List<String> validate(@Nullable final NianTouData data) {
        final List<String> errors = new ArrayList<>();
        if (data == null) {
            errors.add("NianTouData 为空");
            return errors;
        }

        validateItemId(data, errors);
        validateUsages(data, errors);
        return errors;
    }

    private static void validateItemId(final NianTouData data, final List<String> errors) {
        final String itemId = data.itemID();
        if (itemId == null || itemId.isBlank()) {
            errors.add(ITEM_ID_FIELD + " 不能为空");
            return;
        }
        if (!itemId.equals(itemId.toLowerCase(Locale.ROOT))) {
            errors.add(ITEM_ID_FIELD + " 必须全小写: " + itemId);
        }

        final ResourceLocation id;
        try {
            id = ResourceLocation.parse(itemId);
        } catch (Exception e) {
            errors.add(ITEM_ID_FIELD + " 不是合法的 ResourceLocation: " + itemId);
            return;
        }

        final String path = id.getPath();
        if (!path.matches(ID_PATTERN)) {
            errors.add(
                ITEM_ID_FIELD + " 的 path 必须为 snake_case: " + id.getNamespace() + ":" + path
            );
        }

        final Item resolved = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        if (resolved == Items.AIR) {
            errors.add(ITEM_ID_FIELD + " 对应物品不存在: " + itemId);
        }
    }

    private static void validateUsages(final NianTouData data, final List<String> errors) {
        if (data.usages() == null || data.usages().isEmpty()) {
            errors.add("usages 不能为空");
            return;
        }

        final Set<String> seenUsageIds = new HashSet<>();
        for (int i = 0; i < data.usages().size(); i++) {
            final NianTouData.Usage usage = data.usages().get(i);
            validateUsage(i, usage, seenUsageIds, errors);
        }
    }

    private static void validateUsage(
        final int index,
        @Nullable final NianTouData.Usage usage,
        final Set<String> seenUsageIds,
        final List<String> errors
    ) {
        final String prefix = "usages[" + index + "].";
        if (usage == null) {
            errors.add(prefix + " 不能为空");
            return;
        }
        if (usage.usageTitle() == null || usage.usageTitle().isBlank()) {
            errors.add(prefix + "usageTitle 不能为空");
        }

        final String usageId = usage.usageID();
        if (usageId == null || usageId.isBlank()) {
            errors.add(prefix + USAGE_ID_FIELD + " 不能为空");
            return;
        }
        if (!usageId.equals(usageId.toLowerCase(Locale.ROOT))) {
            errors.add(prefix + USAGE_ID_FIELD + " 必须全小写: " + usageId);
        }

        final ResourceLocation id;
        try {
            id = ResourceLocation.parse(usageId);
        } catch (Exception e) {
            errors.add(prefix + USAGE_ID_FIELD + " 不是合法的 ResourceLocation: " + usageId);
            return;
        }

        final String path = id.getPath();
        if (!path.matches(ID_PATTERN)) {
            errors.add(prefix + USAGE_ID_FIELD + " 的 path 必须为 snake_case: " + usageId);
        }

        if (!seenUsageIds.add(usageId)) {
            errors.add(prefix + USAGE_ID_FIELD + " 重复: " + usageId);
        }

        final boolean isSkill = NianTouUsageId.isActive(usageId);
        final boolean isPassive = NianTouUsageId.isPassive(usageId);
        if (isSkill && isPassive) {
            errors.add(prefix + USAGE_ID_FIELD + " 不允许同时包含 _active_ 与 _passive_: " + usageId);
        } else if (!isSkill && !isPassive) {
            errors.add(prefix + USAGE_ID_FIELD + " 必须包含 _active_ 或 _passive_: " + usageId);
        }

        validateMetadata(prefix, usage.metadata(), errors);
    }

    private static void validateMetadata(
        final String prefix,
        @Nullable final Map<String, String> metadata,
        final List<String> errors
    ) {
        if (metadata == null || metadata.isEmpty()) {
            return;
        }
        for (String key : metadata.keySet()) {
            if (key == null || key.isBlank()) {
                errors.add(prefix + "metadata key 不能为空");
                continue;
            }
            if (!key.equals(key.toLowerCase(Locale.ROOT))) {
                errors.add(prefix + "metadata key 必须全小写: " + key);
            }
            if (!key.matches(METADATA_KEY_PATTERN)) {
                errors.add(prefix + "metadata key 必须为 snake_case: " + key);
            }
        }
    }
}
