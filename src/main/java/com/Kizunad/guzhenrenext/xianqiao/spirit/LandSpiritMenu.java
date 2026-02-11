package com.Kizunad.guzhenrenext.xianqiao.spirit;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoMenus;
import java.util.UUID;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;

/**
 * 地灵管理菜单（仅数据同步，不包含物品槽）。
 * <p>
 * 本菜单用于同步仙窍基础信息：
 * 坐标、半径、时间流速、下次灾劫倒计时。
 * </p>
 */
public class LandSpiritMenu extends AbstractContainerMenu {

    /** 数据槽：中心 X。 */
    private static final int DATA_CENTER_X = 0;

    /** 数据槽：中心 Y。 */
    private static final int DATA_CENTER_Y = 1;

    /** 数据槽：中心 Z。 */
    private static final int DATA_CENTER_Z = 2;

    /** 数据槽：当前半径。 */
    private static final int DATA_RADIUS = 3;

    /** 数据槽：时间流速（千分比）。 */
    private static final int DATA_TIME_SPEED_PERMILLE = 4;

    /** 数据槽：下次灾劫倒计时（tick，int 上限截断）。 */
    private static final int DATA_TRIBULATION_REMAINING_TICKS = 5;

    /** ContainerData 字段总数。 */
    private static final int DATA_FIELDS = 6;

    /** 时间流速千分比基准。 */
    private static final int PERMILLE_BASE = 1000;

    private final ContainerData data;

    /**
     * 客户端网络构造。
     *
     * @param containerId 容器 id
     * @param inventory 玩家背包
     */
    public LandSpiritMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainerData(DATA_FIELDS));
    }

    private LandSpiritMenu(int containerId, Inventory inventory, ContainerData data) {
        super(XianqiaoMenus.LAND_SPIRIT.get(), containerId);
        this.data = data;
        checkContainerDataCount(data, DATA_FIELDS);
        addDataSlots(data);
    }

    /**
     * 服务端构造：绑定地灵主人与仙窍维度，以实时读取 SavedData。
     *
     * @param containerId 容器 id
     * @param inventory 玩家背包
     * @param ownerUUID 地灵主人 UUID
     * @param apertureLevel 仙窍维度
     */
    public LandSpiritMenu(int containerId, Inventory inventory, UUID ownerUUID, ServerLevel apertureLevel) {
        this(containerId, inventory, createData(ownerUUID, apertureLevel));
    }

    private static ContainerData createData(UUID ownerUUID, ServerLevel apertureLevel) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                ApertureInfo info = ApertureWorldData.get(apertureLevel).getAperture(ownerUUID);
                if (info == null) {
                    return 0;
                }
                return switch (index) {
                    case DATA_CENTER_X -> info.center().getX();
                    case DATA_CENTER_Y -> info.center().getY();
                    case DATA_CENTER_Z -> info.center().getZ();
                    case DATA_RADIUS -> info.currentRadius();
                    case DATA_TIME_SPEED_PERMILLE -> Math.round(info.timeSpeed() * PERMILLE_BASE);
                    case DATA_TRIBULATION_REMAINING_TICKS -> getSafeTribulationRemainingTicks(info, apertureLevel);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // 地灵菜单在 MVP 阶段仅做只读同步，暂不接收客户端反写。
            }

            @Override
            public int getCount() {
                return DATA_FIELDS;
            }
        };
    }

    /**
     * 将灾劫倒计时安全压缩为 int。
     */
    private static int getSafeTribulationRemainingTicks(ApertureInfo info, ServerLevel apertureLevel) {
        long rawTick = info.nextTribulationTick();
        long gameTime = apertureLevel.getGameTime();
        long remainingTicks = rawTick > gameTime ? rawTick - gameTime : rawTick;
        if (remainingTicks <= 0L) {
            return 0;
        }
        return remainingTicks > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remainingTicks;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    /**
     * 获取仙窍中心 X。
     */
    public int getCenterX() {
        return data.get(DATA_CENTER_X);
    }

    /**
     * 获取仙窍中心 Y。
     */
    public int getCenterY() {
        return data.get(DATA_CENTER_Y);
    }

    /**
     * 获取仙窍中心 Z。
     */
    public int getCenterZ() {
        return data.get(DATA_CENTER_Z);
    }

    /**
     * 获取仙窍当前半径。
     */
    public int getRadius() {
        return data.get(DATA_RADIUS);
    }

    /**
     * 获取时间流速千分比（1000 表示 1.0x）。
     */
    public int getTimeSpeedPermille() {
        return data.get(DATA_TIME_SPEED_PERMILLE);
    }

    /**
     * 获取下次灾劫倒计时（tick）。
     */
    public int getTribulationRemainingTicks() {
        return data.get(DATA_TRIBULATION_REMAINING_TICKS);
    }
}
