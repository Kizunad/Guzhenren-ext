package com.Kizunad.guzhenrenext.xianqiao.alchemy.item;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.effect.PillEffectState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 丹药基础物品。
 * <p>
 * 仅提供最小品质字段承载，供后续系统读取。
 * </p>
 */
public class PillItem extends Item {

    /** 品质在丹药自定义 NBT 中的字段键。 */
    private static final String TAG_PILL_QUALITY = "pill_quality";

    private static final String CUI_SHENG_DAN_ITEM_PATH = "cui_sheng_dan";

    private static final String HU_TI_DAN_ITEM_PATH = "hu_ti_dan";

    private static final String HUI_CHUN_DAN_ITEM_PATH = "hui_chun_dan";

    private static final String RUN_ZE_DAN_ITEM_PATH = "run_ze_dan";

    private static final int TICKS_PER_SECOND = 20;

    private static final int SECONDS_PER_MINUTE = 60;

    private static final int CUI_SHENG_DURATION_MINUTES = 10;

    private static final int CUI_SHENG_CONSUME_COUNT = 1;

    private static final int HU_TI_CONSUME_COUNT = 1;

    private static final int HUI_CHUN_CONSUME_COUNT = 1;

    private static final int RUN_ZE_CONSUME_COUNT = 1;

    private static final int RUN_ZE_DURATION_MINUTES = 10;

    private static final float HUI_CHUN_HEAL_HUANG = 4.0F;

    private static final float HUI_CHUN_HEAL_DI = 6.0F;

    private static final float HUI_CHUN_HEAL_XUAN = 8.0F;

    private static final float HUI_CHUN_HEAL_TIAN = 12.0F;

    private static final long CUI_SHENG_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * CUI_SHENG_DURATION_MINUTES;

    private static final int HU_TI_DURATION_MINUTES = 5;

    private static final long HU_TI_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * HU_TI_DURATION_MINUTES;

    private static final long RUN_ZE_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * RUN_ZE_DURATION_MINUTES;

    /** 数值显示为整数时使用的浮点比较容差。 */
    private static final double INTEGER_DISPLAY_EPSILON = 0.0001D;

    /** 丹药品质。 */
    private final PillQuality quality;

    public PillItem(Properties properties, PillQuality quality) {
        super(properties);
        if (quality == null) {
            throw new IllegalArgumentException("quality cannot be null");
        }
        this.quality = quality;
    }

