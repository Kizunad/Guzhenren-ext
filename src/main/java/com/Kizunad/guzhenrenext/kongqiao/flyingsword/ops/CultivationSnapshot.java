package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops;

import com.Kizunad.guzhenrenext.guzhenrenBridge.EntityHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.QiyunHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import net.minecraft.world.entity.LivingEntity;

public record CultivationSnapshot(
    double zhenyuan,
    double niantou,
    double hunpo,
    double qiyun,
    int guMasterRank
) {

    public static CultivationSnapshot capture(final LivingEntity entity) {
        return new CultivationSnapshot(
            ZhenYuanHelper.getAmount(entity),
            NianTouHelper.getAmount(entity),
            HunPoHelper.getAmount(entity),
            QiyunHelper.getAmount(entity),
            EntityHelper.getGuMasterRank(entity)
        );
    }

    public static CultivationSnapshot of(
        final double zhenyuan,
        final double niantou,
        final double hunpo,
        final double qiyun,
        final int guMasterRank
    ) {
        return new CultivationSnapshot(zhenyuan, niantou, hunpo, qiyun, guMasterRank);
    }
}
