package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * 刀道被动：持续维持后，为自身添加属性修饰符（随【刀】道道痕动态变化）。
 * <p>
 * 用于护甲/护甲韧性等“量级以几百为主”的属性强化。
 * </p>
 */
public final class DaoDaoSustainedAttributeModifierEffect
    extends AbstractDaoDaoSustainedEffect {

    private static final String DEFAULT_MODIFIER_NAMESPACE = "guzhenrenext";

    private final Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute;
    private final AttributeModifier.Operation operation;
    private final String amountMetaKey;
    private final double defaultAmount;
    private final ResourceLocation modifierId;

    public DaoDaoSustainedAttributeModifierEffect(
        final String usageId,
        final Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        final AttributeModifier.Operation operation,
        final String amountMetaKey,
        final double defaultAmount
    ) {
        super(usageId, DaoHenHelper.DaoType.DAO_DAO);
        this.attribute = Objects.requireNonNull(attribute, "attribute");
        this.operation = Objects.requireNonNull(operation, "operation");
        this.amountMetaKey = Objects.requireNonNull(amountMetaKey, "amountMetaKey");
        this.defaultAmount = defaultAmount;
        this.modifierId = buildModifierId(usageId, amountMetaKey);
    }

    @Override
    protected void onSustainSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo,
        final double daoHenMultiplier
    ) {
        final double baseAmount = UsageMetadataHelper.getDouble(
            usageInfo,
            amountMetaKey,
            defaultAmount
        );
        final double scaled = DaoHenEffectScalingHelper.scaleValue(
            baseAmount,
            daoHenMultiplier
        );
        applyModifier(user, scaled);
    }

    @Override
    protected void onInactive(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        removeModifier(user);
    }

    private void applyModifier(final LivingEntity user, final double amount) {
        final AttributeInstance instance = user.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        final double safeAmount = Math.max(0.0, amount);
        final AttributeModifier existing = instance.getModifier(modifierId);
        if (existing != null && Double.compare(existing.amount(), safeAmount) == 0) {
            return;
        }
        if (existing != null) {
            instance.removeModifier(modifierId);
        }
        if (safeAmount > 0.0) {
            instance.addTransientModifier(
                new AttributeModifier(modifierId, safeAmount, operation)
            );
        }
    }

    private void removeModifier(final LivingEntity user) {
        final AttributeInstance instance = user.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (instance.getModifier(modifierId) != null) {
            instance.removeModifier(modifierId);
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
}
