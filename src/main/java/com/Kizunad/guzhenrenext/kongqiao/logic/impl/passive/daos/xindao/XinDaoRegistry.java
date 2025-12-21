package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xindao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoActiveSelfRecoveryEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoSustainedResourceRegenEffect;

public final class XinDaoRegistry {

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.XIN_DAO;

    private XinDaoRegistry() {}

    public static void registerAll() {
        registerShuChong();
    }

    private static void registerShuChong() {
        final String passive = "guzhenren:shuchong_passive_bookworm_insight";
        final String active = "guzhenren:shuchong_active_bookworm_epiphany";

        GuEffectRegistry.register(new DaoSustainedResourceRegenEffect(passive, DAO_TYPE));
        GuEffectRegistry.register(
            new DaoActiveSelfRecoveryEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active)
            )
        );
    }
}

