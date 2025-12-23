package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao.BianHuaDaoShazhaoAsuraRendEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao.BianHuaDaoShazhaoCopperIronSlamEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao.BianHuaDaoShazhaoEarthBearGrappleEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao.BianHuaDaoShazhaoFoxShadowPounceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao.BianHuaDaoShazhaoGoldenDragonTailSweepEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao.BianHuaDaoShazhaoHeavenDemonDevourFormEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao.BianHuaDaoShazhaoPhantomCloneAssaultEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao.BianHuaDaoShazhaoTurtleShellBashEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao.BianHuaDaoShazhaoUglyStoneSmashEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao.BianHuaDaoShazhaoXuanwuShellQuakeEffect;

/**
 * 变化道杀招逻辑注册（主动 + 被动）。
 */
public final class BianHuaDaoShazhaoRegistry {

    private BianHuaDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoBeastHideCarapaceEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoScaleBackBulwarkEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoBeastBloodRegenEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoSoaringDragonPhantomEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoAsuraWarformEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoBlackStoneUglyHideEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoTurtleBreathRegenEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoPhantomCloneMindRegenEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoGoldenBellDragonLeapEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoHeavenDemonBloodBodyEffect());

        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoCopperIronSlamEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoFoxShadowPounceEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoEarthBearGrappleEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoXuanwuShellQuakeEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoAsuraRendEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoUglyStoneSmashEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoTurtleShellBashEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoPhantomCloneAssaultEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoGoldenDragonTailSweepEffect());
        ShazhaoEffectRegistry.register(new BianHuaDaoShazhaoHeavenDemonDevourFormEffect());
    }
}
