package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 小魂蛊逻辑：被动回复魂魄。
 */
public class XiaoHunGuEffect implements IGuEffect {

    private static final Logger LOGGER = LoggerFactory.getLogger(XiaoHunGuEffect.class);
    public static final String USAGE_ID = "guzhenren:xiaohungu_passive";
    private static final double DEFAULT_REGEN = 2.0; // 每秒回复 2 点

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onSecond(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        double amount = DEFAULT_REGEN;
        
        if (usageInfo.metadata() != null && usageInfo.metadata().containsKey("regen")) {
            try {
                amount = Double.parseDouble(usageInfo.metadata().get("regen"));
            } catch (NumberFormatException ignored) {
                // 使用默认值
            }
        }

        double current = HunPoHelper.getAmount(user);
        double max = HunPoHelper.getMaxAmount(user);

        if (current < max) {
            HunPoHelper.modify(user, amount);
            // LOGGER.debug("小魂蛊为 {} 回复了 {} 点魂魄", user.getName().getString(), amount);
        }
    }
}
