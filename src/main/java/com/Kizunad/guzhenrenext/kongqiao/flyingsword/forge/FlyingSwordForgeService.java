package com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

public final class FlyingSwordForgeService {

    private FlyingSwordForgeService() {}

    private static final String GUZHENREN_NAMESPACE = "guzhenren";

    private static final String[] DAO_TAGS = {
        "huo_dao", "leidao", "jiandao", "ren_dao", "xue_dao", "shui_dao",
        "du_dao", "bing_xue_dao", "feng_dao", "tu_dao", "mu_dao", "jin_dao",
        "hun_dao", "gu_dao", "tian_dao", "nu_dao", "tou_dao", "xin_dao",
        "ying_dao", "xing_dao", "yue_dao", "yundao", "zhi_dao", "zhou_dao",
        "lv_dao", "yu_dao", "shi_dao", "guang_dao", "daodao", "lian_dao",
        "bianha_dao"
    };

    private static final String[] Z_TAGS = {"z5", "z4", "z3", "z2", "z1"};
    private static final int[] Z_POINTS = {5, 4, 3, 2, 1};

    public static void handleInsertItem(ServerPlayer player, Container inputContainer) {
        ItemStack stack = inputContainer.getItem(0);
        if (stack.isEmpty()) {
            return;
        }

        FlyingSwordForgeAttachment forge = KongqiaoAttachments.getFlyingSwordForge(player);
        if (forge == null) {
            return;
        }

        if (!forge.isActive()) {
            if (stack.getItem() instanceof SwordItem) {
                startForge(player, forge, stack, inputContainer);
            } else {
                forge.setLastMessage("请先放入一把剑作为核心剑");
            }
            return;
        }

        String stackItemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (stackItemId.equals(forge.getBaseSwordItemId())) {
            feedMaterialSword(player, forge, stack, inputContainer);
            return;
        }

        if (tryFeedGuchong(player, forge, stack, inputContainer)) {
            return;
        }

        forge.setLastMessage("材料不匹配：需要 " + forge.getBaseSwordItemId() + " 或蛊虫");
    }

    private static void startForge(
        ServerPlayer player,
        FlyingSwordForgeAttachment forge,
        ItemStack stack,
        Container inputContainer
    ) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        try {
            forge.setBaseSwordItem((CompoundTag) stack.save(player.registryAccess()));
        } catch (Exception e) {
            forge.setBaseSwordItem(new CompoundTag());
        }
        forge.setBaseSwordItemId(itemId);
        forge.setRequiredSwordCount(FlyingSwordForgeAttachment.DEFAULT_REQUIRED_SWORD_COUNT);
        forge.setFedSwordCount(1);
        forge.setActive(true);
        forge.setLastMessage("已设置核心剑: " + stack.getHoverName().getString());

        stack.shrink(1);
        inputContainer.setChanged();