    /**
     * 获取丹药品质。
     *
     * @return 品质枚举
     */
    public PillQuality getQuality() {
        return quality;
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        TooltipContext context,
        List<Component> tooltipComponents,
        TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, flag);
        if (!isSupportedPill(stack)) {
            return;
        }
        PillQuality currentQuality = readQuality(stack);
        tooltipComponents.add(buildQualityTooltip(currentQuality));
        tooltipComponents.add(buildEffectTooltip(stack, currentQuality));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isSupportedPill(stack)) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        if (isCuiShengDan(stack)) {
            return useCuiShengDan(level, player, stack);
        }
        if (isHuTiDan(stack)) {
            return useHuTiDan(level, player, stack);
        }
        if (isHuiChunDan(stack)) {
            return useHuiChunDan(player, stack);
        }
        if (isRunZeDan(stack)) {
            return useRunZeDan(level, player, stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    private static InteractionResultHolder<ItemStack> useCuiShengDan(Level level, Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        double growthMultiplier = currentQuality.getEffectMultiplier();
        PillEffectState.writeGrowthAccelerationState(
            player,
            level.getGameTime(),
            CUI_SHENG_DURATION_TICKS,
            growthMultiplier
        );
        stack.shrink(CUI_SHENG_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useHuTiDan(Level level, Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        PillEffectState.writeBodyDefenseState(
            player,
            level.getGameTime(),
            HU_TI_DURATION_TICKS,
            currentQuality
        );
        double defenseAmount = PillEffectState.resolveBodyDefenseAmount(currentQuality);
        PillEffectState.applyBodyDefenseModifier(player, defenseAmount);
        stack.shrink(HU_TI_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useHuiChunDan(Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        float healAmount = resolveHuiChunHealAmount(currentQuality);
        player.heal(healAmount);
        stack.shrink(HUI_CHUN_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useRunZeDan(Level level, Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        double accelerationMultiplier = PillEffectState.resolveRunZeAccelerationMultiplier(currentQuality);
        PillEffectState.writeRunZeAccelerationState(
            player,
            level.getGameTime(),
            RUN_ZE_DURATION_TICKS,
            accelerationMultiplier
        );
        stack.shrink(RUN_ZE_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    /**
     * 解析回春丹即时治疗量。
     * <p>
     * 业务约束为固定映射：HUANG=4、DI=6、XUAN=8、TIAN=12。
     * 将映射集中在单一函数，避免后续维护时出现数值漂移。
     * </p>
     *
     * @param quality 丹药品质
     * @return 对应治疗量
     */
    private static float resolveHuiChunHealAmount(PillQuality quality) {
        PillQuality safeQuality = quality == null ? PillQuality.HUANG : quality;
        return switch (safeQuality) {
            case HUANG -> HUI_CHUN_HEAL_HUANG;
            case DI -> HUI_CHUN_HEAL_DI;
            case XUAN -> HUI_CHUN_HEAL_XUAN;
            case TIAN -> HUI_CHUN_HEAL_TIAN;
        };
    }

    /**
     * 将品质写入丹药堆栈。
     * <p>
     * 本方法统一使用 CustomData 承载 NBT 语义，避免在业务层散落字符串键。
     * </p>
     *
     * @param stack 丹药堆栈
     * @param quality 品质
     */
    public static void writeQuality(ItemStack stack, PillQuality quality) {
        if (stack == null || stack.isEmpty() || quality == null) {
            return;
        }
        CompoundTag customDataTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        customDataTag.putString(TAG_PILL_QUALITY, quality.getSerializedName());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customDataTag));
    }

    /**
     * 从丹药堆栈读取品质。
     * <p>
     * 读取优先级：
     * 1) 堆栈 NBT 中显式写入的品质；
     * 2) 物品注册时的默认品质；
     * 3) `PillQuality` 默认值（黄）。
     * </p>
     *
     * @param stack 丹药堆栈
     * @return 解析出的品质
     */
    public static PillQuality readQuality(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return PillQuality.fromSerializedName(null);
        }
        CompoundTag customDataTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (customDataTag.contains(TAG_PILL_QUALITY, Tag.TAG_STRING)) {
            String serializedName = customDataTag.getString(TAG_PILL_QUALITY);
            return PillQuality.fromSerializedName(serializedName);
        }
        if (stack.getItem() instanceof PillItem pillItem) {
            return pillItem.getQuality();
        }
        return PillQuality.fromSerializedName(null);
    }

    private static boolean isCuiShengDan(ItemStack stack) {
        return isPillItemPath(stack, CUI_SHENG_DAN_ITEM_PATH);
    }

    private static boolean isHuTiDan(ItemStack stack) {
        return isPillItemPath(stack, HU_TI_DAN_ITEM_PATH);
    }

    private static boolean isHuiChunDan(ItemStack stack) {
        return isPillItemPath(stack, HUI_CHUN_DAN_ITEM_PATH);
    }

    private static boolean isRunZeDan(ItemStack stack) {
        return isPillItemPath(stack, RUN_ZE_DAN_ITEM_PATH);
    }

    private static boolean isSupportedPill(ItemStack stack) {
        return isCuiShengDan(stack) || isHuTiDan(stack) || isHuiChunDan(stack) || isRunZeDan(stack);
    }

    private static boolean isPillItemPath(ItemStack stack, String expectedPath) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return itemId != null
            && GuzhenrenExt.MODID.equals(itemId.getNamespace())
            && expectedPath.equals(itemId.getPath());
    }

    private static Component buildQualityTooltip(PillQuality quality) {
        PillQuality safeQuality = quality == null ? PillQuality.HUANG : quality;
        String qualityName = safeQuality.getDisplayName() + "品";
        return Component.literal("品质: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(qualityName).withStyle(resolveQualityColor(safeQuality)));
    }

    private static Component buildEffectTooltip(ItemStack stack, PillQuality quality) {
        PillQuality safeQuality = quality == null ? PillQuality.HUANG : quality;
        if (isCuiShengDan(stack)) {
            double multiplier = safeQuality.getEffectMultiplier();
            return Component.literal(
                "效果: 灵植生长加速 x" + formatNumber(multiplier) + "（持续10分钟）"
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isHuTiDan(stack)) {
            double defenseAmount = PillEffectState.resolveBodyDefenseAmount(safeQuality);
            return Component.literal(
                "效果: 护甲 +" + formatNumber(defenseAmount) + "（持续5分钟）"
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isHuiChunDan(stack)) {
            float healAmount = resolveHuiChunHealAmount(safeQuality);
            return Component.literal(
                "效果: 立即回复生命 +" + formatNumber(healAmount)
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isRunZeDan(stack)) {
            double multiplier = PillEffectState.resolveRunZeAccelerationMultiplier(safeQuality);
            return Component.literal(
                "效果: 产线倍率 x" + formatNumber(multiplier) + "（持续10分钟）"
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        return Component.literal("效果: -").withStyle(ChatFormatting.DARK_GRAY);
    }

    private static ChatFormatting resolveQualityColor(PillQuality quality) {
        PillQuality safeQuality = quality == null ? PillQuality.HUANG : quality;
        return switch (safeQuality) {
            case HUANG -> ChatFormatting.YELLOW;
            case DI -> ChatFormatting.AQUA;
            case XUAN -> ChatFormatting.LIGHT_PURPLE;
            case TIAN -> ChatFormatting.RED;
        };
    }

    private static String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < INTEGER_DISPLAY_EPSILON) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format("%.1f", value);
    }
}
