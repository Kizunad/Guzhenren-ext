package com.Kizunad.guzhenrenext.xianqiao.alchemy.menu;

import com.Kizunad.guzhenrenext.xianqiao.alchemy.blockentity.AlchemyFurnaceBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.PillItem;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.PillQuality;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.service.AlchemyService;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 炼丹炉菜单。
 * <p>
 * 负责处理炼丹炉的交互逻辑，包含：
 * 1) 1 个主材槽位 (0)
 * 2) 4 个辅材槽位 (1-4)
 * 3) 1 个产出槽位 (5)
 * 4) 1 个转运蛊控制槽位 (6)
 * 5) 玩家背包与快捷栏
 * </p>
 */
public class AlchemyFurnaceMenu extends AbstractContainerMenu {

    /** 容器数据字段数量（当前阶段为 0，预留给进度/品质等）。 */
    private static final int DATA_COUNT = 0;

    // --- 槽位坐标定义 (参考标准 176x166 纹理布局) ---
    // 主材放在中间
    private static final int MAIN_SLOT_X = 80;
    private static final int MAIN_SLOT_Y = 35;

    // 辅材环绕主材 (上下左右)
    private static final int AUX_1_X = 56; // 左
    private static final int AUX_1_Y = 35;
    private static final int AUX_2_X = 104; // 右
    private static final int AUX_2_Y = 35;
    private static final int AUX_3_X = 80; // 上
    private static final int AUX_3_Y = 13;
    private static final int AUX_4_X = 80; // 下
    private static final int AUX_4_Y = 57;

    // 产出槽位 (右侧)
    private static final int OUTPUT_SLOT_X = 140;
    private static final int OUTPUT_SLOT_Y = 35;

    // 转运蛊控制槽位（左上）
    private static final int TRANSFER_CONTROL_SLOT_X = 8;
    private static final int TRANSFER_CONTROL_SLOT_Y = 13;

    /** 炼制按钮 ID。 */
    public static final int BUTTON_REFINE = 0;

    /**
     * 单次成功产出对应的经验值。
     * <p>
     * 当前阶段仅做“成功次数 -> 经验值”的最小可追踪映射，
     * 后续若接入实际发放逻辑（如方块实体结算/UI 展示），可在此处统一调整系数。
     * </p>
     */
    private static final int XP_PER_SUCCESS = 1;

    /** 无品质时的界面占位文本。 */
    private static final String QUALITY_EMPTY_TEXT = "品质: -";
    /** 品质文本前缀。 */
    private static final String QUALITY_TEXT_PREFIX = "品质: ";
    /** 无品质输出时使用的默认文本颜色。 */
    private static final int QUALITY_EMPTY_COLOR = 0xFF404040;

    // 玩家背包相关
    private static final int PLAYER_INV_X_START = 8;
    private static final int PLAYER_INV_Y_START = 84;
    private static final int HOTBAR_Y = 142;
    private static final int SLOT_SPACING = 18;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;

    /** 快捷栏在背包槽位索引中的起始偏移量（即背包有 9 列）。 */
    private static final int HOTBAR_OFFSET = 9;

    private final Container container;
    private final ContainerData data;

    /**
     * 最近一次点击炼制按钮得到的经验值。
     * <p>
     * 该字段用于最小化追踪批量炼制收益，不在本阶段直接生成经验球。
     * </p>
     */
    private int lastRefineExperience;

    /**
     * 客户端构造：用于网络包接收时的实例化。
     *
     * @param containerId 容器 ID
     * @param playerInventory 玩家背包
     */
    public AlchemyFurnaceMenu(int containerId, Inventory playerInventory) {
        this(
            containerId,
            playerInventory,
            new SimpleContainer(AlchemyFurnaceBlockEntity.INVENTORY_SIZE),
            new SimpleContainerData(DATA_COUNT)
        );
    }

    /**
     * 服务端/通用构造：完整的容器初始化。
     *
     * @param containerId 容器 ID
     * @param playerInventory 玩家背包
     * @param container 炼丹炉容器实例
     * @param data 容器数据（进度等）
     */
    public AlchemyFurnaceMenu(
        int containerId,
        Inventory playerInventory,
        Container container,
        ContainerData data
    ) {
        super(XianqiaoMenus.ALCHEMY_FURNACE.get(), containerId);
        this.container = container;
        this.data = data;

        checkContainerSize(container, AlchemyFurnaceBlockEntity.INVENTORY_SIZE);
        checkContainerDataCount(data, DATA_COUNT);

        container.startOpen(playerInventory.player);

        // 1. 添加炼丹炉槽位
        addFurnaceSlots();

        // 2. 添加玩家背包槽位
        addPlayerSlots(playerInventory);

        // 3. 添加数据槽位
        addDataSlots(data);
    }

    /**
     * 网络工厂方法：遵循 IMenuTypeExtension 规范。
     *
     * @param containerId 容器 ID
     * @param inventory 玩家背包
     * @param buf 网络缓冲（需读取 BlockPos 保持协议一致）
     * @return 菜单实例
     */
    public static AlchemyFurnaceMenu fromNetwork(
        int containerId,
        Inventory inventory,
        FriendlyByteBuf buf
    ) {
        buf.readBlockPos(); // 协议占位，当前不需要具体 Pos
        return new AlchemyFurnaceMenu(containerId, inventory);
    }

