package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordAIMode;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 飞剑生成与恢复工具（最小版）。
 */
public final class FlyingSwordSpawner {

    private FlyingSwordSpawner() {}

    @Nullable
    public static FlyingSwordEntity spawnBasic(
        ServerLevel level,
        Player owner
    ) {
        if (level == null || owner == null) {
            return null;
        }

        FlyingSwordEntity sword = FlyingSwordEntities.FLYING_SWORD.get().create(
            level
        );
        if (sword == null) {
            return null;
        }

        sword.setPos(
            owner.getX(),
            owner.getY() +
                owner.getBbHeight() * FlyingSwordConstants.OWNER_HEIGHT_ORBIT,
            owner.getZ()
        );
        sword.setOwner(owner);
        sword.setAIMode(SwordAIMode.ORBIT);

        ItemStack main = owner.getMainHandItem();
        if (main != null && !main.isEmpty()) {
            sword.setDisplayItemStack(main);
        } else {
            sword.setDisplayItemStack(new ItemStack(Items.IRON_SWORD));
        }

        if (!level.addFreshEntity(sword)) {
            return null;
        }

        return sword;
    }

    @Nullable
    public static FlyingSwordEntity restoreFromStorage(
        ServerLevel level,
        Player owner,
        FlyingSwordStorageAttachment.RecalledSword recalled
    ) {
        if (level == null || owner == null || recalled == null) {
            return null;
        }
        if (recalled.itemWithdrawn) {
            return null;
        }

        FlyingSwordEntity sword = FlyingSwordEntities.FLYING_SWORD.get().create(
            level
        );
        if (sword == null) {
            return null;
        }

        sword.setPos(
            owner.getX(),
            owner.getY() +
                owner.getBbHeight() * FlyingSwordConstants.OWNER_HEIGHT_ORBIT,
            owner.getZ()
        );
        sword.setOwner(owner);
        sword.setAIMode(SwordAIMode.ORBIT);

        // 属性/耐久/经验：Phase 2 最小恢复
        try {
            sword.readAttributesFromTag(recalled.attributes);
        } catch (Exception ignored) {}

        // 展示物品：尝试从 NBT 恢复。
        try {
            if (
                recalled.displayItem != null && !recalled.displayItem.isEmpty()
            ) {
                ItemStack stack = ItemStack.parseOptional(
                    level.registryAccess(),
                    recalled.displayItem
                );
                if (!stack.isEmpty()) {
                    sword.setDisplayItemStack(stack);
                }
            }
        } catch (Exception ignored) {}

        if (!level.addFreshEntity(sword)) {
            return null;
        }

        return sword;
    }
}
