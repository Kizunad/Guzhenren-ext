package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.Comparator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 三转犬魄蛊：被动【代僵】。
 * <p>
 * 奴道蛊师的经典保命手段：用兽群的命换自己的命。
 * <ul>
 *   <li>当玩家受到致死伤害时，若周围存在一只“满血且已驯服、归属玩家”的狼，则强制转移本次致死结果。</li>
 *   <li>狼立即死亡，玩家保留 1 点血存活。</li>
 *   <li>为了避免极端情况下（连锁伤害、同 tick 多次触发）重复消耗，加入短暂冷却。</li>
 * </ul>
 * </p>
 */
public class QuanPuGuSubstituteSacrificeEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:quanpugu_passive_substitute_sacrifice";

    private static final String NBT_COOLDOWN_UNTIL =
        "guzhenrenext_quanpugu_substitute_cooldown_until";

    private static final double DEFAULT_RADIUS = 12.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public float onHurt(
        LivingEntity victim,
        DamageSource source,
        float damage,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (victim.level().isClientSide()) {
            return damage;
        }
        if (!(victim instanceof Player player)) {
            return damage;
        }
        if (!(victim.level() instanceof ServerLevel serverLevel)) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(player);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return damage;
        }

        // 不处理无敌/旁观等状态，保持与原版一致的生命逻辑。
        if (player.isInvulnerableTo(source) || player.isSpectator()) {
            return damage;
        }

        // 只在“将要致死”时触发。这里使用当前血量做判定，避免过早触发。
        if (damage < player.getHealth()) {
            return damage;
        }

        CompoundTag data = player.getPersistentData();
        long gameTime = serverLevel.getGameTime();
        if (data.contains(NBT_COOLDOWN_UNTIL)) {
            long until = data.getLong(NBT_COOLDOWN_UNTIL);
            if (gameTime < until) {
                return damage;
            }
        }

        double baseRadius = getMetaDouble(usageInfo, "radius", DEFAULT_RADIUS);
        double radius = baseRadius
            * DaoHenCalculator.calculateSelfMultiplier(
                player,
                DaoHenHelper.DaoType.NU_DAO
            );

        AABB area = player.getBoundingBox().inflate(radius);
        List<Wolf> wolves = serverLevel.getEntitiesOfClass(
            Wolf.class,
            area,
            wolf ->
                wolf.isAlive()
                    && wolf.isTame()
                    && wolf.getOwner() == player
                    && wolf.getHealth() >= wolf.getMaxHealth()
        );

        Wolf substitute = wolves
            .stream()
            .min(Comparator.comparingDouble(w -> w.distanceToSqr(player)))
            .orElse(null);
        if (substitute == null) {
            return damage;
        }

        // 触发：把“致死结果”转移给狗，玩家保留 1 点血存活。
        // 这里不尝试精确转移伤害值，因为目标明确为“必死”，直接击杀更稳健。
        substitute.hurt(source, Float.MAX_VALUE);
        if (substitute.isAlive()) {
            // 极少数来源可能被实体免疫，兜底用通用伤害再次击杀。
            substitute.hurt(substitute.damageSources().generic(), Float.MAX_VALUE);
        }

        player.setHealth(1.0F);
        data.putLong(
            NBT_COOLDOWN_UNTIL,
            gameTime + getMetaInt(usageInfo, "cooldown_ticks", DEFAULT_COOLDOWN_TICKS)
        );

        // 取消本次伤害：生命已被手动设置为 1，避免出现“未受伤仍满血”的不一致。
        return 0.0F;
    }

    private static double getMetaDouble(
        NianTouData.Usage usage,
        String key,
        double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
                // 念头配置属于数据驱动：无效值回退默认，避免影响服务器稳定性。
            }
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
            } catch (NumberFormatException ignored) {
                // 念头配置属于数据驱动：无效值回退默认，避免影响服务器稳定性。
            }
        }
        return defaultValue;
    }
}
