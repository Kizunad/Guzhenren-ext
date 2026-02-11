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
        player.sendSystemMessage(Component.literal("[飞剑培养] 已吸收 " + daoKey + " 道痕 +" + zPoints));

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

    private static final String TAG_PATH_SUFFIX_DAO = "dao";
    private static final String TAG_PATH_SUFFIX_DAO_UNDERSCORE = "_dao";

    private static String getDaoKey(ItemStack stack) {
        String found = null;
        int count = 0;

        java.util.Iterator<TagKey<Item>> it =
            stack.getItem().builtInRegistryHolder().tags().iterator();
        while (it.hasNext()) {
            TagKey<Item> tag = it.next();
            if (tag == null) {
                continue;
            }

            ResourceLocation id = tag.location();
            if (id == null || !GUZHENREN_NAMESPACE.equals(id.getNamespace())) {
                continue;
            }

            String path = id.getPath();
            if (path == null || path.isBlank()) {
                continue;
            }

            if (!looksLikeDaoTag(path)) {
                continue;
            }

            found = tagPathToDaoKey(path);
            count++;
            if (count > 1) {
                return null;
            }
        }

        return found;
    }

    private static boolean looksLikeDaoTag(String path) {
        return path.endsWith(TAG_PATH_SUFFIX_DAO_UNDERSCORE) || path.endsWith(TAG_PATH_SUFFIX_DAO);
    }

    private static String tagPathToDaoKey(String tagPath) {
        if (tagPath.endsWith(TAG_PATH_SUFFIX_DAO_UNDERSCORE)) {
            tagPath =
                tagPath.substring(
                    0,
                    tagPath.length() - TAG_PATH_SUFFIX_DAO_UNDERSCORE.length()
                ) + TAG_PATH_SUFFIX_DAO;
        }
        return daoTagToKey(tagPath);
    }

    private static final String DAO_TAG_HUO_DAO_COMPAT = "huodao";

    private static String daoTagToKey(String daoTag) {
        if (DAO_TAG_HUO_DAO_COMPAT.equals(daoTag)) {
            return DaoHenHelper.DaoType.HUO_DAO.getKey();
        }

        for (DaoHenHelper.DaoType type : DaoHenHelper.DaoType.values()) {
            if (type != null && type.getKey().equals(daoTag)) {
                return type.getKey();
            }
        }
        return daoTag.replace("_", "");
    }

    private static final int QUALITY_TIER_SPIRIT_THRESHOLD = 10;
    private static final int QUALITY_TIER_MYSTIC_THRESHOLD = 20;
    private static final int QUALITY_TIER_EARTH_THRESHOLD = 35;
    private static final int QUALITY_TIER_HEAVEN_THRESHOLD = 50;
    private static final int QUALITY_TIER_KING_THRESHOLD = 70;

    private static SwordQuality computeQualityFromDaoMarks(
        Map<String, Integer> marks
    ) {
        int total = 0;
        if (marks != null) {
            for (Integer v : marks.values()) {
                if (v != null && v > 0) {
                    total += v;
                }
            }
        }

        if (total < QUALITY_TIER_SPIRIT_THRESHOLD) {
            return SwordQuality.COMMON;
        }
        if (total < QUALITY_TIER_MYSTIC_THRESHOLD) {
            return SwordQuality.SPIRIT;
        }
        if (total < QUALITY_TIER_EARTH_THRESHOLD) {
            return SwordQuality.MYSTIC;
        }
        if (total < QUALITY_TIER_HEAVEN_THRESHOLD) {
            return SwordQuality.EARTH;
        }
        if (total < QUALITY_TIER_KING_THRESHOLD) {
            return SwordQuality.HEAVEN;
        }
        return SwordQuality.KING;
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

        com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes attrs =
            new com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes(
                computeQualityFromDaoMarks(forge.getDaoMarks())
            );
        attrs.getImprint().setMarks(forge.getDaoMarks());
        attrs.fullRestoreDurability();

        recalled.attributes = attrs.toNBT();
        recalled.quality = attrs.getQuality();
        recalled.level = attrs.getLevel();
        recalled.experience = attrs.getExperience();
        recalled.durability = (float) attrs.durability;

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
