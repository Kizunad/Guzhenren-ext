package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 三转鬼炎蛊：领域型攻防一体。
 * <p>
 * 1. 正常状态：
 *    - 消耗真元。
 *    - 增加盔甲值。
 *    - 周围生成青色羊毛鬼火，对敌人造成 魂道+火焰 伤害。
 * 2. 停滞状态 (被重创后)：
 *    - 消耗魂魄 (1点/s)。
 *    - 失去盔甲值和伤害能力。
 *    - 持续 60s 后恢复。
 * </p>
 */
public class GuiYanGuEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:guiyangu_passive_shield";

    // NBT Keys
    public static final String NBT_STASIS_TIMER = "GuiYanStasisTimer";

    // 配置常量
    private static final double DEFAULT_BASE_COST = 3840.0; // 3转1阶约 5.0/s
    private static final double DEFAULT_ARMOR_BONUS = 8.0; // 增加的盔甲值
    private static final double DEFAULT_AOE_DAMAGE = 20.0; // 总伤害
    private static final double DEFAULT_AOE_RADIUS = 4.0; // 半径
    private static final double STASIS_SOUL_COST = 1.0; // 停滞期魂魄消耗
    private static final int STASIS_SMOKE_COUNT = 5;
    private static final double STASIS_SMOKE_HEIGHT = 1.0;
    private static final double STASIS_SMOKE_SPREAD_XZ = 0.3;
    private static final double STASIS_SMOKE_SPREAD_Y = 0.5;
    private static final double STASIS_SMOKE_SPEED = 0.05;
    private static final int BURN_DURATION_SECONDS = 3;
    private static final int PARTICLE_RING_COUNT = 5;
    private static final int PARTICLE_ROTATE_PERIOD_TICKS = 60;
    private static final double PARTICLE_ROTATE_DIVISOR = 10.0;
    private static final double PARTICLE_VERTICAL_BASE = 1.0;
    private static final double PARTICLE_FLOAT_SPEED = 0.1;
    private static final double PARTICLE_FLOAT_AMPLITUDE = 0.5;
    private static final double FLAME_SPREAD = 0.1;
    private static final double FLAME_SPEED = 0.02;

    // Attribute Modifier Id
    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.parse("guzhenren:guiyangu_armor");

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onUnequip(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
        removeAttribute(user);
    }

    @Override
    public void onSecond(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        Level level = user.level();

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            removeAttribute(user);
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }

        // 1. 检查停滞状态
        int stasisTimer = getStasisTimer(stack);

        if (stasisTimer > 0) {
            // --- 停滞期逻辑 ---
            handleStasis(user, stack, stasisTimer);
            // 确保从激活列表中移除
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }

        // --- 正常逻辑 ---

        // 2. 消耗真元
        double baseCost = getMetaDouble(
            usageInfo,
            "zhenyuan_base_cost",
            DEFAULT_BASE_COST
        );
        double realCost = ZhenYuanHelper.calculateGuCost(user, baseCost);

        if (!ZhenYuanHelper.hasEnough(user, realCost)) {
            // 真元不足，移除属性修饰并返回
            removeAttribute(user);
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }
        ZhenYuanHelper.modify(user, -realCost);

        // 标记为激活 (用于事件监听器快速查询)
        KongqiaoAttachments.getActivePassives(user).add(USAGE_ID);

        // 3. 应用盔甲属性
        double armorBonus = getMetaDouble(
            usageInfo,
            "armor_bonus",
            DEFAULT_ARMOR_BONUS
        );
        double armorMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUN_DAO
        );
        applyAttribute(user, armorBonus * armorMultiplier);

        // 4. 视觉特效 (青色羊毛鬼火)
        double radius = getMetaDouble(
            usageInfo,
            "aoe_radius",
            DEFAULT_AOE_RADIUS
        );
        spawnGhostWoolParticles(user, radius);

        // 5. AOE 伤害
        double damageAmount = getMetaDouble(
            usageInfo,
            "damage_amount",
            DEFAULT_AOE_DAMAGE
        );
        applyAoeDamage(user, radius, damageAmount);
    }

    // --- 停滞期处理 ---
    private void handleStasis(LivingEntity user, ItemStack stack, int timer) {
        // 移除属性加成
        removeAttribute(user);

        // 扣除魂魄
        HunPoHelper.modify(user, -STASIS_SOUL_COST);

        // 减少计时器
        setStasisTimer(stack, timer - 1);

        // 视觉提示：冒烟（表示熄灭/受损）
        if (user.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                user.getX(),
                user.getY() + STASIS_SMOKE_HEIGHT,
                user.getZ(),
                STASIS_SMOKE_COUNT,
                STASIS_SMOKE_SPREAD_XZ,
                STASIS_SMOKE_SPREAD_Y,
                STASIS_SMOKE_SPREAD_XZ,
                STASIS_SMOKE_SPEED
            );
        }
    }

    // --- AOE 伤害逻辑 ---
    private void applyAoeDamage(
        LivingEntity attacker,
        double radius,
        double amount
    ) {
        if (attacker.level().isClientSide) {
            return;
        }

        AABB area = attacker.getBoundingBox().inflate(radius, 2.0, radius);
        List<LivingEntity> targets = attacker
            .level()
            .getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity target : targets) {
            if (target == attacker) {
                continue;
            }

            // 排除宠物和队友
            if (isAlly(attacker, target)) {
                continue;
            }

            // 伤害计算 (混合伤害：一半魂道，一半火焰)
            // 这里简单处理为两次伤害，或者一次自定义伤害。为了触发火焰效果，使用 fire 伤害源。
            float halfDmg = (float) (amount / 2.0);
            double soulMultiplier = DaoHenCalculator.calculateMultiplier(
                attacker,
                target,
                DaoHenHelper.DaoType.HUN_DAO
            );
            float soulDamage = (float) (halfDmg * soulMultiplier);

            // 1. 魂道伤害 (利用之前的 Helper，如果有) 或 魔法伤害模拟
            // target.hurt(attacker.damageSources().indirectMagic(attacker, attacker), halfDmg);
            HunPoHelper.modify(target, -soulDamage); // 魂道部分受道痕增幅

            // 2. 火焰伤害
            // 使用带来源的伤害源，保证中立生物能识别为玩家/施法者攻击
            target.hurt(attacker.damageSources().mobAttack(attacker), halfDmg);
            target.igniteForSeconds(BURN_DURATION_SECONDS); // 灼烧
        }
    }

    private boolean isAlly(LivingEntity owner, LivingEntity target) {
        if (target.isAlliedTo(owner)) {
            return true;
        }
        if (target instanceof TamableAnimal pet && pet.getOwner() == owner) {
            return true;
        }
        return false;
    }

    // --- 属性修饰逻辑 ---
    private void applyAttribute(LivingEntity user, double amount) {
        AttributeInstance attr = user.getAttribute(Attributes.ARMOR);
        if (attr != null) {
            AttributeModifier modifier = attr.getModifier(ARMOR_MODIFIER_ID);
            if (modifier == null || modifier.amount() != amount) {
                if (modifier != null) {
                    attr.removeModifier(ARMOR_MODIFIER_ID);
                }
                attr.addTransientModifier(
                    new AttributeModifier(
                        ARMOR_MODIFIER_ID,
                        amount,
                        AttributeModifier.Operation.ADD_VALUE
                    )
                );
            }
        }
    }

    private void removeAttribute(LivingEntity user) {
        AttributeInstance attr = user.getAttribute(Attributes.ARMOR);
        if (attr != null && attr.getModifier(ARMOR_MODIFIER_ID) != null) {
            attr.removeModifier(ARMOR_MODIFIER_ID);
        }
    }

    // --- 视觉特效 ---
    private void spawnGhostWoolParticles(LivingEntity user, double radius) {
        if (user.level() instanceof ServerLevel serverLevel) {
            // 围绕玩家生成一圈青色羊毛粒子
            int particleCount = PARTICLE_RING_COUNT;
            double angleIncrement = (2 * Math.PI) / particleCount;

            // 随时间旋转
            double timeOffset =
                (user.tickCount % PARTICLE_ROTATE_PERIOD_TICKS) /
                PARTICLE_ROTATE_DIVISOR;

            for (int i = 0; i < particleCount; i++) {
                double angle = i * angleIncrement + timeOffset;
                double px = user.getX() + Math.cos(angle) * radius;
                double pz = user.getZ() + Math.sin(angle) * radius;
                double py =
                    user.getY() +
                    PARTICLE_VERTICAL_BASE +
                    Math.sin(user.tickCount * PARTICLE_FLOAT_SPEED + i) *
                    PARTICLE_FLOAT_AMPLITUDE; // 上下浮动

                serverLevel.sendParticles(
                    new ItemParticleOption(
                        ParticleTypes.ITEM,
                        new ItemStack(Items.CYAN_WOOL)
                    ),
                    px,
                    py,
                    pz,
                    0, // count 0 means strictly configure speed/motion
                    0,
                    0,
                    0,
                    0 // No motion
                );

                // 伴随魂火粒子
                serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    px,
                    py,
                    pz,
                    1,
                    FLAME_SPREAD,
                    FLAME_SPREAD,
                    FLAME_SPREAD,
                    FLAME_SPEED
                );
            }
        }
    }

    private double getMetaDouble(
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

    private int getStasisTimer(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return 0;
        }
        CompoundTag tag = data.copyTag();
        return tag.getInt(NBT_STASIS_TIMER);
    }

    private void setStasisTimer(ItemStack stack, int value) {
        CompoundTag tag = stack
            .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag();
        tag.putInt(NBT_STASIS_TIMER, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
