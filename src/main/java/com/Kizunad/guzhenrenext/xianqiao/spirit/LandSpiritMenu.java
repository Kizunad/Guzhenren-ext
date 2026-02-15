package com.Kizunad.guzhenrenext.xianqiao.spirit;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoMenus;
import com.Kizunad.guzhenrenext.xianqiao.service.SpiritUnlockService;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * 地灵管理菜单（仅数据同步，不包含物品槽）。
 * <p>
 * 本菜单用于同步仙窍基础信息：
 * 坐标、chunk 边界、时间流速、下次灾劫倒计时。
 * </p>
 */
public class LandSpiritMenu extends AbstractContainerMenu {

    /** 单个 chunk 的方块边长（用于兼容展示值换算）。 */
    private static final int CHUNK_SIZE_BLOCKS = 16;

    /** 数据槽：中心 X。 */
    private static final int DATA_CENTER_X = 0;

    /** 数据槽：中心 Y。 */
    private static final int DATA_CENTER_Y = 1;

    /** 数据槽：中心 Z。 */
    private static final int DATA_CENTER_Z = 2;

    /** 数据槽：边界最小 chunk X。 */
    private static final int DATA_MIN_CHUNK_X = 3;

    /** 数据槽：边界最大 chunk X。 */
    private static final int DATA_MAX_CHUNK_X = 4;

    /** 数据槽：边界最小 chunk Z。 */
    private static final int DATA_MIN_CHUNK_Z = 5;

    /** 数据槽：边界最大 chunk Z。 */
    private static final int DATA_MAX_CHUNK_Z = 6;

    /** 数据槽：时间流速（千分比）。 */
    private static final int DATA_TIME_SPEED_PERMILLE = 7;

    /** 数据槽：下次灾劫倒计时（tick，int 上限截断）。 */
    private static final int DATA_TRIBULATION_REMAINING_TICKS = 8;

    /** 数据槽：地灵好感度（favorability * 10，保留 1 位小数）。 */
    private static final int DATA_FAVORABILITY_PERMILLE = 9;

    /** 数据槽：地灵转数（tier）。 */
    private static final int DATA_TIER = 10;

    /** 数据槽：当前阶段（stage）。 */
    private static final int DATA_CURRENT_STAGE = 11;

    /** 数据槽：下一阶段所需最小转数。 */
    private static final int DATA_NEXT_STAGE_MIN_TIER = 12;

    /** 数据槽：下一阶段所需最小好感度（favorability * 10）。 */
    private static final int DATA_NEXT_STAGE_MIN_FAVORABILITY_PERMILLE = 13;

    /** ContainerData 字段总数。 */
    private static final int DATA_FIELDS = 14;

    /** 时间流速千分比基准。 */
    private static final int PERMILLE_BASE = 1000;

    /** 好感度缩放因子：favorability * 10 保留 1 位小数。 */
    private static final int FAVORABILITY_PERMILLE_FACTOR = 10;

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
                int currentStage = SpiritUnlockService.computeStage(info.tier(), info.favorability());
                int nextStage = SpiritUnlockService.getNextStage(currentStage);
                return switch (index) {
                    case DATA_CENTER_X -> info.center().getX();
                    case DATA_CENTER_Y -> info.center().getY();
                    case DATA_CENTER_Z -> info.center().getZ();
                    case DATA_MIN_CHUNK_X -> info.minChunkX();
                    case DATA_MAX_CHUNK_X -> info.maxChunkX();
                    case DATA_MIN_CHUNK_Z -> info.minChunkZ();
                    case DATA_MAX_CHUNK_Z -> info.maxChunkZ();
                    case DATA_TIME_SPEED_PERMILLE -> Math.round(info.timeSpeed() * PERMILLE_BASE);
                    case DATA_TRIBULATION_REMAINING_TICKS -> getSafeTribulationRemainingTicks(info, apertureLevel);
                    case DATA_FAVORABILITY_PERMILLE -> Math.round(info.favorability() * FAVORABILITY_PERMILLE_FACTOR);
                    case DATA_TIER -> info.tier();
                    case DATA_CURRENT_STAGE -> currentStage;
                    case DATA_NEXT_STAGE_MIN_TIER -> SpiritUnlockService.getMinTierForStage(nextStage);
                    case DATA_NEXT_STAGE_MIN_FAVORABILITY_PERMILLE ->
                        Math.round(
                            SpiritUnlockService.getMinFavorabilityForStage(nextStage) * FAVORABILITY_PERMILLE_FACTOR
                        );
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

    /** 获取边界最小 chunk X。 */
    public int getMinChunkX() {
        return data.get(DATA_MIN_CHUNK_X);
    }

    /** 获取边界最大 chunk X。 */
    public int getMaxChunkX() {
        return data.get(DATA_MAX_CHUNK_X);
    }

    /** 获取边界最小 chunk Z。 */
    public int getMinChunkZ() {
        return data.get(DATA_MIN_CHUNK_Z);
    }

    /** 获取边界最大 chunk Z。 */
    public int getMaxChunkZ() {
        return data.get(DATA_MAX_CHUNK_Z);
    }

    /** 获取 X 轴 chunk 跨度（闭区间）。 */
    public int getChunkSpanX() {
        return getMaxChunkX() - getMinChunkX() + 1;
    }

    /** 获取 Z 轴 chunk 跨度（闭区间）。 */
    public int getChunkSpanZ() {
        return getMaxChunkZ() - getMinChunkZ() + 1;
    }

    /**
     * 获取兼容展示距离（由 chunk 边界推导，供旧界面过渡展示）。
     * <p>
     * 兼容过渡，非真源：range = chunk range；边界 = min/max chunk 闭区间。
     * </p>
     */
    public int getRadius() {
        int chunkHalfRange = Math.max(0, Math.min(getChunkSpanX(), getChunkSpanZ()) / 2);
        return chunkHalfRange * CHUNK_SIZE_BLOCKS;
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

    /**
     * 获取地灵好感度（favorability * 10）。
     */
    public int getFavorabilityPermille() {
        return data.get(DATA_FAVORABILITY_PERMILLE);
    }

    /**
     * 获取地灵转数（tier）。
     */
    public int getTier() {
        return data.get(DATA_TIER);
    }

    /**
     * 获取地灵当前阶段（stage）。
     */
    public int getCurrentStage() {
        return data.get(DATA_CURRENT_STAGE);
    }

    /**
     * 获取下一阶段所需最小转数。
     */
    public int getNextStageMinTier() {
        return data.get(DATA_NEXT_STAGE_MIN_TIER);
    }

    /**
     * 获取下一阶段所需最小好感度（favorability * 10）。
     */
    public int getNextStageMinFavorabilityPermille() {
        return data.get(DATA_NEXT_STAGE_MIN_FAVORABILITY_PERMILLE);
    }
}
