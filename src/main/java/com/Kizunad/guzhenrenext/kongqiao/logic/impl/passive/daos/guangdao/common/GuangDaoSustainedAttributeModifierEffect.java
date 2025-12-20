package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;

/**
 * 光道通用被动：持续性维持（多资源）+ 属性修饰符（随光道道痕动态变化）。
 * <p>
 * 通过 metadata 配置：<br>
 * - zhenyuan_base_cost_per_second / niantou_cost_per_second / jingli_cost_per_second / hunpo_cost_per_second<br>
 * - attribute_amount（基础值，最终值会乘以光道道痕倍率）<br>
 * </p>
 */
public class GuangDaoSustainedAttributeModifierEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_NIANTOU_COST_PER_SECOND =
        "niantou_cost_per_second";
    private static final String META_JINGLI_COST_PER_SECOND =
        "jingli_cost_per_second";
    private static final String META_HUNPO_COST_PER_SECOND =
        "hunpo_cost_per_second";
    private static final String META_ATTRIBUTE_AMOUNT = "attribute_amount";

    private static final double DEFAULT_COST = 0.0;
    private static final double DEFAULT_ATTRIBUTE_AMOUNT = 0.0;
    private static final String DEFAULT_MODIFIER_NAMESPACE = "guzhenrenext";

    private final String usageId;
    private final ResourceLocation modifierId;
    private final AttributeInstanceAccessor accessor;

    /**
     * 通过 accessor 提供具体属性实例获取与修饰符操作，避免在构造器中直接依赖 Holder。
     */
    public GuangDaoSustainedAttributeModifierEffect(
        final String usageId,
        final AttributeInstanceAccessor accessor
    ) {
        this.usageId = usageId;
        this.accessor = accessor;
        this.modifierId = buildModifierId(usageId, accessor.getSalt());
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

        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double niantouCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_NIANTOU_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double jingliCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_JINGLI_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double hunpoCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HUNPO_COST_PER_SECOND,
                DEFAULT_COST
            )
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
        final AttributeInstance attribute = accessor.get(user);
        if (attribute == null) {
            return;
        }

        final double baseAmount = UsageMetadataHelper.getDouble(
            usageInfo,
            META_ATTRIBUTE_AMOUNT,
            DEFAULT_ATTRIBUTE_AMOUNT
        );
        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.GUANG_DAO
        );
        final double amount = baseAmount * multiplier;

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
            new AttributeModifier(modifierId, amount, accessor.getOperation())
        );
    }

    private void removeModifier(final LivingEntity user) {
        final AttributeInstance attribute = accessor.get(user);
        if (attribute == null) {
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

    /**
     * 属性实例获取器（减少不同属性参数的重复构造）。
     */
    public interface AttributeInstanceAccessor {
        AttributeInstance get(LivingEntity user);

        Operation getOperation();

        String getSalt();
    }
}

