package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;

/**
 * 狼魂蛊：被动·夜行者。
 * <p>
 * 夜晚或低光照环境下，提升移动速度，体现“夜战/游击”的机动性。
 * </p>
 */
public class LangHunGuNightWalkerEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:langhungu_passive_nightwalker";

    private static final double DEFAULT_SPEED_BONUS = 0.2;
    private static final int DEFAULT_LOW_LIGHT_THRESHOLD = 7;
    private static final ResourceLocation SPEED_MODIFIER_ID =
        ResourceLocation.parse("guzhenren:langhungu_nightwalker_speed");

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onTick(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            removeSpeedModifier(user);
            return;
        }
        if (shouldBoost(user, usageInfo)) {
            double bonus = getMetaDouble(usageInfo, "speed_bonus", DEFAULT_SPEED_BONUS);
            double multiplier = DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.HUN_DAO);
            applySpeedModifier(user, bonus * multiplier);
            return;
        }
        removeSpeedModifier(user);
    }

    @Override
    public void onUnequip(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        removeSpeedModifier(user);
    }

    private boolean shouldBoost(LivingEntity user, NianTouData.Usage usageInfo) {
        Level level = user.level();
        boolean isNight = level.dimensionType().hasSkyLight() && level.isNight();
        int threshold = getMetaInt(usageInfo, "low_light_threshold", DEFAULT_LOW_LIGHT_THRESHOLD);
        BlockPos pos = user.blockPosition();
        int brightness = level.getMaxLocalRawBrightness(pos);
        boolean lowLight = brightness <= threshold;
        return isNight || lowLight;
    }

    private void applySpeedModifier(LivingEntity user, double amount) {
        AttributeInstance attr = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) {
            return;
        }
        AttributeModifier modifier = attr.getModifier(SPEED_MODIFIER_ID);
        if (modifier == null || Double.compare(modifier.amount(), amount) != 0) {
            if (modifier != null) {
                attr.removeModifier(SPEED_MODIFIER_ID);
            }
            attr.addTransientModifier(
                new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    amount,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                )
            );
        }
    }

    private void removeSpeedModifier(LivingEntity user) {
        AttributeInstance attr = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null && attr.getModifier(SPEED_MODIFIER_ID) != null) {
            attr.removeModifier(SPEED_MODIFIER_ID);
        }
    }

    private static double getMetaDouble(NianTouData.Usage usage, String key, double defaultValue) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static int getMetaInt(NianTouData.Usage usage, String key, int defaultValue) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}
