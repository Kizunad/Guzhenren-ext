package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.LiuPaiHelper;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordConstants;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.effects.FlyingSwordEffects;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordExpCalculator;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthData;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthTuning;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.integration.domain.SwordSpeedModifiers;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.FlyingSwordCooldownOps;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 飞剑战斗驱动（集中式）。
 * <p>
 * 从 {@code FlyingSwordEntity} 中提取，统一战斗追击与攻击逻辑。
 * </p>
 * <p>
 * Phase 3 更新：
 * <ul>
 *     <li>使用品质/等级系统计算伤害</li>
 *     <li>攻击成功后获取经验</li>
 *     <li>支持击杀/精英/Boss 额外经验</li>
 * </ul>
 * </p>
 */
public final class SwordCombatOps {

    private SwordCombatOps() {}

    /**
     * 执行战斗追击 tick。
     * <p>
     * 驱动飞剑向目标移动，到达攻击距离后尝试攻击。
     * </p>
     *
     * @param sword          飞剑实体
     * @param owner          主人
     * @param target         目标
     * @param chaseSpeedScale 追击速度倍率（相对于 speedMax）
     */
    public static void tickCombatPursuit(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity target,
        double chaseSpeedScale
    ) {
        if (target == null || target.isRemoved() || target.isDeadOrDying()) {
            return;
        }

        // 目标位置（瞄准目标中心偏高处）
        Vec3 targetPos = target
            .position()
            .add(
                0.0,
                target.getBbHeight() * SwordGrowthTuning.TARGET_HEIGHT_RATIO,
                0.0
            );
        Vec3 swordPos = sword.position();
        Vec3 delta = targetPos.subtract(swordPos);

        // 计算追击速度（使用有效速度，包含品质加成）
        FlyingSwordAttributes attrs = sword.getSwordAttributes();
        double speed = Math.max(
            0.0,
            attrs.getEffectiveSpeedMax() * Math.max(0.0, chaseSpeedScale)
        );

        // 应用速度向目标移动
        applyVelocityTowards(sword, delta, speed);

        // 攻击判定
        double attackRange = FlyingSwordConstants.ATTACK_RANGE;
        if (delta.lengthSqr() <= attackRange * attackRange) {
            tryAttack(sword, owner, target);
        }
    }

    /**
     * 尝试攻击目标。
     *
     * @param sword  飞剑实体
     * @param owner  主人（伤害来源）
     * @param target 目标
     */
    public static void tryAttack(
        FlyingSwordEntity sword,
        @Nullable LivingEntity owner,
        LivingEntity target
    ) {
        if (owner == null || owner.isRemoved()) {
            return;
        }
        if (target == null || target.isRemoved() || target.isDeadOrDying()) {
            return;
        }
        if (!FlyingSwordCooldownOps.isAttackReady(sword)) {
            return;
        }
        if (!(sword.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 记录目标攻击前的生命值
        float targetHealthBefore = target.getHealth();

        // 计算伤害（使用品质/等级系统）
        float damage = calculateDamage(sword, owner);

        // 伤害来源：归属于主人的 mobAttack
        DamageSource source = serverLevel.damageSources().mobAttack(owner);

        // 执行攻击
        boolean success = target.hurt(source, damage);
        if (success) {
            // 设置攻击冷却（使用属性系统的冷却值）
            int cooldown = sword.getSwordAttributes().attackCooldown;
            FlyingSwordCooldownOps.setAttackCooldown(sword, cooldown);

            // 计算实际造成的伤害
            float actualDamage = targetHealthBefore - target.getHealth();
            if (actualDamage < 0) {
                actualDamage = damage; // 如果目标死亡，使用预期伤害
            }

            // 检查是否击杀
            boolean isKill = target.isDeadOrDying();

            // 处理攻击成功回调（包含经验获取）
            onAttackSuccess(sword, owner, target, actualDamage, isKill);
        }
    }

    /**
     * 计算飞剑伤害。
     * <p>
     * 使用品质/等级系统计算最终伤害。
     * </p>
     *
     * @param sword 飞剑实体
     * @param owner 主人
     * @return 最终伤害值
     */
    public static float calculateDamage(
        FlyingSwordEntity sword,
        LivingEntity owner
    ) {
        FlyingSwordAttributes attrs = sword.getSwordAttributes();

        // 获取有效伤害（包含品质、等级、临时修正）
        double baseDamage = attrs.getEffectiveDamage();

        // 道痕加成：剑道道痕每点 +1% 伤害，上限 +500%
        double jiandaoHen = DaoHenHelper.getDaoHen(
            owner,
            DaoHenHelper.DaoType.JIAN_DAO
        );
        double daohenBonus = Math.min(
            jiandaoHen * SwordGrowthTuning.DAOHEN_JIANDAO_DAMAGE_COEF,
            SwordGrowthTuning.DAOHEN_DAMAGE_BONUS_CAP
        );

        // 流派加成：剑道流派每点 +2% 伤害，上限 +1000%
        double jiandaoLiupai = LiuPaiHelper.getLiuPai(
            owner,
            LiuPaiHelper.LiuPaiType.JIAN_DAO
        );
        double liupaiBonus = Math.min(
            jiandaoLiupai * SwordGrowthTuning.LIUPAI_JIANDAO_DAMAGE_COEF,
            SwordGrowthTuning.LIUPAI_DAMAGE_BONUS_CAP
        );

        // 应用道痕/流派加成
        baseDamage *= (1.0 + daohenBonus + liupaiBonus);

        // 速度加成（速度越快伤害越高）
        double speedRatio =
            sword.getDeltaMovement().length() / attrs.getEffectiveSpeedMax();
        double speedBonus =
            1.0 +
            Math.min(
                SwordGrowthTuning.SPEED_DAMAGE_BONUS_CAP,
                speedRatio * SwordGrowthTuning.SPEED_DAMAGE_BONUS_COEF
            );

        return (float) (baseDamage * speedBonus);
    }

    /**
     * 攻击成功回调。
     * <p>
     * 处理：
     * <ul>
     *     <li>经验获取</li>
     *     <li>粒子效果</li>
     *     <li>音效</li>
     *     <li>击杀特效</li>
     * </ul>
     * </p>
     *
     * @param sword  飞剑实体
     * @param owner  主人
     * @param target 目标
     * @param damage 实际造成的伤害
     * @param isKill 是否击杀
     */
    private static void onAttackSuccess(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity target,
        float damage,
        boolean isKill
    ) {
        FlyingSwordAttributes attrs = sword.getSwordAttributes();

        // 播放攻击命中特效
        FlyingSwordEffects.playHitEffect(sword, target, damage);

        // 如果击杀，播放击杀特效
        if (isKill) {
            FlyingSwordEffects.playKillEffect(sword, target);
        }

        // 计算基础经验获取
        int baseExpGain = SwordExpCalculator.calculateExpGain(
            damage,
            target,
            isKill,
            attrs.getQuality()
        );

        // 流派经验加成：剑道流派每点 +1% 经验，上限 +500%
        double jiandaoLiupai = LiuPaiHelper.getLiuPai(
            owner,
            LiuPaiHelper.LiuPaiType.JIAN_DAO
        );
        double liupaiExpBonus = Math.min(
            jiandaoLiupai * SwordGrowthTuning.LIUPAI_JIANDAO_EXP_COEF,
            SwordGrowthTuning.LIUPAI_EXP_BONUS_CAP
        );
        int expGain = (int) Math.round(baseExpGain * (1.0 + liupaiExpBonus));

        // 添加经验（自动升级）
        if (expGain > 0) {
            SwordGrowthData.ExpAddResult result = attrs.addExperience(expGain);

            // 如果升级了，播放升级效果
            if (result.levelsGained > 0) {
                onLevelUp(sword, owner, result);
            }
        }
    }

    /**
     * 升级回调。
     *
     * @param sword  飞剑实体
     * @param owner  主人
     * @param result 升级结果
     */
    private static void onLevelUp(
        FlyingSwordEntity sword,
        LivingEntity owner,
        SwordGrowthData.ExpAddResult result
    ) {
        // 播放升级特效（粒子 + 音效）
        FlyingSwordEffects.playLevelUpEffect(
            sword,
            result.levelsGained,
            result.newLevel
        );

        // 发送升级消息给玩家
        if (owner instanceof net.minecraft.world.entity.player.Player player) {
            FlyingSwordAttributes attrs = sword.getSwordAttributes();
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    String.format(
                        "§a[飞剑升级] %s → Lv.%d | 伤害:%.1f 速度:%.2f",
                        attrs.getQuality().getDisplayName(),
                        result.newLevel,
                        attrs.damage,
                        attrs.speedMax
                    )
                ),
                true
            );
        }
    }

