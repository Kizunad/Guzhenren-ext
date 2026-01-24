package com.Kizunad.guzhenrenext.bastion.skill;

import com.Kizunad.guzhenrenext.bastion.skill.impl.special.HighTierZhiDaoCalculationAuraEffect;
import com.Kizunad.guzhenrenext.bastion.skill.impl.special.HighTierZhiDaoFateThreadsEffect;
import com.Kizunad.guzhenrenext.bastion.skill.impl.zhidao.ZhiDaoDestinyRewriteEffect;
import com.Kizunad.guzhenrenext.bastion.skill.impl.zhidao.ZhiDaoFateManipulationEffect;
import com.Kizunad.guzhenrenext.bastion.skill.impl.zhidao.ZhiDaoOmniscienceDomainEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地高转技能/特效注册入口。
 * <p>
 * 将 Bastion 专属的 high_tier.skills / special_effects 注册到 {@link ShazhaoEffectRegistry}。
 * </p>
 */
public final class BastionHighTierEffectRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BastionHighTierEffectRegistry.class
    );

    private static boolean registered = false;

    private BastionHighTierEffectRegistry() {
    }

    /**
     * 注册所有 Bastion 高转效果。
     */
    public static void registerAll() {
        if (registered) {
            return;
        }

        // 高转技能（shazhao_active/shazhao_passive）
        ShazhaoEffectRegistry.register(new ZhiDaoFateManipulationEffect());
        ShazhaoEffectRegistry.register(new ZhiDaoOmniscienceDomainEffect());
        ShazhaoEffectRegistry.register(new ZhiDaoDestinyRewriteEffect());

        // special_effects（不属于 shazhao_ 前缀，但同样走 ShazhaoEffectRegistry 映射）
        ShazhaoEffectRegistry.register(new HighTierZhiDaoCalculationAuraEffect());
        ShazhaoEffectRegistry.register(new HighTierZhiDaoFateThreadsEffect());

        registered = true;
        LOGGER.info("BastionHighTierEffectRegistry 已注册 Bastion 高转技能/特效");
    }
}
