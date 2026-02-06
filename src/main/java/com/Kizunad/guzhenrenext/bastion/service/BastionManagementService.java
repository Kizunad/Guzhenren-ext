package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.menu.BastionManagementMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * 基地管理菜单服务：负责在服务端打开 Bastion 管理界面。
 */
public final class BastionManagementService {

    private static final Component TITLE =
        Component.translatable("screen.guzhenrenext.bastion_management.title");

    private BastionManagementService() {
    }

    /**
     * 打开基地管理菜单。
     *
     * @param level    服务端世界
     * @param player   打开界面的玩家
     * @param bastion  目标基地数据
     * @param isRemote 是否为远程模式（令牌打开）
     */
    public static void openManagementMenu(
        ServerLevel level,
        ServerPlayer player,
        BastionData bastion,
        boolean isRemote
    ) {
        if (level.isClientSide()) {
            return;
        }
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
                return new BastionManagementMenu(
                    containerId,
                    playerInventory,
                    bastion.id(),
                    isRemote
                );
            }
        };
        player.openMenu(
            provider,
            buf -> BastionManagementMenu.writeInitialData(buf, bastion, isRemote)
        );
    }
}
