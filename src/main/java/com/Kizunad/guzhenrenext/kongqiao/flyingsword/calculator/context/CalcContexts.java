package com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.context;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordSelectionAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment;
import net.minecraft.world.entity.player.Player;

/**
 * 计算上下文入口：按需从实体/主人收集必要数据。
 * <p>
 * Phase 2：最小实现，仅提供 owner/selection/storage 占位，后续可扩展领域/道痕/器官等数据。
 * </p>
 */
public record CalcContexts(
    FlyingSwordEntity sword,
    Player owner,
    FlyingSwordSelectionAttachment selection,
    FlyingSwordStorageAttachment storage
) {

    public static CalcContexts from(FlyingSwordEntity sword) {
        if (sword == null) {
            return new CalcContexts(null, null, null, null);
        }
        Player owner = sword.getOwner() instanceof Player p ? p : null;
        if (owner == null) {
            return new CalcContexts(sword, null, null, null);
        }
        var selection = com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments.getFlyingSwordSelection(owner);
        var storage = com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments.getFlyingSwordStorage(owner);
        return new CalcContexts(sword, owner, selection, storage);
    }
}
