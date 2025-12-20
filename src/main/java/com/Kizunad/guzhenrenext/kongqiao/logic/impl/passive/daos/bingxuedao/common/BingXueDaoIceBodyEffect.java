package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common;

import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * 冰雪道被动：冰肌玉骨（持续性维持 + 多属性增益）。
 * <p>
 * 通过 metadata 配置：<br>
 * - zhenyuan_base_cost_per_second<br>
 * - armor_bonus<br>
 * - toughness_bonus<br>
 * - max_health_bonus<br>
 * - clear_freeze (true/false)<br>
 * </p>
 */
public class BingXueDaoIceBodyEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_NIANTOU_COST_PER_SECOND =
        "niantou_cost_per_second";
    private static final String META_JINGLI_COST_PER_SECOND =
        "jingli_cost_per_second";
    private static final String META_HUNPO_COST_PER_SECOND =
        "hunpo_cost_per_second";
    private static final String META_ARMOR_BONUS = "armor_bonus";
    private static final String META_TOUGHNESS_BONUS = "toughness_bonus";
    private static final String META_MAX_HEALTH_BONUS = "max_health_bonus";
    private static final String META_CLEAR_FREEZE = "clear_freeze";

    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 0.0;
    private static final double DEFAULT_ARMOR_BONUS = 0.0;
    private static final double DEFAULT_TOUGHNESS_BONUS = 0.0;
    private static final double DEFAULT_MAX_HEALTH_BONUS = 0.0;
    private static final boolean DEFAULT_CLEAR_FREEZE = true;

    private static final String DEFAULT_MODIFIER_NAMESPACE = "guzhenrenext";

    private final String usageId;
    private final ResourceLocation armorModifierId;
    private final ResourceLocation toughnessModifierId;
    private final ResourceLocation maxHealthModifierId;

    public BingXueDaoIceBodyEffect(final String usageId) {
        this.usageId = usageId;
        this.armorModifierId = buildModifierId(usageId, "armor");
        this.toughnessModifierId = buildModifierId(usageId, "toughness");
        this.maxHealthModifierId = buildModifierId(usageId, "max_health");
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public void onSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            setActive(user, false);
            removeAllModifiers(user);
            return;
        }

        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        final double niantouCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST_PER_SECOND, 0.0)
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST_PER_SECOND, 0.0)
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST_PER_SECOND, 0.0)
        );
        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            setActive(user, false);
            removeAllModifiers(user);
            return;
        }

        applyModifiers(user, usageInfo);
        if (
            UsageMetadataHelper.getBoolean(
                usageInfo,
                META_CLEAR_FREEZE,
                DEFAULT_CLEAR_FREEZE
            )
        ) {
            user.setTicksFrozen(0);
        }

        setActive(user, true);
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }
        setActive(user, false);
        removeAllModifiers(user);
    }

    private void applyModifiers(
        final LivingEntity user,
        final NianTouData.Usage usageInfo
    ) {
        final double armorBonus = UsageMetadataHelper.getDouble(
            usageInfo,
            META_ARMOR_BONUS,
            DEFAULT_ARMOR_BONUS
        );
        final double toughnessBonus = UsageMetadataHelper.getDouble(
            usageInfo,
            META_TOUGHNESS_BONUS,
            DEFAULT_TOUGHNESS_BONUS
        );
        final double maxHealthBonus = UsageMetadataHelper.getDouble(
            usageInfo,
            META_MAX_HEALTH_BONUS,
            DEFAULT_MAX_HEALTH_BONUS
        );

        applyModifier(
            user.getAttribute(Attributes.ARMOR),
            armorModifierId,
            armorBonus
        );
        applyModifier(
            user.getAttribute(Attributes.ARMOR_TOUGHNESS),
            toughnessModifierId,
            toughnessBonus
        );
        applyModifier(
            user.getAttribute(Attributes.MAX_HEALTH),
            maxHealthModifierId,
            maxHealthBonus
        );
    }

    private static void applyModifier(
        final AttributeInstance attribute,
        final ResourceLocation modifierId,
        final double amount
    ) {
        if (attribute == null || modifierId == null) {
            return;
        }
        final AttributeModifier existing = attribute.getModifier(modifierId);
        if (existing != null && Double.compare(existing.amount(), amount) == 0) {
            return;
        }
        if (existing != null) {
            attribute.removeModifier(modifierId);
        }
        if (Double.compare(amount, 0.0) == 0) {
            return;
        }
        attribute.addTransientModifier(
            new AttributeModifier(modifierId, amount, AttributeModifier.Operation.ADD_VALUE)
        );
    }

    private void removeAllModifiers(final LivingEntity user) {
        removeModifier(user.getAttribute(Attributes.ARMOR), armorModifierId);
        removeModifier(
            user.getAttribute(Attributes.ARMOR_TOUGHNESS),
            toughnessModifierId
        );
        removeModifier(
            user.getAttribute(Attributes.MAX_HEALTH),
            maxHealthModifierId
        );
    }

    private static void removeModifier(
        final AttributeInstance attribute,
        final ResourceLocation modifierId
    ) {
        if (attribute == null || modifierId == null) {
            return;
        }
        if (attribute.getModifier(modifierId) != null) {
            attribute.removeModifier(modifierId);
        }
    }

    private static ResourceLocation buildModifierId(
        final String usageId,
        final String salt
    ) {
        final String rawUsageId = usageId == null ? "unknown" : usageId;
        final String rawSalt = salt == null || salt.isBlank() ? "modifier" : salt;
        final String path = sanitizeForPath(rawUsageId) + "_" + sanitizeForPath(rawSalt);
        return ResourceLocation.parse(DEFAULT_MODIFIER_NAMESPACE + ":" + path);
    }

    private static String sanitizeForPath(final String value) {
        final String raw = value == null ? "" : value;
        return raw.replace(':', '_').replace('/', '_');
    }

    private void setActive(final LivingEntity user, final boolean active) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives == null) {
            return;
        }
        if (active) {
            actives.add(usageId);
            return;
        }
        actives.remove(usageId);
    }
}
