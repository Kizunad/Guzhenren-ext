package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

/**
 * 三转冰魄蛊：冰魄护身。
 * <p>
 * 1. 消耗真元。
 * 2. 增加盔甲韧性 (Armor Toughness)。
 * 3. 产生微弱的雪花粒子特效 (死寂氛围)。
 * </p>
 */
public class BingPoGuEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:bingpogu_passive_guard";
    
    // Config
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 240.0;
    private static final double DEFAULT_TOUGHNESS = 4.0;
    private static final float SNOWFLAKE_CHANCE = 0.3f;
    private static final int SNOWFLAKE_COUNT = 1;
    private static final double SNOWFLAKE_OFFSET = 0.5;
    private static final double SNOWFLAKE_SPEED = 0.01;
    
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID =
        ResourceLocation.parse("guzhenren:bingpogu_toughness");

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onSecond(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            removeAttribute(user);
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUN_DAO
        );
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
            ) * selfMultiplier
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
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            removeAttribute(user);
            return;
        }

        // 2. 激活状态
        KongqiaoAttachments.getActivePassives(user).add(USAGE_ID);

        // 3. 属性修饰 (韧性)
        double toughness = UsageMetadataHelper.getDouble(
            usageInfo,
            "toughness_bonus",
            DEFAULT_TOUGHNESS
        );
        applyAttribute(user, toughness * selfMultiplier);
        
        // 4. 环境特效
        if (
            user.level() instanceof ServerLevel serverLevel &&
            user.getRandom().nextFloat() < SNOWFLAKE_CHANCE
        ) {
            // 偶尔飘落雪花，代表身处寒域
            serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,
                user.getX(),
                user.getY() + user.getBbHeight() / 2,
                user.getZ(),
                SNOWFLAKE_COUNT,
                SNOWFLAKE_OFFSET,
                SNOWFLAKE_OFFSET,
                SNOWFLAKE_OFFSET,
                SNOWFLAKE_SPEED
            );
        }
    }

    @Override
    public void onUnequip(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
        removeAttribute(user);
    }
    
    private void applyAttribute(LivingEntity user, double amount) {
        AttributeInstance attr = user.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (attr != null) {
            AttributeModifier modifier = attr.getModifier(TOUGHNESS_MODIFIER_ID);
            if (modifier == null || modifier.amount() != amount) {
                if (modifier != null) {
                    attr.removeModifier(TOUGHNESS_MODIFIER_ID);
                }
                attr.addTransientModifier(
                    new AttributeModifier(
                        TOUGHNESS_MODIFIER_ID,
                        amount,
                        AttributeModifier.Operation.ADD_VALUE
                    )
                );
            }
        }
    }

    private void removeAttribute(LivingEntity user) {
        AttributeInstance attr = user.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (attr != null && attr.getModifier(TOUGHNESS_MODIFIER_ID) != null) {
            attr.removeModifier(TOUGHNESS_MODIFIER_ID);
        }
    }
}
