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
 * 血葫芦被动：按造成的伤害“收集血液”，存入物品 NBT（可供主动消耗）。
 * <p>
 * 说明：这是偏“资源管理/辅助”的血道特色玩法，存储值本身不直接造成强度膨胀，
 * 主动技能会对单次消耗做上限限制，避免一次性回血/回资源过量。
 * </p>
 */
public class XueDaoBloodGourdStorageEffect implements IGuEffect {

    public static final String KEY_BLOOD_STORAGE =
        "GuzhenrenExtXueDao_BloodGourdStorage";

    private static final String META_STORAGE_GAIN_PER_DAMAGE =
        "storage_gain_per_damage";
    private static final String META_MAX_STORAGE = "max_storage";

    private static final double DEFAULT_GAIN_PER_DAMAGE = 3.0;
    private static final double DEFAULT_MAX_STORAGE = 100000.0;

    private final String usageId;

    public XueDaoBloodGourdStorageEffect(final String usageId) {
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
                META_STORAGE_GAIN_PER_DAMAGE,
                DEFAULT_GAIN_PER_DAMAGE
            )
        );
        final double maxStorage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_MAX_STORAGE,
                DEFAULT_MAX_STORAGE
            )
        );
        if (baseRate <= 0.0 || maxStorage <= 0.0) {
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
        final double current = Math.max(0.0, tag.getDouble(KEY_BLOOD_STORAGE));
        final double next = Math.min(maxStorage, current + gain);
        if (Double.compare(current, next) != 0) {
            tag.putDouble(KEY_BLOOD_STORAGE, next);
            ItemStackCustomDataHelper.setCustomDataTag(stack, tag);
        }
        return damage;
    }
}
