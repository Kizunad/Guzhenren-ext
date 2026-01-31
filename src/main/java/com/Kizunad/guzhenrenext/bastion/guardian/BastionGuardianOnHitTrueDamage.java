package com.Kizunad.guzhenrenext.bastion.guardian;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * 守卫近战“穿甲/真伤”补偿。
 * <p>
 * 背景：玩家体系存在大量护甲/抗性/减伤，单纯提高 ATTACK_DAMAGE 仍可能被压到很低。
 * 因此给守卫的“普攻造成伤害”附加一段 magic 伤害（受抗性影响，但不吃护甲）。
 * </p>
 * <p>
 * 约束：
 * <ul>
 *   <li>只在守卫造成的 mobAttack 伤害上触发。</li>
 *   <li>防止递归：附加伤害使用 magic，并在事件中忽略 magic 来源。</li>
 * </ul>
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BastionGuardianOnHitTrueDamage {

    private BastionGuardianOnHitTrueDamage() {
    }

    private static final class Config {
        static final float BASE_RATIO = 0.25f;
        static final float LI_DAO_RATIO = 0.35f;
        static final float MAX_RATIO = 0.6f;

        static final float MIN_TRIGGER_DAMAGE = 1.0f;

        private Config() {
        }
    }

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        if (event.getNewDamage() < Config.MIN_TRIGGER_DAMAGE) {
            return;
        }

        DamageSource source = event.getSource();
        if (source == null) {
            return;
        }

        Entity attacker = source.getEntity();
        if (!(attacker instanceof Mob mob)) {
            return;
        }
        if (!BastionGuardianData.isGuardian(mob)) {
            return;
        }

        BastionDao dao = resolveDao(level, mob);
        float ratio = computeRatio(dao);

        float extra = event.getNewDamage() * ratio;
        if (extra <= 0.0f) {
            return;
        }

        // 直接附加一段 magic 伤害（不改写原伤害，以免影响击退/仇恨等原版逻辑）。
        // 防递归：如果当前伤害源本身就是 magic，则不再附加。
        if (isMagicDamageSource(source)) {
            return;
        }
        victim.hurt(mob.damageSources().magic(), extra);
    }

    private static boolean isMagicDamageSource(DamageSource source) {
        if (source == null) {
            return false;
        }
        // 只要伤害来自我们的 magic source（entity 为空、direct 为空），就不再附加。
        return source.getEntity() == null && source.getDirectEntity() == null;
    }

    private static float computeRatio(BastionDao dao) {
        float ratio = Config.BASE_RATIO;
        if (dao == BastionDao.LI_DAO) {
            ratio = Config.LI_DAO_RATIO;
        }
        return Math.min(Config.MAX_RATIO, Math.max(0.0f, ratio));
    }

    private static BastionDao resolveDao(ServerLevel level, Mob guardian) {
        UUID bastionId = BastionGuardianData.getBastionId(guardian);
        if (bastionId == null) {
            return BastionDao.ZHI_DAO;
        }
        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData bastion = savedData.getBastion(bastionId);
        if (bastion == null) {
            return BastionDao.ZHI_DAO;
        }
        return bastion.primaryDao();
    }
}
