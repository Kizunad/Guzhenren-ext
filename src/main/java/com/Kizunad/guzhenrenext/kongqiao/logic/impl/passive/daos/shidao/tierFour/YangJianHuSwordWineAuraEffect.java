package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierFour;

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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * 四转养剑葫：被动【剑酒养锋】。
 * <p>
 * 设计目标：稳定输出向被动（攻速 + 少量攻击力），以持续真元消耗制衡。</p>
 */
public class YangJianHuSwordWineAuraEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:yangjianhu_passive_sword_wine_aura";

    private static final String META_ATTACK_SPEED_BONUS = "attack_speed_bonus";
    private static final String META_ATTACK_DAMAGE_BONUS = "attack_damage_bonus";

    private static final double DEFAULT_ATTACK_SPEED_BONUS = 0.10;
    private static final double DEFAULT_ATTACK_DAMAGE_BONUS = 1.0;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 210.0;

    private static final ResourceLocation ATTACK_SPEED_MODIFIER_ID =
        ResourceLocation.parse("guzhenren:yangjianhu_attack_speed");
    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER_ID =
        ResourceLocation.parse("guzhenren:yangjianhu_attack_damage");

    @Override
    public String getUsageId() {
        return USAGE_ID;
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
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            deactivate(user);
            return;
        }

        final double niantouCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                0.0
            )
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                0.0
            )
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
                0.0
            )
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
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
            deactivate(user);
            return;
        }

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final double attackSpeedBonus = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ATTACK_SPEED_BONUS,
                DEFAULT_ATTACK_SPEED_BONUS
            ),
            0.0,
            2.0
        );
        final double attackDamageBonus = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ATTACK_DAMAGE_BONUS,
                DEFAULT_ATTACK_DAMAGE_BONUS
            )
        );
        applyOrUpdateModifier(
            user.getAttribute(Attributes.ATTACK_SPEED),
            ATTACK_SPEED_MODIFIER_ID,
            UsageMetadataHelper.clamp(attackSpeedBonus * multiplier, 0.0, 2.0),
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
        applyOrUpdateModifier(
            user.getAttribute(Attributes.ATTACK_DAMAGE),
            ATTACK_DAMAGE_MODIFIER_ID,
            attackDamageBonus * multiplier,
            AttributeModifier.Operation.ADD_VALUE
        );

        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives != null) {
            actives.add(USAGE_ID);
        }
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        deactivate(user);
    }

    private static void deactivate(final LivingEntity user) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives != null) {
            actives.remove(USAGE_ID);
        }
        removeModifiers(user);
    }

    private static void applyOrUpdateModifier(
        final AttributeInstance attribute,
        final ResourceLocation id,
        final double amount,
        final AttributeModifier.Operation operation
    ) {
        if (attribute == null) {
            return;
        }
        final AttributeModifier existing = attribute.getModifier(id);
        if (existing != null && Double.compare(existing.amount(), amount) == 0) {
            return;
        }
        if (existing != null) {
            attribute.removeModifier(id);
        }
        if (Double.compare(amount, 0.0) == 0) {
            return;
        }
        attribute.addTransientModifier(new AttributeModifier(id, amount, operation));
    }

    private static void removeModifiers(final LivingEntity user) {
        final AttributeInstance speed = user.getAttribute(Attributes.ATTACK_SPEED);
        if (speed != null && speed.getModifier(ATTACK_SPEED_MODIFIER_ID) != null) {
            speed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
        }

        final AttributeInstance damage = user.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null && damage.getModifier(ATTACK_DAMAGE_MODIFIER_ID) != null) {
            damage.removeModifier(ATTACK_DAMAGE_MODIFIER_ID);
        }
    }
}
