package com.Kizunad.guzhenrenext.bastion.menu;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionModifier;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentNode;
import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentRegistry;
import com.Kizunad.guzhenrenext.bastion.service.BastionEvolutionBoostService;
import com.Kizunad.guzhenrenext.bastion.service.BastionTalentService;
import com.Kizunad.guzhenrenext.bastion.service.BastionTeleportService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * 基地管理菜单：提供查看、传送、资源兑换天赋点、加速进化等能力。
 * <p>
 * 说明：
 * <ul>
 *     <li>客户端只读初始快照，用于渲染展示。</li>
 *     <li>所有按钮操作均在服务端校验并执行，避免客户端提交事实数据。</li>
 * </ul>
 * </p>
 */
public class BastionManagementMenu extends AbstractContainerMenu {

    /** 传送按钮 ID。 */
    public static final int BUTTON_TELEPORT = 1;
    /** 兑换天赋点按钮 ID（少量）。 */
    public static final int BUTTON_CONVERT_TALENT_SMALL = 10;
    /** 兑换天赋点按钮 ID（大量）。 */
    public static final int BUTTON_CONVERT_TALENT_LARGE = 11;
    /** 加速进化按钮 ID。 */
    public static final int BUTTON_BOOST_EVOLUTION = 20;
    /** 天赋解锁按钮 ID 基数（实际 ID = BASE + 节点索引）。 */
    public static final int BUTTON_UNLOCK_TALENT_BASE = 100;
    /** 天赋解锁按钮 ID 上限。 */
    private static final int BUTTON_UNLOCK_TALENT_MAX = 199;

    /** 小额兑换消耗资源。 */
    private static final double SMALL_CONVERT_AMOUNT = 100.0D;
    /** 大额兑换消耗资源。 */
    private static final double LARGE_CONVERT_AMOUNT = 1000.0D;

    private final UUID bastionId;
    private final boolean remote;

    // 客户端快照数据（仅用于渲染）
    private int tier;
    private float evolutionProgress;
    private int resourcePool;
    private int pollution;
    private int threatMeter;
    private int talentPoints;
    private BastionDao primaryDao;
    private Set<String> unlockedNodes = Set.of();
    // 词缀数据
    private Set<BastionModifier> modifiers = Set.of();
    // 节点统计数据
    private int totalNodes;
    private int totalMycelium;
    private int totalAnchors;
    private int auraRadius;
    private int baseAuraRadius;
    private Map<Integer, Integer> nodesByTier = Map.of();

    /**
    * 服务端构造函数。
    */
    public BastionManagementMenu(
        int containerId,
        Inventory playerInventory,
        UUID bastionId,
        boolean remote
    ) {
        super(BastionMenus.BASTION_MANAGEMENT.get(), containerId);
        this.bastionId = bastionId;
        this.remote = remote;
    }

    /**
     * 客户端网络构造：读取服务端发来的只读快照。
     */
    public static BastionManagementMenu fromNetwork(
        int containerId,
        Inventory inventory,
        FriendlyByteBuf buf
    ) {
        UUID id = buf.readUUID();
        boolean isRemote = buf.readBoolean();
        BastionManagementMenu menu = new BastionManagementMenu(containerId, inventory, id, isRemote);
        menu.tier = buf.readVarInt();
        menu.evolutionProgress = buf.readFloat();
        menu.resourcePool = buf.readVarInt();
        menu.pollution = buf.readVarInt();
        menu.threatMeter = buf.readVarInt();
        menu.talentPoints = buf.readVarInt();
        menu.primaryDao = BastionDao.valueOf(buf.readUtf().toUpperCase(Locale.ROOT));
        int unlockedCount = buf.readVarInt();
        Set<String> receivedUnlockedNodes = new HashSet<>();
        for (int i = 0; i < unlockedCount; i++) {
            receivedUnlockedNodes.add(buf.readUtf());
        }
        menu.unlockedNodes = Set.copyOf(receivedUnlockedNodes);
        // 读取词缀数据
        int modifierCount = buf.readVarInt();
        Set<BastionModifier> receivedModifiers = new HashSet<>();
        for (int i = 0; i < modifierCount; i++) {
            receivedModifiers.add(BastionModifier.valueOf(buf.readUtf().toUpperCase(Locale.ROOT)));
        }
        menu.modifiers = Set.copyOf(receivedModifiers);
        // 读取节点统计数据
        menu.totalNodes = buf.readVarInt();
        menu.totalMycelium = buf.readVarInt();
        menu.totalAnchors = buf.readVarInt();
        menu.auraRadius = buf.readVarInt();
        menu.baseAuraRadius = buf.readVarInt();
        int tierCount = buf.readVarInt();
        Map<Integer, Integer> tiers = new HashMap<>();
        for (int i = 0; i < tierCount; i++) {
            tiers.put(buf.readVarInt(), buf.readVarInt());
        }
        menu.nodesByTier = Map.copyOf(tiers);
        return menu;
    }

