package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.network.ClientboundBackPngEffectPayload;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 熊魂蛊：战斗技能【熊罴撼地】（主动）。
 * <p>
 * 效果：
 * 1) 以自身为中心震地，对 3 格内敌人造成缓慢 II。
 * 2) 造成巨量魂魄伤害 + 普通伤害（混合机制：有魂魄就扣魂魄，否则转为生命值伤害）。
 * 3) 自身短暂免疫击退（用击退抗性属性实现，持续 2 秒）。
 * 4) 视觉：地面裂纹粒子 + 背后巨熊虚影 PNG（客户端渲染）。
 * </p>
 */
public class XiongHunGuEarthShatterEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:xionghungu_active_earthshatter";

    private static final String TAG_KNOCKBACK_TICKS =
        "guzhenrenext_xionghungu_knockback_ticks";
    private static final ResourceLocation KNOCKBACK_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            GuzhenrenExt.MODID,
            "xionghungu_knockback_immune"
        );

    private static final double DEFAULT_RADIUS = 3.0;
    private static final double DEFAULT_SOUL_DAMAGE = 100.0;
    private static final float DEFAULT_PHYSICAL_DAMAGE = 40.0F;
    private static final int DEFAULT_SLOW_DURATION_TICKS = 60;
    private static final int DEFAULT_KNOCKBACK_IMMUNE_TICKS = 40;
    private static final double DEFAULT_ACTIVATE_SOUL_COST = 20.0;
    private static final double DEFAULT_ACTIVATE_ZHENYUAN_COST_SANZHUAN_YIJIE = 20.0;
    /**
     * 三转一阶真元到 baseCost 的换算分母。
     * <p>
     * 来自 {@link ZhenYuanHelper#calculateGuCost} 的反推：三转一阶（zhuanshu=3, jieduan=1）时，
     * denominator = (2^(1 + 3*4) * 3 * 3) / 96 = 768。
     * 设定“消耗 20 三转一阶真元”则 baseCost = 20 * 768 = 15360，再按玩家当前转数/阶段换算实际消耗。
     * </p>
     */
    private static final double ZHENYUAN_SANZHUAN_YIJIE_DENOMINATOR = 768.0;

    private static final ResourceLocation BEAR_PHANTOM_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(
            GuzhenrenExt.MODID,
            "textures/effects/bear_phantom.png"
        );

    private static final float BEAR_PHANTOM_ASPECT = 1365.0F / 768.0F;
    private static final float BEAR_PHANTOM_HEIGHT = 1.5F;
    private static final float BEAR_PHANTOM_WIDTH = BEAR_PHANTOM_HEIGHT * BEAR_PHANTOM_ASPECT;
    private static final float BEAR_PHANTOM_BACK_OFFSET = 0.9F;
    private static final float BEAR_PHANTOM_UP_OFFSET = 0.15F;
    private static final int BEAR_PHANTOM_COLOR = 0x88FFFFFF;
    private static final boolean BEAR_PHANTOM_FULL_BRIGHT = true;

    private static final double SOUL_ACTIVE_RATIO = 0.2;
    private static final double ZERO = 0.0;

    private static final int CRACK_RING_SAMPLES = 36;
    private static final double CRACK_Y_OFFSET = 0.05;
    private static final double CRACK_SPREAD_XZ = 0.08;
    private static final double CRACK_SPREAD_Y = 0.02;
    private static final double CRACK_SPEED = 0.02;
    private static final double AOE_HEIGHT = 1.5;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onTick(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }
        int ticks = getKnockbackTicks(stack);
        if (ticks <= 0) {
            removeKnockbackImmunity(user);
            return;
        }

        applyKnockbackImmunity(user);
        setKnockbackTicks(stack, ticks - 1);
    }

    @Override
    public void onUnequip(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }
        setKnockbackTicks(stack, 0);
        removeKnockbackImmunity(user);
    }

    @Override
    public boolean onActivate(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return false;
        }
        if (!isSoulSufficient(user)) {
            if (user instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                    Component.literal("魂魄不足，熊魂蛊沉眠"),
                    true
                );
            }
            return false;
        }

        double soulCost = getMetaDouble(
            usageInfo,
            "activate_soul_cost",
            DEFAULT_ACTIVATE_SOUL_COST
        );
        double zhenyuanCostSanzhuanYijie = getMetaDouble(
            usageInfo,
            "activate_zhenyuan_cost_sanzhuan_yijie",
            DEFAULT_ACTIVATE_ZHENYUAN_COST_SANZHUAN_YIJIE
        );
        double zhenyuanBaseCost = zhenyuanCostSanzhuanYijie
            * ZHENYUAN_SANZHUAN_YIJIE_DENOMINATOR;
        double realZhenyuanCost = ZhenYuanHelper.calculateGuCost(user, zhenyuanBaseCost);

        if (!hasEnoughCost(user, soulCost, realZhenyuanCost)) {
            if (user instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                    Component.literal(
                        "消耗不足：需要 "
                            + soulCost
                            + " 魂魄 + "
                            + zhenyuanCostSanzhuanYijie
                            + " 三转一阶真元"
                    ),
                    true
                );
            }
            return false;
        }
        consumeCost(user, soulCost, realZhenyuanCost);

        double radius = getMetaDouble(usageInfo, "radius", DEFAULT_RADIUS);
        double soulDamage = getMetaDouble(
            usageInfo,
            "soul_damage",
            DEFAULT_SOUL_DAMAGE
        );
        float physicalDamage = getMetaFloat(
            usageInfo,
            "physical_damage",
            DEFAULT_PHYSICAL_DAMAGE
        );
        int slowDuration = getMetaInt(
            usageInfo,
            "slow_duration_ticks",
            DEFAULT_SLOW_DURATION_TICKS
        );
        int immuneTicks = getMetaInt(
            usageInfo,
            "knockback_immune_ticks",
            DEFAULT_KNOCKBACK_IMMUNE_TICKS
        );

        // 1) 自身击退免疫
        setKnockbackTicks(stack, immuneTicks);
        applyKnockbackImmunity(user);

        // 2) AOE 震地
        AABB area = user.getBoundingBox().inflate(radius, AOE_HEIGHT, radius);
        List<LivingEntity> targets = user
            .level()
            .getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : targets) {
            if (target == user) {
                continue;
            }
            if (isAlly(user, target)) {
                continue;
            }

            target.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    slowDuration,
                    1
                )
            );
            applySoulDamage(user, target, soulDamage);
            target.hurt(user.damageSources().mobAttack(user), physicalDamage);
        }

        // 3) 裂纹粒子（以脚下方块作为材质）
        if (user.level() instanceof ServerLevel serverLevel) {
            spawnCracks(serverLevel, user.position(), radius);
        }

        // 4) 背后巨熊虚影（客户端渲染，发送给周围跟踪该实体的玩家 + 施放者自己）
        int pngTicks = getMetaInt(usageInfo, "png_duration_ticks", immuneTicks);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
            user,
            new ClientboundBackPngEffectPayload(
                user.getId(),
                pngTicks,
                BEAR_PHANTOM_TEXTURE,
                BEAR_PHANTOM_WIDTH,
                BEAR_PHANTOM_HEIGHT,
                BEAR_PHANTOM_BACK_OFFSET,
                BEAR_PHANTOM_UP_OFFSET,
                BEAR_PHANTOM_COLOR,
                BEAR_PHANTOM_FULL_BRIGHT
            )
        );

        return true;
    }

    private static boolean hasEnoughCost(
        LivingEntity user,
        double soulCost,
        double zhenyuanCost
    ) {
        if (soulCost > 0 && HunPoHelper.getAmount(user) < soulCost) {
            return false;
        }
        if (zhenyuanCost > 0 && !ZhenYuanHelper.hasEnough(user, zhenyuanCost)) {
            return false;
        }
        return true;
    }

    private static void consumeCost(
        LivingEntity user,
        double soulCost,
        double zhenyuanCost
    ) {
        if (soulCost > 0) {
            HunPoHelper.modify(user, -soulCost);
        }
        if (zhenyuanCost > 0) {
            ZhenYuanHelper.modify(user, -zhenyuanCost);
        }
    }

    private static boolean isSoulSufficient(LivingEntity user) {
        double max = HunPoHelper.getMaxAmount(user);
        if (max <= 0) {
            return false;
        }
        double current = HunPoHelper.getAmount(user);
        return (current / max) >= SOUL_ACTIVE_RATIO;
    }

    private static void applySoulDamage(
        LivingEntity attacker,
        LivingEntity target,
        double amount
    ) {
        if (amount <= 0) {
            return;
        }
        double targetSoul = HunPoHelper.getAmount(target);
        if (targetSoul > 0) {
            HunPoHelper.modify(target, -amount);
            HunPoHelper.checkAndKill(target);
        } else {
            target.hurt(attacker.damageSources().magic(), (float) amount);
        }
    }

    private static boolean isAlly(LivingEntity owner, LivingEntity target) {
        if (target.isAlliedTo(owner)) {
            return true;
        }
        if (target instanceof TamableAnimal pet && pet.getOwner() == owner) {
            return true;
        }
        return false;
    }

    private static void spawnCracks(
        ServerLevel serverLevel,
        Vec3 center,
        double radius
    ) {
        BlockPos basePos = BlockPos.containing(
            center.x,
            center.y - CRACK_Y_OFFSET,
            center.z
        );
        BlockState state = serverLevel.getBlockState(basePos.below());
        if (state.isAir()) {
            state = serverLevel.getBlockState(basePos);
        }

        BlockParticleOption particle = new BlockParticleOption(
            ParticleTypes.BLOCK,
            state
        );

        for (int i = 0; i < CRACK_RING_SAMPLES; i++) {
            double angle = (Math.PI * 2.0 * i) / CRACK_RING_SAMPLES;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            serverLevel.sendParticles(
                particle,
                x,
                center.y + CRACK_Y_OFFSET,
                z,
                2,
                CRACK_SPREAD_XZ,
                CRACK_SPREAD_Y,
                CRACK_SPREAD_XZ,
                CRACK_SPEED
            );
        }
    }

    private static void applyKnockbackImmunity(LivingEntity user) {
        AttributeInstance attr = user.getAttribute(
            Attributes.KNOCKBACK_RESISTANCE
        );
        if (attr == null) {
            return;
        }
        AttributeModifier existing = attr.getModifier(KNOCKBACK_MODIFIER_ID);
        if (existing == null || existing.amount() != 1.0) {
            if (existing != null) {
                attr.removeModifier(KNOCKBACK_MODIFIER_ID);
            }
            attr.addTransientModifier(
                new AttributeModifier(
                    KNOCKBACK_MODIFIER_ID,
                    1.0,
                    AttributeModifier.Operation.ADD_VALUE
                )
            );
        }
    }

    private static void removeKnockbackImmunity(LivingEntity user) {
        AttributeInstance attr = user.getAttribute(
            Attributes.KNOCKBACK_RESISTANCE
        );
        if (attr != null) {
            attr.removeModifier(KNOCKBACK_MODIFIER_ID);
        }
    }

    private static int getKnockbackTicks(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return 0;
        }
        return data.copyTag().getInt(TAG_KNOCKBACK_TICKS);
    }

    private static void setKnockbackTicks(ItemStack stack, int ticks) {
        net.minecraft.nbt.CompoundTag tag = stack
            .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag();
        tag.putInt(TAG_KNOCKBACK_TICKS, Math.max(0, ticks));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static double getMetaDouble(
        NianTouData.Usage usage,
        String key,
        double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private static float getMetaFloat(
        NianTouData.Usage usage,
        String key,
        float defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Float.parseFloat(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private static int getMetaInt(
        NianTouData.Usage usage,
        String key,
        int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
