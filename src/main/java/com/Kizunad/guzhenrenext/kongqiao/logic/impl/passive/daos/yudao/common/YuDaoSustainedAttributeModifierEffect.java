package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
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
 * 宇道通用被动：持续性维持（真元）+ 属性修饰符。
 * <p>
 * 通过 metadata 配置：<br>
 * - zhenyuan_base_cost_per_second<br>
 * - attribute_amount<br>
 * </p>
 */
public class YuDaoSustainedAttributeModifierEffect implements IGuEffect {

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

    public YuDaoSustainedAttributeModifierEffect(
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

        final double baseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        final double cost = ZhenYuanHelper.calculateGuCost(user, baseCost);
        if (cost > 0.0 && !ZhenYuanHelper.hasEnough(user, cost)) {
            setActive(user, false);
            removeModifier(user);
            return;
        }
        if (cost > 0.0) {
            ZhenYuanHelper.modify(user, -cost);
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