    /**
     * 向目标方向应用速度。
     * <p>
     * 封装飞剑的速度插值与移动逻辑。
     * </p>
     */
    private static void applyVelocityTowards(
        FlyingSwordEntity sword,
        Vec3 delta,
        double baseSpeed
    ) {
        if (delta == null) {
            return;
        }

        double distance = delta.length();
        if (distance < FlyingSwordConstants.MIN_DISTANCE) {
            sword.setDeltaMovement(Vec3.ZERO);
            return;
        }

        var attrs = sword.getSwordAttributes();

        // 领域速度加成
        double domainScale = SwordSpeedModifiers.computeDomainSpeedScale(sword);

        // 目标速度（使用有效速度）
        double effectiveMaxSpeed = attrs.getEffectiveSpeedMax() * domainScale;
        double targetSpeed = Math.max(
            baseSpeed,
            Math.min(distance, effectiveMaxSpeed)
        );

        // 期望速度向量
        Vec3 desiredVel = delta.normalize().scale(targetSpeed);

        // 当前速度
        Vec3 current = sword.getDeltaMovement();

        // 加速度插值（使用有效加速度）
        double lerpFactor = Math.min(1.0, attrs.getEffectiveAccel());
        Vec3 newVel = current.lerp(desiredVel, lerpFactor);

        // 速度上限
        if (newVel.length() > effectiveMaxSpeed) {
            newVel = newVel.normalize().scale(effectiveMaxSpeed);
        }

        // 应用速度并移动
        sword.setDeltaMovement(newVel);
        sword.move(
            net.minecraft.world.entity.MoverType.SELF,
            sword.getDeltaMovement()
        );

        // 更新朝向
        if (newVel.lengthSqr() > FlyingSwordConstants.LOOK_EPSILON) {
            sword.setYRot(
                (float) (Math.atan2(newVel.z, newVel.x) *
                        FlyingSwordConstants.RAD_TO_DEG) -
                    FlyingSwordConstants.LOOK_ROTATE_DEG_OFFSET
            );
        }
    }

    /**
     * 检查飞剑是否距离主人过远（用于 HUNT 模式脱缰判定）。
     */
    public static boolean isTooFarFromOwner(
        FlyingSwordEntity sword,
        LivingEntity owner,
        double leashRange
    ) {
        if (owner == null) {
            return true;
        }
        double distSq = sword.distanceToSqr(owner);
        return distSq > leashRange * leashRange;
    }
}
