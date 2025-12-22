package com.Kizunad.guzhenrenext.commands;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.guzhenrenext.customNPCImpl.ai.Action.GuzhenrenItemUseAction;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoId;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class GuzhenrenDebugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("guzhenren_mind")
                .requires(source -> source.hasPermission(2))
                .then(
                    Commands.literal("use_hand_item")
                        .then(
                            Commands.argument("npc", EntityArgument.entity())
                                .executes(GuzhenrenDebugCommand::useHandItem)
                        )
                )
        );

        dispatcher.register(
            Commands.literal("guzhenren_debug")
                .requires(source -> source.hasPermission(2))
                .then(
                    Commands.literal("unlock_all")
                        .executes(GuzhenrenDebugCommand::unlockAllSelf)
                        .then(
                            Commands.argument("player", EntityArgument.player())
                                .executes(GuzhenrenDebugCommand::unlockAllPlayer)
                        )
                )
        );
    }

    private static int useHandItem(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "npc");
            if (!(entity instanceof Mob mob)) {
                context.getSource().sendFailure(Component.literal("目标必须是 Mob 实体"));
                return 0;
            }

            if (!mob.hasData(NpcMindAttachment.NPC_MIND)) {
                context.getSource().sendFailure(Component.literal("目标没有 Mind 系统"));
                return 0;
            }

            INpcMind mind = mob.getData(NpcMindAttachment.NPC_MIND);
            ItemStack mainHandItem = mob.getItemInHand(InteractionHand.MAIN_HAND);

            if (mainHandItem.isEmpty()) {
                context.getSource().sendFailure(Component.literal("目标主手为空"));
                return 0;
            }

            // 创建并提交动作
            GuzhenrenItemUseAction action = new GuzhenrenItemUseAction(mainHandItem);
            mind.getActionExecutor().addAction(action);

            context.getSource().sendSuccess(
                () -> Component.literal(
                    "已让 NPC 尝试使用主手物品: " + mainHandItem.getDisplayName().getString()
                ),
                true
            );
            return 1;

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int unlockAllSelf(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            return unlockAll(context.getSource(), player);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int unlockAllPlayer(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            return unlockAll(context.getSource(), player);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int unlockAll(CommandSourceStack source, ServerPlayer player) {
        NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(player);
        if (unlocks == null) {
            source.sendFailure(Component.literal("目标玩家缺少 NianTouUnlocks 附加数据"));
            return 0;
        }

        // Debug：一键解锁全部念头用途与杀招，便于快速测试轮盘/推演/UI 流程。
        NianTouUnlockCount nianTouUnlockCount = unlockAllNianTouUsages(unlocks);
        int unlockedShazhao = unlockAllShazhao(unlocks);
        int unlockedNianTouItems = nianTouUnlockCount.items();
        int unlockedNianTouUsages = nianTouUnlockCount.usages();

        unlocks.clearProcess();
        unlocks.setShazhaoMessage("已通过 Debug 命令解锁全部杀招");
        PacketDistributor.sendToPlayer(player, new PacketSyncNianTouUnlocks(unlocks));

        source.sendSuccess(
            () -> Component.literal(
                "已为玩家解锁全部念头用途与杀招"
                    + " | nianTouItems="
                    + unlockedNianTouItems
                    + " nianTouUsages="
                    + unlockedNianTouUsages
                    + " shazhao="
                    + unlockedShazhao
            ),
            true
        );
        player.sendSystemMessage(
            Component.literal(
                "Debug：已解锁全部念头用途与杀招"
                    + " | nianTouItems="
                    + unlockedNianTouItems
                    + " nianTouUsages="
                    + unlockedNianTouUsages
                    + " shazhao="
                    + unlockedShazhao
            )
        );
        return 1;
    }

    private record NianTouUnlockCount(int items, int usages) {}

    private static NianTouUnlockCount unlockAllNianTouUsages(NianTouUnlocks unlocks) {
        int unlockedItems = 0;
        int unlockedUsages = 0;
        for (NianTouData data : NianTouDataManager.getAll()) {
            if (data == null || data.itemID() == null || data.itemID().isBlank()) {
                continue;
            }
            ResourceLocation itemId;
            try {
                itemId = ResourceLocation.parse(data.itemID());
            } catch (Exception e) {
                continue;
            }
            unlocks.unlock(itemId);
            unlockedItems++;
            if (data.usages() != null) {
                unlockedUsages += data.usages().size();
            }
        }
        return new NianTouUnlockCount(unlockedItems, unlockedUsages);
    }

    private static int unlockAllShazhao(NianTouUnlocks unlocks) {
        int unlockedShazhao = 0;
        for (ShazhaoData data : ShazhaoDataManager.getAll()) {
            if (data == null || data.shazhaoID() == null || data.shazhaoID().isBlank()) {
                continue;
            }
            ResourceLocation shazhaoId;
            try {
                shazhaoId = ResourceLocation.parse(data.shazhaoID());
            } catch (Exception e) {
                continue;
            }
            ResourceLocation migrated = ShazhaoId.migrateLegacyId(shazhaoId);
            if (migrated == null) {
                continue;
            }
            unlocks.unlockShazhao(migrated);
            unlockedShazhao++;
        }
        return unlockedShazhao;
    }
}
