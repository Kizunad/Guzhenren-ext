package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoOwner;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoSettings;
import com.Kizunad.guzhenrenext.kongqiao.menu.AttackInventoryMenu;
import com.Kizunad.guzhenrenext.kongqiao.menu.GuchongFeedMenu;
import com.Kizunad.guzhenrenext.kongqiao.menu.KongqiaoMenu;
import com.Kizunad.guzhenrenext.kongqiao.validator.TagBasedKongqiaoSlotValidator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 空窍服务入口。
 * <p>
 * 负责创建默认容器、拓展行数以及打开菜单等高层操作。
 * </p>
 */
public final class KongqiaoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        KongqiaoService.class
    );
    private static final Component TITLE = Component.translatable(
        "menu.guzhenrenext.kongqiao"
    );
    private static final Component ATTACK_TITLE = Component.translatable(
        "menu.guzhenrenext.attack_inventory"
    );
    private static final Component FEED_TITLE = Component.translatable(
        "menu.guzhenrenext.guchong_feed"
    );

    private KongqiaoService() {}

    /**
     * 创建一个默认配置的空窍容器。
     */
    public static KongqiaoInventory createInventory() {
        return new KongqiaoInventory(
            new KongqiaoSettings(),
            new TagBasedKongqiaoSlotValidator()
        );
    }

    /**
     * 尝试拓展下一行。
     *
     * @return true 表示拓展成功
     */
    public static boolean expand(KongqiaoOwner owner) {
        boolean changed = owner.getKongqiaoInventory().getSettings().unlockNextRow();
        if (changed) {
            owner.markKongqiaoDirty();
            LOGGER.debug("[Kongqiao] {} 解锁新的空窍行数", owner.getKongqiaoId());
        }
        return changed;
    }

    /**
     * 服务端打开空窍 UI。
     */
    public static void openKongqiaoMenu(
        ServerPlayer player,
        KongqiaoOwner owner
    ) {
        if (player.level().isClientSide()) {
            return;
        }
        KongqiaoInventory inventory = owner.getKongqiaoInventory();
        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return TITLE;
            }

            @Override
            public AbstractContainerMenu createMenu(
                int containerId,
                Inventory playerInventory,
                Player playerEntity
            ) {
                return new KongqiaoMenu(containerId, playerInventory, inventory);
            }
        };
        player.openMenu(
            provider,
            buf -> KongqiaoMenu.writeClientData(inventory, buf)
        );
    }

    /**
     * 打开攻击背包界面。
     */
    public static void openAttackInventoryMenu(
        ServerPlayer player,
        KongqiaoOwner owner
    ) {
        if (player.level().isClientSide()) {
            return;
        }
        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return ATTACK_TITLE;
            }

            @Override
            public AbstractContainerMenu createMenu(
                int containerId,
                Inventory playerInventory,
                Player playerEntity
            ) {
                return new AttackInventoryMenu(
                    containerId,
                    playerInventory,
                    owner.getAttackInventory()
                );
            }
        };
        player.openMenu(provider);
    }

    /**
     * 打开蛊虫喂食界面。
     */
    public static void openGuchongFeedMenu(
        ServerPlayer player,
        KongqiaoOwner owner
    ) {
        if (player.level().isClientSide()) {
            return;
        }
        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return FEED_TITLE;
            }

            @Override
            public AbstractContainerMenu createMenu(
                int containerId,
                Inventory playerInventory,
                Player playerEntity
            ) {
                return new GuchongFeedMenu(
                    containerId,
                    playerInventory,
                    owner.getFeedInventory()
                );
            }
        };
        player.openMenu(provider);
    }

    /**
     * 将玩家背包与攻击背包互换。
     */
    public static void swapAttackInventory(
        ServerPlayer player,
        KongqiaoOwner owner
    ) {
        AttackInventoryService.swapWithPlayerInventory(
            player,
            owner.getAttackInventory()
        );
        owner.markKongqiaoDirty();
    }
}
