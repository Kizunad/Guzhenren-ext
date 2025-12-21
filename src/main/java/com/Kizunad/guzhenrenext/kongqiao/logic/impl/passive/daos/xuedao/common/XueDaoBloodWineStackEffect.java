package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 血凝剑蛊被动：攻击积攒“血酒”进度（写入物品 NBT，供主动饮用）。
 * <p>
 * 说明：该被动本身不直接加伤，而是把战斗行为转化为后续爆发窗口。
 * </p>
 */
public class XueDaoBloodWineStackEffect implements IGuEffect {

    public static final String KEY_BLOOD_WINE =
        "GuzhenrenExtXueDao_BloodWine";

    private static final String META_WINE_GAIN_PER_DAMAGE =
        "wine_gain_per_damage";
    private static final String META_MAX_WINE = "max_wine";

    private static final double DEFAULT_GAIN_PER_DAMAGE = 1.0;
    private static final double DEFAULT_MAX_WINE = 1000.0;

    private final String usageId;

    public XueDaoBloodWineStackEffect(final String usageId) {
        this.usageId = usageId;
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (attacker == null || usageInfo == null || stack == null) {
            return damage;
        }
        if (attacker.level().isClientSide()) {
            return damage;
        }
        if (damage <= 0.0F) {
            return damage;
        }

        final double baseRate = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_WINE_GAIN_PER_DAMAGE,
                DEFAULT_GAIN_PER_DAMAGE
            )
        );
        final double maxWine = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_MAX_WINE,
                DEFAULT_MAX_WINE
            )
        );
        if (baseRate <= 0.0 || maxWine <= 0.0) {
            return damage;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(attacker, DaoHenHelper.DaoType.XUE_DAO)
        );
        final double gain = damage * baseRate * selfMultiplier;
        if (gain <= 0.0) {
            return damage;
        }

        final CompoundTag tag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        final double current = Math.max(0.0, tag.getDouble(KEY_BLOOD_WINE));
        final double next = Math.min(maxWine, current + gain);
        if (Double.compare(current, next) != 0) {
            tag.putDouble(KEY_BLOOD_WINE, next);
            ItemStackCustomDataHelper.setCustomDataTag(stack, tag);
        }

        return damage;
    }
}
