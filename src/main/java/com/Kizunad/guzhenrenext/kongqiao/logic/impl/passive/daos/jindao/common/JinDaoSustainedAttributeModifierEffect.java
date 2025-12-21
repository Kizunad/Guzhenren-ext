package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * 金道通用被动：持续性维持（多资源）+ 属性修饰符。
 * <p>
 * 通过 metadata 配置：<br>
 * - zhenyuan_base_cost_per_second / niantou_cost_per_second / jingli_cost_per_second / hunpo_cost_per_second<br>
 * - attribute_amount<br>
 * </p>
 */
public class JinDaoSustainedAttributeModifierEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_ATTRIBUTE_AMOUNT = "attribute_amount";

    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 0.0;
    private static final String DEFAULT_MODIFIER_NAMESPACE = "guzhenrenext";

    private final String usageId;
    private final Holder<Attribute> attribute;
    private final AttributeModifier.Operation operation;
    private final double defaultAmount;
    private final ResourceLocation modifierId;

    public JinDaoSustainedAttributeModifierEffect(
        final String usageId,
        final Holder<Attribute> attribute,
        final AttributeModifier.Operation operation,
        final double defaultAmount,
        final String uuidSalt
    ) {
        this.usageId = usageId;
        this.attribute = attribute;
        this.operation = operation;
        this.defaultAmount = defaultAmount;
        this.modifierId = buildModifierId(usageId, uuidSalt);
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
            removeModifier(user);
            return;
        }

        final double niantouCostPerSecond = UsageMetadataHelper.getDouble(
            usageInfo,
            GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
            0.0
        );
        final double jingliCostPerSecond = UsageMetadataHelper.getDouble(
            usageInfo,
            GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
            0.0
        );
        final double hunpoCostPerSecond = UsageMetadataHelper.getDouble(
            usageInfo,
            GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
            0.0
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        if (!GuEffectCostHelper.tryConsumeSustain(
            user,
            niantouCostPerSecond,
            jingliCostPerSecond,
            hunpoCostPerSecond,
            zhenyuanBaseCostPerSecond
        )) {
            setActive(user, false);
            removeModifier(user);
            return;
        }

        setActive(user, true);
        applyModifier(user, usageInfo);
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
        removeModifier(user);
    }

    private void applyModifier(
        final LivingEntity user,
        final NianTouData.Usage usageInfo
    ) {
        if (attribute == null) {
            return;
        }
        final AttributeInstance attr = user.getAttribute(attribute);
        if (attr == null) {
            return;
        }

        final double amount = UsageMetadataHelper.getDouble(
            usageInfo,
            META_ATTRIBUTE_AMOUNT,
            defaultAmount
        );
        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.JIN_DAO
        );
        final double finalAmount = amount * multiplier;
        final AttributeModifier existing = attr.getModifier(modifierId);
        if (
            existing != null
                && Double.compare(existing.amount(), finalAmount) == 0
        ) {
            return;
        }
        if (existing != null) {
            attr.removeModifier(modifierId);
        }
        attr.addTransientModifier(
            new AttributeModifier(modifierId, finalAmount, operation)
        );
    }

    private void removeModifier(final LivingEntity user) {
        if (attribute == null) {
            return;
        }
        final AttributeInstance attr = user.getAttribute(attribute);
        if (attr == null) {
            return;
        }
        if (attr.getModifier(modifierId) != null) {
            attr.removeModifier(modifierId);
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
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(
            user
        );
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