        player.sendSystemMessage(Component.literal("[飞剑培养] 已设置核心剑，开始培养"));
    }

    private static void feedMaterialSword(
        ServerPlayer player,
        FlyingSwordForgeAttachment forge,
        ItemStack stack,
        Container inputContainer
    ) {
        int needed = forge.getRequiredSwordCount() - forge.getFedSwordCount();
        if (needed <= 0) {
            forge.setLastMessage("剑材料已满，可以收取成品");
            return;
        }

        int toConsume = Math.min(needed, stack.getCount());
        int actualAdded = forge.addFedSwordCount(toConsume);
        stack.shrink(actualAdded);
        inputContainer.setChanged();

        forge.setLastMessage(
            "已投喂剑 x" + actualAdded + " (" +
            forge.getFedSwordCount() + "/" + forge.getRequiredSwordCount() + ")"
        );

        if (forge.canClaim()) {
            player.sendSystemMessage(Component.literal("[飞剑培养] 剑材料已满，可以收取成品！"));
        }
    }

    private static boolean tryFeedGuchong(
        ServerPlayer player,
        FlyingSwordForgeAttachment forge,
        ItemStack stack,
        Container inputContainer
    ) {
        int zPoints = getZPoints(stack);
        if (zPoints <= 0) {
            return false;
        }

        String daoKey = getDaoKey(stack);
        if (daoKey == null) {
            forge.setLastMessage("蛊虫道属性不唯一或无法识别");
            return false;
        }

        forge.addDaoMark(daoKey, zPoints);
        stack.shrink(1);
        inputContainer.setChanged();

        forge.setLastMessage(
            "已吸收 " + daoKey + " 道痕 +" + zPoints +
            " (累计: " + forge.getDaoMark(daoKey) + ")"
        );

        return true;
    }

    private static int getZPoints(ItemStack stack) {
        for (int i = 0; i < Z_TAGS.length; i++) {
            TagKey<Item> tag = TagKey.create(
                BuiltInRegistries.ITEM.key(),
                ResourceLocation.fromNamespaceAndPath(GUZHENREN_NAMESPACE, Z_TAGS[i])
            );
            if (stack.is(tag)) {
                return Z_POINTS[i];
            }
        }
        return 0;
    }

    private static String getDaoKey(ItemStack stack) {
        String found = null;
        int count = 0;

        for (String daoTag : DAO_TAGS) {
            TagKey<Item> tag = TagKey.create(
                BuiltInRegistries.ITEM.key(),
                ResourceLocation.fromNamespaceAndPath(GUZHENREN_NAMESPACE, daoTag)
            );
            if (stack.is(tag)) {
                found = daoTagToKey(daoTag);
                count++;
                if (count > 1) {
                    return null;
                }
            }
        }

        return found;
    }

    private static String daoTagToKey(String daoTag) {
        return switch (daoTag) {
            case "huo_dao" -> DaoHenHelper.DaoType.HUO_DAO.name().toLowerCase();
            case "leidao" -> DaoHenHelper.DaoType.LEI_DAO.name().toLowerCase();
            case "jiandao" -> DaoHenHelper.DaoType.JIAN_DAO.name().toLowerCase();
            case "ren_dao" -> DaoHenHelper.DaoType.REN_DAO.name().toLowerCase();
            case "xue_dao" -> DaoHenHelper.DaoType.XUE_DAO.name().toLowerCase();
            case "shui_dao" -> DaoHenHelper.DaoType.SHUI_DAO.name().toLowerCase();
            case "du_dao" -> DaoHenHelper.DaoType.DU_DAO.name().toLowerCase();
            case "bing_xue_dao" -> DaoHenHelper.DaoType.BING_XUE_DAO.name().toLowerCase();
            case "feng_dao" -> DaoHenHelper.DaoType.FENG_DAO.name().toLowerCase();
            case "tu_dao" -> DaoHenHelper.DaoType.TU_DAO.name().toLowerCase();
            case "mu_dao" -> DaoHenHelper.DaoType.MU_DAO.name().toLowerCase();
            case "jin_dao" -> DaoHenHelper.DaoType.JIN_DAO.name().toLowerCase();
            case "hun_dao" -> DaoHenHelper.DaoType.HUN_DAO.name().toLowerCase();
            case "gu_dao" -> DaoHenHelper.DaoType.GU_DAO.name().toLowerCase();
            case "tian_dao" -> DaoHenHelper.DaoType.TIAN_DAO.name().toLowerCase();
            case "nu_dao" -> DaoHenHelper.DaoType.NU_DAO.name().toLowerCase();
            case "tou_dao" -> DaoHenHelper.DaoType.TOU_DAO.name().toLowerCase();
            case "xin_dao" -> DaoHenHelper.DaoType.XIN_DAO.name().toLowerCase();
            case "ying_dao" -> DaoHenHelper.DaoType.YING_DAO.name().toLowerCase();
            case "xing_dao" -> DaoHenHelper.DaoType.XING_DAO.name().toLowerCase();
            case "yue_dao" -> DaoHenHelper.DaoType.YUE_DAO.name().toLowerCase();
            case "yundao" -> DaoHenHelper.DaoType.YUN_DAO.name().toLowerCase();
            case "zhi_dao" -> DaoHenHelper.DaoType.ZHI_DAO.name().toLowerCase();
            case "zhou_dao" -> DaoHenHelper.DaoType.ZHOU_DAO.name().toLowerCase();
            case "lv_dao" -> DaoHenHelper.DaoType.LV_DAO.name().toLowerCase();
            case "yu_dao" -> DaoHenHelper.DaoType.YU_DAO.name().toLowerCase();
            case "shi_dao" -> DaoHenHelper.DaoType.SHI_DAO.name().toLowerCase();
            case "guang_dao" -> DaoHenHelper.DaoType.GUANG_DAO.name().toLowerCase();
            case "daodao" -> DaoHenHelper.DaoType.DAO_DAO.name().toLowerCase();
            case "lian_dao" -> DaoHenHelper.DaoType.LIAN_DAO.name().toLowerCase();
            case "bianha_dao" -> DaoHenHelper.DaoType.BIAN_HUA_DAO.name().toLowerCase();
            default -> daoTag.replace("_", "");
        };
    }

    public static boolean claim(ServerPlayer player) {
        FlyingSwordForgeAttachment forge = KongqiaoAttachments.getFlyingSwordForge(player);
        if (forge == null || !forge.canClaim()) {
            if (forge != null) {
                forge.setLastMessage("尚未满足收取条件");
            }
            return false;
        }

        FlyingSwordStorageAttachment storage = KongqiaoAttachments.getFlyingSwordStorage(player);
        if (storage == null) {
            forge.setLastMessage("无法访问飞剑存储");
            return false;
        }

        FlyingSwordStorageAttachment.RecalledSword recalled =
            new FlyingSwordStorageAttachment.RecalledSword();

        recalled.displayItem = forge.getBaseSwordItem().copy();
        recalled.quality = SwordQuality.COMMON;
        recalled.level = 1;
        recalled.experience = 0;
        recalled.durability = 1.0f;

        CompoundTag attributesTag = new CompoundTag();
        CompoundTag daoMarksTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : forge.getDaoMarks().entrySet()) {
            daoMarksTag.putInt(entry.getKey(), entry.getValue());
        }
        attributesTag.put("DaoMarks", daoMarksTag);
        recalled.attributes = attributesTag;

        boolean success = storage.recallSword(recalled);
        if (!success) {
            forge.setLastMessage("飞剑存储已满");
            player.sendSystemMessage(Component.literal("[飞剑培养] 存储已满，无法收取"));
            return false;
        }

        forge.clear();
        forge.setLastMessage("成功收取飞剑！");
        player.sendSystemMessage(Component.literal("[飞剑培养] 飞剑已入库，按 V 键恢复"));
        return true;
    }

    public static void cancel(ServerPlayer player) {
        FlyingSwordForgeAttachment forge = KongqiaoAttachments.getFlyingSwordForge(player);
        if (forge == null || !forge.isActive()) {
            return;
        }

        CompoundTag baseSwordTag = forge.getBaseSwordItem();
        if (baseSwordTag != null && !baseSwordTag.isEmpty()) {
            try {
                ItemStack returnStack = ItemStack.parse(
                    player.registryAccess(),
                    baseSwordTag
                ).orElse(ItemStack.EMPTY);
                if (!returnStack.isEmpty()) {
                    if (!player.getInventory().add(returnStack)) {
                        player.drop(returnStack, false);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        forge.clear();
        forge.setLastMessage("培养已取消，核心剑已返还");
        player.sendSystemMessage(Component.literal("[飞剑培养] 培养已取消"));
    }
}
