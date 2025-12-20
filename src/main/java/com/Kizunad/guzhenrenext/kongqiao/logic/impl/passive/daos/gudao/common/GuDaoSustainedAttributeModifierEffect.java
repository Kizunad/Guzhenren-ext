package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
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
 * 骨道通用被动：持续性维持（多资源）+ 属性修饰符。
 * <p>
 * - 资源消耗：由 metadata 指定（含真元折算、念头/精力/魂魄等）。
 * - 属性增幅：随骨道道痕动态变化（倍率裁剪防止失控）。
 * </p>
 */
public class GuDaoSustainedAttributeModifierEffect implements IGuEffect {

    private static final String META_ATTRIBUTE_AMOUNT = "attribute_amount";
    private static final String DEFAULT_MODIFIER_NAMESPACE = "guzhenrenext";
    private static final double DEFAULT_COST = 0.0;

    private final String usageId;
    private final Holder<Attribute> attribute;
    private final AttributeModifier.Operation operation;
    private final double defaultAmount;
    private final ResourceLocation modifierId;

    public GuDaoSustainedAttributeModifierEffect(
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

        final double niantouCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double jingliCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double hunpoCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST
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

        final double baseAmount = UsageMetadataHelper.getDouble(
            usageInfo,
            META_ATTRIBUTE_AMOUNT,
            defaultAmount
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.GU_DAO
        );
        final double amount = DaoHenEffectScalingHelper.scaleValue(
            baseAmount,
            selfMultiplier
        );

        final AttributeModifier existing = attr.getModifier(modifierId);
        if (existing != null && Double.compare(existing.amount(), amount) == 0) {
            return;
        }
        if (existing != null) {
            attr.removeModifier(modifierId);
        }
        attr.addTransientModifier(
            new AttributeModifier(modifierId, amount, operation)
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
        final String path = sanitizeForPath(rawUsageId) + "_"
            + sanitizeForPath(rawSalt);
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