    /**
     * 写入初始数据到网络缓冲。
     */
    public static void writeInitialData(FriendlyByteBuf buf, BastionData bastion, boolean isRemote) {
        buf.writeUUID(bastion.id());
        buf.writeBoolean(isRemote);
        buf.writeVarInt(bastion.tier());
        buf.writeFloat((float) bastion.evolutionProgress());
        buf.writeVarInt((int) Math.floor(bastion.resourcePool()));
        buf.writeVarInt((int) Math.floor(bastion.pollution()));
        buf.writeVarInt(bastion.threatMeter());
        int points = bastion.talentData() == null ? 0 : bastion.talentData().availablePoints();
        buf.writeVarInt(points);
        buf.writeUtf(bastion.primaryDao().getSerializedName());
        Set<String> unlocked = bastion.talentData() == null
            ? Set.of()
            : bastion.talentData().unlockedNodes();
        buf.writeVarInt(unlocked.size());
        for (String nodeId : unlocked) {
            buf.writeUtf(nodeId);
        }
        Set<BastionModifier> mods = bastion.modifiers() == null ? Set.of() : bastion.modifiers();
        buf.writeVarInt(mods.size());
        for (BastionModifier mod : mods) {
            buf.writeUtf(mod.getSerializedName());
        }
        // 写入节点统计数据
        buf.writeVarInt(bastion.totalNodes());
        buf.writeVarInt(bastion.totalMycelium());
        buf.writeVarInt(bastion.totalAnchors());
        buf.writeVarInt(bastion.getAuraRadius());
        buf.writeVarInt(bastion.getBaseAuraRadius());
        Map<Integer, Integer> tiers = bastion.nodesByTier();
        buf.writeVarInt(tiers.size());
        for (var entry : tiers.entrySet()) {
            buf.writeVarInt(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (!(serverPlayer.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        BastionSavedData savedData = BastionSavedData.get(serverLevel);
        BastionData bastion = savedData.getBastion(bastionId);
        if (bastion == null) {
            serverPlayer.sendSystemMessage(Component.translatable("message.guzhenrenext.bastion_menu.not_found"));
            return false;
        }

        BastionData.CaptureState captureState = bastion.captureState();
        if (captureState == null || !captureState.isCapturedBy(serverPlayer.getUUID())) {
            serverPlayer.sendSystemMessage(Component.translatable("message.guzhenrenext.bastion_menu.not_owner"));
            return false;
        }

        // 处理天赋解锁（100-199）
        if (id >= BUTTON_UNLOCK_TALENT_BASE && id <= BUTTON_UNLOCK_TALENT_MAX) {
            int nodeIndex = id - BUTTON_UNLOCK_TALENT_BASE;
            return handleUnlockTalent(serverLevel, serverPlayer, bastion, nodeIndex);
        }

        return switch (id) {
            case BUTTON_TELEPORT -> handleTeleport(serverLevel, serverPlayer, bastion);
            case BUTTON_CONVERT_TALENT_SMALL ->
                handleConvertTalent(serverLevel, serverPlayer, bastion, SMALL_CONVERT_AMOUNT);
            case BUTTON_CONVERT_TALENT_LARGE ->
                handleConvertTalent(serverLevel, serverPlayer, bastion, LARGE_CONVERT_AMOUNT);
            case BUTTON_BOOST_EVOLUTION -> handleBoostEvolution(serverLevel, serverPlayer, bastion);
            default -> super.clickMenuButton(player, id);
        };
    }

    private boolean handleTeleport(ServerLevel level, ServerPlayer player, BastionData bastion) {
        return BastionTeleportService.tryTeleport(level, player, bastion.id());
    }

    private boolean handleConvertTalent(
        ServerLevel level,
        ServerPlayer player,
        BastionData bastion,
        double amount
    ) {
        try {
            BastionTalentService.ConversionResult result =
                BastionTalentService.convertResourceToTalentPoints(bastion, amount);
            BastionSavedData.get(level).updateBastion(result.updatedBastion());
            player.sendSystemMessage(Component.literal(
                "§a兑换成功：+" + result.pointsGained() + " 天赋点"
            ));
            return true;
        } catch (IllegalArgumentException ex) {
            player.sendSystemMessage(Component.literal("§c" + ex.getMessage()));
            return false;
        }
    }

    private boolean handleBoostEvolution(ServerLevel level, ServerPlayer player, BastionData bastion) {
        if (remote) {
            player.sendSystemMessage(Component.translatable("message.guzhenrenext.bastion_menu.remote_no_boost"));
            return false;
        }
        return BastionEvolutionBoostService.tryBoostEvolution(level, bastion, player);
    }

    /**
     * 处理天赋节点解锁请求。
     */
    private boolean handleUnlockTalent(
        ServerLevel level,
        ServerPlayer player,
        BastionData bastion,
        int nodeIndex
    ) {
        java.util.List<BastionTalentNode> allNodes = BastionTalentRegistry.getAllNodes();
        if (nodeIndex < 0 || nodeIndex >= allNodes.size()) {
            player.sendSystemMessage(Component.literal("§c无效的天赋节点索引"));
            return false;
        }
        String nodeId = allNodes.get(nodeIndex).id();

        BastionTalentService.UnlockResult result = BastionTalentService.tryUnlockNode(bastion, nodeId);
        if (result.success()) {
            BastionSavedData.get(level).updateBastion(result.updatedBastion());
            player.sendSystemMessage(Component.literal("§a天赋解锁成功：" + nodeId));
            return true;
        } else {
            player.sendSystemMessage(Component.literal("§c解锁失败：" + result.message()));
            return false;
        }
    }

    public UUID getBastionId() {
        return bastionId;
    }

    public boolean isRemote() {
        return remote;
    }

    public int getTier() {
        return tier;
    }

    public float getEvolutionProgress() {
        return evolutionProgress;
    }

    public int getResourcePool() {
        return resourcePool;
    }

    public int getPollution() {
        return pollution;
    }

    public int getThreatMeter() {
        return threatMeter;
    }

    public int getTalentPoints() {
        return talentPoints;
    }

    public BastionDao getPrimaryDao() {
        return primaryDao;
    }

    public Set<String> getUnlockedNodes() {
        return unlockedNodes;
    }

    public Set<BastionModifier> getModifiers() {
        return modifiers;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public int getTotalMycelium() {
        return totalMycelium;
    }

    public int getTotalAnchors() {
        return totalAnchors;
    }

    public int getAuraRadius() {
        return auraRadius;
    }

    public int getBaseAuraRadius() {
        return baseAuraRadius;
    }

    public Map<Integer, Integer> getNodesByTier() {
        return nodesByTier;
    }
}
