package com.Kizunad.guzhenrenext.bastion.guardian;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 基地守卫的“阵营/交战”统一裁决规则。
 * <p>
 * 规则：
 * <ul>
 *   <li>同一基地（bastionId 相同）守卫永不互殴。</li>
 *   <li>不同基地的守卫：仅在“距离接近（<=96）”或“光环重叠”时允许互殴。</li>
 * </ul>
 * </p>
 */
public final class BastionGuardianCombatRules {

    private BastionGuardianCombatRules() {
    }

    /**
     * 守卫交战距离阈值（方块）。
     */
    public static final int MAX_GUARDIAN_ENGAGE_DISTANCE = 96;

    private static final long MAX_GUARDIAN_ENGAGE_DISTANCE_SQ =
        (long) MAX_GUARDIAN_ENGAGE_DISTANCE * MAX_GUARDIAN_ENGAGE_DISTANCE;

    public static boolean isGuardian(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }
        return BastionGuardianData.isGuardian(entity);
    }

    @Nullable
    public static UUID getBastionId(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }
        return BastionGuardianData.getBastionId(entity);
    }

    public static boolean isSameBastion(@Nullable Entity a, @Nullable Entity b) {
        UUID idA = getBastionId(a);
        if (idA == null) {
            return false;
        }
        UUID idB = getBastionId(b);
        return idA.equals(idB);
    }

    /**
     * 是否允许 a 对 b 造成/尝试造成伤害。
     * <p>
     * 注意：该规则只处理“守卫 vs 守卫”情形。非守卫目标应走原版/其他规则。</p>
     */
    public static boolean canGuardianDamage(ServerLevel level, Entity attacker, Entity victim) {
        if (!isGuardian(attacker) || !isGuardian(victim)) {
            return true;
        }

        // 同一基地：永不互殴
        if (isSameBastion(attacker, victim)) {
            return false;
        }

        // 近距离：允许交战
        long distSq = (long) attacker.blockPosition().distSqr(victim.blockPosition());
        if (distSq <= MAX_GUARDIAN_ENGAGE_DISTANCE_SQ) {
            return true;
        }

        // 光环重叠：允许交战（即便超过 96 格）
        UUID bastionA = getBastionId(attacker);
        UUID bastionB = getBastionId(victim);
        if (bastionA == null || bastionB == null) {
            return false;
        }

        return areBastionAurasOverlapping(level, bastionA, bastionB);
    }

    /**
     * 判断两个基地光环是否重叠。
     * <p>
     * 采用“核心距离 <= 半径之和”的判定。
     * 该判定不依赖实体当前位置，避免频繁扫描复杂区域。</p>
     */
    public static boolean areBastionAurasOverlapping(ServerLevel level, UUID bastionA, UUID bastionB) {
        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData a = savedData.getBastion(bastionA);
        BastionData b = savedData.getBastion(bastionB);
        if (a == null || b == null) {
            return false;
        }

        BlockPos coreA = a.corePos();
        BlockPos coreB = b.corePos();
        int radiusA = a.getAuraRadius();
        int radiusB = b.getAuraRadius();

        long sum = (long) radiusA + radiusB;
        long maxDistSq = sum * sum;
        long distSq = (long) coreA.distSqr(coreB);
        return distSq <= maxDistSq;
    }

    /**
     * 从伤害源中解析“真正攻击者”。
     * <p>
     * 对于投射物/间接伤害，优先使用 DamageSource#getEntity()（通常为 owner）。
     * 若为空则退回 direct entity。</p>
     */
    @Nullable
    public static Entity resolveAttacker(@Nullable DamageSource source) {
        if (source == null) {
            return null;
        }
        Entity owner = source.getEntity();
        if (owner != null) {
            return owner;
        }
        return source.getDirectEntity();
    }

    /**
     * 清理非法目标：同基地/越界交战。
     */
    public static void clearInvalidTarget(ServerLevel level, LivingEntity self, @Nullable LivingEntity target) {
        if (!(self instanceof net.minecraft.world.entity.Mob mob)) {
            return;
        }
        if (target == null || target.isRemoved() || !target.isAlive()) {
            return;
        }

        if (!isGuardian(self) || !isGuardian(target)) {
            return;
        }

        if (!canGuardianDamage(level, self, target)) {
            mob.setTarget(null);
        }
    }
}
