package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * 刀道被动：持续维持后，为自身添加多个属性修饰符（随【刀】道道痕动态变化）。
 * <p>
 * 用于同一被动同时强化护甲与护甲韧性，避免一个 itemId 挂多个被动用途。
 * </p>
 */
public final class DaoDaoSustainedMultiAttributeModifierEffect
    extends AbstractDaoDaoSustainedEffect {

    private static final String DEFAULT_MODIFIER_NAMESPACE = "guzhenrenext";

    private final List<ModifierSpec> modifiers;

    public DaoDaoSustainedMultiAttributeModifierEffect(
        final String usageId,
        final List<ModifierSpec> modifiers
    ) {
        super(usageId, DaoHenHelper.DaoType.DAO_DAO);
        this.modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
    }

    @Override
    protected void onSustainSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo,
        final double daoHenMultiplier
    ) {
        final double multiplier = DaoHenEffectScalingHelper.clampMultiplier(
            daoHenMultiplier
        );
        for (ModifierSpec spec : modifiers) {
            if (spec == null) {
                continue;
            }
            final double baseAmount = UsageMetadataHelper.getDouble(
                usageInfo,
                spec.amountMetaKey,
                spec.defaultAmount
            );
            final double scaled = Math.max(0.0, baseAmount * multiplier);
            applyModifier(user, spec, scaled);
        }
    }

    @Override
    protected void onInactive(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        for (ModifierSpec spec : modifiers) {
            if (spec == null) {
                continue;
            }
            removeModifier(user, spec);
        }
    }

    private static void applyModifier(
        final LivingEntity user,
        final ModifierSpec spec,
        final double amount
    ) {
        final AttributeInstance instance = user.getAttribute(spec.attribute);
        if (instance == null) {
            return;
        }

        final AttributeModifier existing = instance.getModifier(spec.modifierId);
        if (existing != null && Double.compare(existing.amount(), amount) == 0) {
            return;
        }
        if (existing != null) {
            instance.removeModifier(spec.modifierId);
        }
        if (amount <= 0.0) {
            return;
        }
        instance.addTransientModifier(
            new AttributeModifier(spec.modifierId, amount, spec.operation)
        );
    }

    private static void removeModifier(
        final LivingEntity user,
        final ModifierSpec spec
    ) {
        final AttributeInstance instance = user.getAttribute(spec.attribute);
        if (instance == null) {
            return;
        }
        if (instance.getModifier(spec.modifierId) != null) {
            instance.removeModifier(spec.modifierId);
        }
    }

    public static ModifierSpec of(
        final String usageId,
        final Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        final AttributeModifier.Operation operation,
        final String amountMetaKey,
        final double defaultAmount
    ) {
        Objects.requireNonNull(usageId, "usageId");
        Objects.requireNonNull(attribute, "attribute");
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(amountMetaKey, "amountMetaKey");

        final ResourceLocation modifierId = buildModifierId(
            usageId,
            amountMetaKey
        );
        return new ModifierSpec(
            attribute,
            operation,
            amountMetaKey,
            defaultAmount,
            modifierId
        );
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

    public record ModifierSpec(
        Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        AttributeModifier.Operation operation,
        String amountMetaKey,
        double defaultAmount,
        ResourceLocation modifierId
    ) {
        public ModifierSpec {
            Objects.requireNonNull(attribute, "attribute");
            Objects.requireNonNull(operation, "operation");
            Objects.requireNonNull(amountMetaKey, "amountMetaKey");
            Objects.requireNonNull(modifierId, "modifierId");
        }
    }
}
