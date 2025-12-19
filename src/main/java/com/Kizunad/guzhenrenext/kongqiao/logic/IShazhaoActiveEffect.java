package com.Kizunad.guzhenrenext.kongqiao.logic;

import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.server.level.ServerPlayer;

/**
 * 杀招主动触发接口。
 * <p>
 * 主动杀招仅在轮盘触发时调用，返回 false 表示条件不足或触发失败。
 * </p>
 */
public interface IShazhaoActiveEffect extends IShazhaoEffect {

    /**
     * 主动触发杀招。
     *
     * @param player 使用者
     * @param data   杀招配置
     * @return true 表示触发成功；false 表示条件不足或失败
     */
    boolean onActivate(ServerPlayer player, ShazhaoData data);
}