    private void addFurnaceSlots() {
        // 0: 主材
        addSlot(new Slot(container, AlchemyFurnaceBlockEntity.SLOT_MAIN, MAIN_SLOT_X, MAIN_SLOT_Y));
        // 1-4: 辅材
        addSlot(new Slot(container, AlchemyFurnaceBlockEntity.SLOT_AUX_1, AUX_1_X, AUX_1_Y));
        addSlot(new Slot(container, AlchemyFurnaceBlockEntity.SLOT_AUX_2, AUX_2_X, AUX_2_Y));
        addSlot(new Slot(container, AlchemyFurnaceBlockEntity.SLOT_AUX_3, AUX_3_X, AUX_3_Y));
        addSlot(new Slot(container, AlchemyFurnaceBlockEntity.SLOT_AUX_4, AUX_4_X, AUX_4_Y));
        // 5: 产出 (使用 OutputSlot 限制放入)
        addSlot(new OutputSlot(container, AlchemyFurnaceBlockEntity.SLOT_OUTPUT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y));
        // 6: 转运蛊控制槽（仅允许 transfer_gu）
        addSlot(
            new TransferControlSlot(
                container,
                AlchemyFurnaceBlockEntity.SLOT_TRANSFER_CONTROL,
                TRANSFER_CONTROL_SLOT_X,
                TRANSFER_CONTROL_SLOT_Y
            )
        );
    }

    private void addPlayerSlots(Inventory playerInventory) {
        // 玩家背包 (索引 9-35)
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                int x = PLAYER_INV_X_START + col * SLOT_SPACING;
                int y = PLAYER_INV_Y_START + row * SLOT_SPACING;
                int index = col + row * PLAYER_INV_COLS + HOTBAR_OFFSET;
                addSlot(new Slot(playerInventory, index, x, y));
            }
        }
        // 快捷栏 (索引 0-8)
        for (int col = 0; col < PLAYER_INV_COLS; col++) {
            int x = PLAYER_INV_X_START + col * SLOT_SPACING;
            addSlot(new Slot(playerInventory, col, x, HOTBAR_Y));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_REFINE) {
            if (player.level().isClientSide) {
                return true;
            }
            ServerPlayer serverPlayer = player instanceof ServerPlayer casted ? casted : null;
            int successfulCount = 0;
            boolean refined = false;
            if (container instanceof AlchemyFurnaceBlockEntity blockEntity && blockEntity.isBulkMode()) {
                int mainCountBefore = container.getItem(AlchemyFurnaceBlockEntity.SLOT_MAIN).getCount();
                successfulCount = AlchemyService.tryRefineBatch(container, blockEntity.getBatchSize(), serverPlayer);
                int mainCountAfter = container.getItem(AlchemyFurnaceBlockEntity.SLOT_MAIN).getCount();
                refined = successfulCount > 0 || mainCountAfter < mainCountBefore;
            } else {
                int outputCountBefore = container.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT).getCount();
                refined = AlchemyService.tryRefine(container, serverPlayer);
                int outputCountAfter = container.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT).getCount();
                if (outputCountAfter > outputCountBefore) {
                    successfulCount = 1;
                }
            }
            lastRefineExperience = successfulCount * XP_PER_SUCCESS;
            if (refined) {
                broadcastChanges();
            }
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    /**
     * 获取最近一次炼制按钮触发后记录的经验值。
     *
     * @return 最近一次炼制经验值（按成功产出次数缩放）
     */
    public int getLastRefineExperience() {
        return lastRefineExperience;
    }

    public int getSuccessRatePercent() {
        return AlchemyService.calculateSuccessRatePercent(container);
    }

    /**
     * 获取产出槽品质显示文本。
     *
     * @return "品质: xxx" 或占位文本
     */
    public String getOutputQualityText() {
        ItemStack outputStack = container.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT);
        if (outputStack.isEmpty()) {
            return QUALITY_EMPTY_TEXT;
        }
        PillQuality quality = PillItem.readQuality(outputStack);
        return QUALITY_TEXT_PREFIX + quality.getDisplayName();
    }

    /**
     * 获取产出槽品质显示颜色。
     *
     * @return 品质颜色；无产出时返回默认文本色
     */
    public int getOutputQualityColor() {
        ItemStack outputStack = container.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT);
        if (outputStack.isEmpty()) {
            return QUALITY_EMPTY_COLOR;
        }
        return PillItem.readQuality(outputStack).getDisplayColor();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copied = stack.copy();
        int containerSlots = AlchemyFurnaceBlockEntity.INVENTORY_SIZE; // 7

        // 如果是容器内的槽位 (0-6) -> 移入玩家背包
        if (index < containerSlots) {
            if (!moveItemStackTo(stack, containerSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // 如果是玩家背包的槽位：
            // 1) transfer_gu 先尝试进入控制槽(6)；
            // 2) 其他物品仅尝试输入槽(0-4)；
            // 3) 始终禁止放入产出槽(5)。
            if (stack.is(XianqiaoItems.TRANSFER_GU.get())) {
                if (
                    !moveItemStackTo(
                        stack,
                        AlchemyFurnaceBlockEntity.SLOT_TRANSFER_CONTROL,
                        AlchemyFurnaceBlockEntity.SLOT_TRANSFER_CONTROL + 1,
                        false
                    )
                ) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, AlchemyFurnaceBlockEntity.SLOT_OUTPUT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copied;
    }

    /**
     * 产出槽位：只允许取出，不允许放入。
     */
    private static final class OutputSlot extends Slot {
        public OutputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    /**
     * 转运蛊控制槽：仅允许放入 transfer_gu。
     */
    private static final class TransferControlSlot extends Slot {
        public TransferControlSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(XianqiaoItems.TRANSFER_GU.get());
        }
    }

}
