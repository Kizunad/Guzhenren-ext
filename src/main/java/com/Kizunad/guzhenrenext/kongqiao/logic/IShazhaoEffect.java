package com.Kizunad.guzhenrenext.kongqiao.logic;

import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.world.entity.LivingEntity;

/**
 * 杀招效果逻辑接口。
 * <p>
 * 用于承载“杀招”的被动/主动效果实现，并由 {@link ShazhaoEffectRegistry} 统一注册。
 * </p>
 */
public interface IShazhaoEffect {

    /**
     * 获取此效果对应的杀招 ID。
     * 必须与 ShazhaoData JSON 中的 shazhaoID 一致。
     */
    String getShazhaoId();

    /**
     * 被动效果：每秒调用一次（由运行服务控制）。
     *
     * @param user 使用者
     * @param data 杀招配置数据
     */
    default void onSecond(LivingEntity user, ShazhaoData data) {
    }

    /**
     * 当杀招未解锁或失效时调用，用于撤销属性修饰等效果。
     *
     * @param user 使用者
     */
    default void onInactive(LivingEntity user) {
    }
}
