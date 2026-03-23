package com.Kizunad.guzhenrenext.xianqiao.alchemy.item;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.effect.PillEffectState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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

    private static final String LING_ZHI_YE_ITEM_PATH = "ling_zhi_ye";

    private static final String HU_TI_DAN_ITEM_PATH = "hu_ti_dan";

    private static final String HUI_CHUN_DAN_ITEM_PATH = "hui_chun_dan";

    private static final String RUN_ZE_DAN_ITEM_PATH = "run_ze_dan";

    private static final String PO_HUAN_DAN_ITEM_PATH = "po_huan_dan";

    private static final String YE_SHI_DAN_ITEM_PATH = "ye_shi_dan";

    private static final String QING_SHEN_DAN_ITEM_PATH = "qing_shen_dan";

    private static final String XIAO_HUAN_DAN_ITEM_PATH = "xiao_huan_dan";

    private static final String BAO_SHI_DAN_ITEM_PATH = "bao_shi_dan";

    private static final String BI_GU_DAN_ITEM_PATH = "bi_gu_dan";

    private static final String GUI_XI_DAN_ITEM_PATH = "gui_xi_dan";

    private static final String BI_HUO_DAN_ITEM_PATH = "bi_huo_dan";

    private static final String BI_DU_DAN_ITEM_PATH = "bi_du_dan";

    private static final String YIN_XI_DAN_ITEM_PATH = "yin_xi_dan";

    private static final String NING_SHEN_DAN_ITEM_PATH = "ning_shen_dan";

    private static final String JI_FENG_DAN_ITEM_PATH = "ji_feng_dan";

    private static final String TIE_GU_DAN_ITEM_PATH = "tie_gu_dan";

    private static final String KUANG_BAO_DAN_ITEM_PATH = "kuang_bao_dan";

    private static final String CUI_TI_DAN_ITEM_PATH = "cui_ti_dan";

    private static final String JU_QI_SAN_ITEM_PATH = "ju_qi_san";

    private static final String QU_SHOU_SAN_ITEM_PATH = "qu_shou_san";

    private static final String SHOU_LIANG_WAN_ITEM_PATH = "shou_liang_wan";

    private static final String XUN_MAI_DAN_ITEM_PATH = "xun_mai_dan";

    private static final int TICKS_PER_SECOND = 20;

    private static final int SECONDS_PER_MINUTE = 60;

    private static final int CUI_SHENG_DURATION_MINUTES = 10;

    private static final int CUI_SHENG_CONSUME_COUNT = 1;

    private static final int LING_ZHI_YE_CONSUME_COUNT = 1;

    private static final int HU_TI_CONSUME_COUNT = 1;

    private static final int HUI_CHUN_CONSUME_COUNT = 1;

    private static final int RUN_ZE_CONSUME_COUNT = 1;

    private static final int PO_HUAN_DAN_CONSUME_COUNT = 1;

    private static final int RUN_ZE_DURATION_MINUTES = 10;

    private static final int YE_SHI_CONSUME_COUNT = 1;
    private static final int YE_SHI_DURATION_MINUTES = 5;

    private static final int QING_SHEN_CONSUME_COUNT = 1;
    private static final int QING_SHEN_DURATION_MINUTES = 5;

    private static final int XIAO_HUAN_CONSUME_COUNT = 1;

    private static final int BAO_SHI_CONSUME_COUNT = 1;

    private static final int BI_GU_CONSUME_COUNT = 1;

    private static final int GUI_XI_CONSUME_COUNT = 1;

    private static final int BI_HUO_CONSUME_COUNT = 1;

    private static final int BI_DU_CONSUME_COUNT = 1;

    private static final int YIN_XI_CONSUME_COUNT = 1;

    private static final int NING_SHEN_CONSUME_COUNT = 1;

    private static final int JI_FENG_CONSUME_COUNT = 1;

    private static final int TIE_GU_CONSUME_COUNT = 1;

    private static final int KUANG_BAO_CONSUME_COUNT = 1;

    private static final int CUI_TI_CONSUME_COUNT = 1;

    private static final int JU_QI_SAN_CONSUME_COUNT = 1;

    private static final int QU_SHOU_SAN_CONSUME_COUNT = 1;

    private static final int SHOU_LIANG_WAN_CONSUME_COUNT = 1;

    private static final int XUN_MAI_DAN_CONSUME_COUNT = 1;

    private static final int XUN_MAI_DAN_SCAN_RADIUS = 3;

    private static final int XUN_MAI_DAN_GLOWING_DURATION_SECONDS = 5;

    private static final double JU_QI_SAN_BASE_RECOVER_AMOUNT = 5.0D;

    private static final double JU_QI_SAN_RECOVERY_APPLIED_EPSILON = 0.0001D;

    private static final double QU_SHOU_SAN_RADIUS = 4.0D;

    private static final double QU_SHOU_SAN_PUSH_DISTANCE = 1.2D;

    private static final double QU_SHOU_SAN_DIRECTION_EPSILON = 0.0001D;

    private static final double SHOU_LIANG_WAN_RADIUS = 4.0D;

    private static final int XUN_MAI_DAN_GLOWING_DURATION_TICKS =
        TICKS_PER_SECOND * XUN_MAI_DAN_GLOWING_DURATION_SECONDS;

    private static final int GUI_XI_DURATION_MINUTES = 5;

    private static final int BI_HUO_DURATION_MINUTES = 5;

    private static final int YIN_XI_DURATION_MINUTES = 5;

    private static final int NING_SHEN_DURATION_MINUTES = 5;

    private static final int JI_FENG_DURATION_MINUTES = 5;

    private static final int TIE_GU_DURATION_MINUTES = 5;

    private static final int KUANG_BAO_DURATION_MINUTES = 5;

    private static final int CUI_TI_DURATION_MINUTES = 5;

    private static final int BAO_SHI_HUNGER_RESTORE = 2;

    private static final float BAO_SHI_SATURATION_RESTORE = 0.5F;

    private static final int BI_GU_HUNGER_RESTORE = 6;

    private static final float BI_GU_SATURATION_RESTORE = 1.5F;

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

    private static final long YE_SHI_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * YE_SHI_DURATION_MINUTES;

    private static final long QING_SHEN_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * QING_SHEN_DURATION_MINUTES;

    private static final long GUI_XI_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * GUI_XI_DURATION_MINUTES;

    private static final long BI_HUO_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * BI_HUO_DURATION_MINUTES;

    private static final long YIN_XI_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * YIN_XI_DURATION_MINUTES;

    private static final long NING_SHEN_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * NING_SHEN_DURATION_MINUTES;

    private static final long JI_FENG_BASE_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * JI_FENG_DURATION_MINUTES;

    private static final long TIE_GU_BASE_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * TIE_GU_DURATION_MINUTES;

    private static final long KUANG_BAO_BASE_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * KUANG_BAO_DURATION_MINUTES;

    private static final long CUI_TI_BASE_DURATION_TICKS =
        (long) TICKS_PER_SECOND * SECONDS_PER_MINUTE * CUI_TI_DURATION_MINUTES;

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
        if (isLingZhiYe(stack)) {
            return useLingZhiYe(level, player, stack);
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
        if (isPoHuanDan(stack)) {
            return usePoHuanDan(player, stack);
        }
        if (isYeShiDan(stack)) {
            return useYeShiDan(level, player, stack);
        }
        if (isQingShenDan(stack)) {
            return useQingShenDan(level, player, stack);
        }
        if (isXiaoHuanDan(stack)) {
            return useXiaoHuanDan(player, stack);
        }
        if (isBaoShiDan(stack)) {
            return useBaoShiDan(player, stack);
        }
        if (isBiGuDan(stack)) {
            return useBiGuDan(player, stack);
        }
        if (isGuiXiDan(stack)) {
            return useGuiXiDan(player, stack);
        }
        if (isBiHuoDan(stack)) {
            return useBiHuoDan(player, stack);
        }
        if (isBiDuDan(stack)) {
            return useBiDuDan(player, stack);
        }
        if (isYinXiDan(stack)) {
            return useYinXiDan(player, stack);
        }
        if (isNingShenDan(stack)) {
            return useNingShenDan(player, stack);
        }
        if (isJiFengDan(stack)) {
            return useJiFengDan(player, stack);
        }
        if (isTieGuDan(stack)) {
            return useTieGuDan(player, stack);
        }
        if (isKuangBaoDan(stack)) {
            return useKuangBaoDan(player, stack);
        }
        if (isCuiTiDan(stack)) {
            return useCuiTiDan(player, stack);
        }
        if (isJuQiSan(stack)) {
            return useJuQiSan(player, stack);
        }
        if (isQuShouSan(stack)) {
            return useQuShouSan(player, stack);
        }
        if (isShouLiangWan(stack)) {
            return useShouLiangWan(player, stack);
        }
        if (isXunMaiDan(stack)) {
            return useXunMaiDan(player, stack);
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

    private static InteractionResultHolder<ItemStack> useLingZhiYe(Level level, Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        double growthMultiplier = currentQuality.getEffectMultiplier();
        PillEffectState.writeGrowthAccelerationState(
            player,
            level.getGameTime(),
            CUI_SHENG_DURATION_TICKS,
            growthMultiplier
        );
        stack.shrink(LING_ZHI_YE_CONSUME_COUNT);
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

    private static InteractionResultHolder<ItemStack> usePoHuanDan(Player player, ItemStack stack) {
        player.removeEffect(MobEffects.BLINDNESS);
        player.removeEffect(MobEffects.CONFUSION);
        stack.shrink(PO_HUAN_DAN_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useYeShiDan(Level level, Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        long durationTicks = (long) (YE_SHI_DURATION_TICKS * currentQuality.getEffectMultiplier());
        MobEffectInstance nightVision = new MobEffectInstance(
            MobEffects.NIGHT_VISION,
            (int) durationTicks,
            0,
            false,
            true
        );
        player.addEffect(nightVision);
        stack.shrink(YE_SHI_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useQingShenDan(Level level, Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        long durationTicks = (long) (QING_SHEN_DURATION_TICKS * currentQuality.getEffectMultiplier());
        MobEffectInstance slowFalling = new MobEffectInstance(
            MobEffects.SLOW_FALLING,
            (int) durationTicks,
            0,
            false,
            true
        );
        player.addEffect(slowFalling);
        stack.shrink(QING_SHEN_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useXiaoHuanDan(Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        float healAmount = resolveHuiChunHealAmount(currentQuality);
        player.heal(healAmount);
        stack.shrink(XIAO_HUAN_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useBaoShiDan(Player player, ItemStack stack) {
        player.getFoodData().eat(BAO_SHI_HUNGER_RESTORE, BAO_SHI_SATURATION_RESTORE);
        stack.shrink(BAO_SHI_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useBiGuDan(Player player, ItemStack stack) {
        player.getFoodData().eat(BI_GU_HUNGER_RESTORE, BI_GU_SATURATION_RESTORE);
        stack.shrink(BI_GU_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useGuiXiDan(Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        long durationTicks = (long) (GUI_XI_DURATION_TICKS * currentQuality.getEffectMultiplier());
        MobEffectInstance waterBreathing = new MobEffectInstance(
            MobEffects.WATER_BREATHING,
            (int) durationTicks,
            0,
            false,
            true
        );
        player.addEffect(waterBreathing);
        stack.shrink(GUI_XI_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useBiHuoDan(Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        long durationTicks = (long) (BI_HUO_DURATION_TICKS * currentQuality.getEffectMultiplier());
        MobEffectInstance fireResistance = new MobEffectInstance(
            MobEffects.FIRE_RESISTANCE,
            (int) durationTicks,
            0,
            false,
            true
        );
        player.addEffect(fireResistance);
        stack.shrink(BI_HUO_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useBiDuDan(Player player, ItemStack stack) {
        player.removeEffect(MobEffects.POISON);
        stack.shrink(BI_DU_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useYinXiDan(Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        long durationTicks = (long) (YIN_XI_DURATION_TICKS * currentQuality.getEffectMultiplier());
        MobEffectInstance invisibility = new MobEffectInstance(
            MobEffects.INVISIBILITY,
            (int) durationTicks,
            0,
            false,
            true
        );
        player.addEffect(invisibility);
        stack.shrink(YIN_XI_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useNingShenDan(Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        long durationTicks = (long) (NING_SHEN_DURATION_TICKS * currentQuality.getEffectMultiplier());
        // 以原版幸运作为“凝神”最小可观测代理，避免在浅度切片引入新系统。
        MobEffectInstance luck = new MobEffectInstance(
            MobEffects.LUCK,
            (int) durationTicks,
            0,
            false,
            true
        );
        player.addEffect(luck);
        stack.shrink(NING_SHEN_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useJiFengDan(Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        long scaledDurationTicks = Math.round(JI_FENG_BASE_DURATION_TICKS * currentQuality.getEffectMultiplier());
        int effectDurationTicks = (int) Math.max(1L, scaledDurationTicks);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, effectDurationTicks));
        stack.shrink(JI_FENG_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useTieGuDan(Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        long scaledDurationTicks = Math.round(TIE_GU_BASE_DURATION_TICKS * currentQuality.getEffectMultiplier());
        int effectDurationTicks = (int) Math.max(1L, scaledDurationTicks);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, effectDurationTicks));
        stack.shrink(TIE_GU_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useKuangBaoDan(Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        long scaledDurationTicks = Math.round(KUANG_BAO_BASE_DURATION_TICKS * currentQuality.getEffectMultiplier());
        int effectDurationTicks = (int) Math.max(1L, scaledDurationTicks);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, effectDurationTicks));
        stack.shrink(KUANG_BAO_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useCuiTiDan(Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        long scaledDurationTicks = Math.round(CUI_TI_BASE_DURATION_TICKS * currentQuality.getEffectMultiplier());
        int effectDurationTicks = (int) Math.max(1L, scaledDurationTicks);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, effectDurationTicks));
        stack.shrink(CUI_TI_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useJuQiSan(Player player, ItemStack stack) {
        PillQuality currentQuality = readQuality(stack);
        double recoverAmount = JU_QI_SAN_BASE_RECOVER_AMOUNT * currentQuality.getEffectMultiplier();
        if (!applyJuQiSanRecovery(player, recoverAmount)) {
            return InteractionResultHolder.fail(stack);
        }
        stack.shrink(JU_QI_SAN_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static boolean applyJuQiSanRecovery(Player player, double recoverAmount) {
        try {
            double zhenYuanBeforeRecover = ZhenYuanHelper.getAmount(player);
            ZhenYuanHelper.modify(player, recoverAmount);
            double zhenYuanAfterRecover = ZhenYuanHelper.getAmount(player);
            return zhenYuanAfterRecover > zhenYuanBeforeRecover + JU_QI_SAN_RECOVERY_APPLIED_EPSILON;
        } catch (NoClassDefFoundError error) {
            return false;
        }
    }

    private static InteractionResultHolder<ItemStack> useQuShouSan(Player player, ItemStack stack) {
        AABB searchArea = player.getBoundingBox().inflate(QU_SHOU_SAN_RADIUS);
        for (Monster target : player.level().getEntitiesOfClass(Monster.class, searchArea)) {
            if (!target.isAlive()) {
                continue;
            }
            double offsetX = target.getX() - player.getX();
            double offsetZ = target.getZ() - player.getZ();
            double horizontalDistance = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
            double normalizedX = horizontalDistance < QU_SHOU_SAN_DIRECTION_EPSILON
                ? 1.0D
                : offsetX / horizontalDistance;
            double normalizedZ = horizontalDistance < QU_SHOU_SAN_DIRECTION_EPSILON
                ? 0.0D
                : offsetZ / horizontalDistance;
            target.setPos(
                target.getX() + normalizedX * QU_SHOU_SAN_PUSH_DISTANCE,
                target.getY(),
                target.getZ() + normalizedZ * QU_SHOU_SAN_PUSH_DISTANCE
            );
        }
        stack.shrink(QU_SHOU_SAN_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useShouLiangWan(Player player, ItemStack stack) {
        AABB searchArea = player.getBoundingBox().inflate(SHOU_LIANG_WAN_RADIUS);
        for (Animal target : player.level().getEntitiesOfClass(Animal.class, searchArea)) {
            if (!target.isAlive() || !target.canFallInLove()) {
                continue;
            }
            target.setInLove(player);
        }
        stack.shrink(SHOU_LIANG_WAN_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static InteractionResultHolder<ItemStack> useXunMaiDan(Player player, ItemStack stack) {
        if (containsTargetOreNearby(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, XUN_MAI_DAN_GLOWING_DURATION_TICKS));
        }
        stack.shrink(XUN_MAI_DAN_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    private static boolean containsTargetOreNearby(Player player) {
        BlockPos center = player.blockPosition();
        BlockPos min = center.offset(-XUN_MAI_DAN_SCAN_RADIUS, -XUN_MAI_DAN_SCAN_RADIUS, -XUN_MAI_DAN_SCAN_RADIUS);
        BlockPos max = center.offset(XUN_MAI_DAN_SCAN_RADIUS, XUN_MAI_DAN_SCAN_RADIUS, XUN_MAI_DAN_SCAN_RADIUS);
        for (BlockPos currentPos : BlockPos.betweenClosed(min, max)) {
            BlockState state = player.level().getBlockState(currentPos);
            if (isTargetOreBlock(state)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTargetOreBlock(BlockState state) {
        return state.is(Blocks.IRON_ORE)
            || state.is(Blocks.DEEPSLATE_IRON_ORE)
            || state.is(Blocks.GOLD_ORE)
            || state.is(Blocks.DEEPSLATE_GOLD_ORE)
            || state.is(Blocks.DIAMOND_ORE)
            || state.is(Blocks.DEEPSLATE_DIAMOND_ORE);
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

    private static boolean isLingZhiYe(ItemStack stack) {
        return isPillItemPath(stack, LING_ZHI_YE_ITEM_PATH);
    }

    private static boolean isHuiChunDan(ItemStack stack) {
        return isPillItemPath(stack, HUI_CHUN_DAN_ITEM_PATH);
    }

    private static boolean isRunZeDan(ItemStack stack) {
        return isPillItemPath(stack, RUN_ZE_DAN_ITEM_PATH);
    }

    private static boolean isPoHuanDan(ItemStack stack) {
        return isPillItemPath(stack, PO_HUAN_DAN_ITEM_PATH);
    }

    private static boolean isYeShiDan(ItemStack stack) {
        return isPillItemPath(stack, YE_SHI_DAN_ITEM_PATH);
    }

    private static boolean isQingShenDan(ItemStack stack) {
        return isPillItemPath(stack, QING_SHEN_DAN_ITEM_PATH);
    }

    private static boolean isXiaoHuanDan(ItemStack stack) {
        return isPillItemPath(stack, XIAO_HUAN_DAN_ITEM_PATH);
    }

    private static boolean isBaoShiDan(ItemStack stack) {
        return isPillItemPath(stack, BAO_SHI_DAN_ITEM_PATH);
    }

    private static boolean isBiGuDan(ItemStack stack) {
        return isPillItemPath(stack, BI_GU_DAN_ITEM_PATH);
    }

    private static boolean isGuiXiDan(ItemStack stack) {
        return isPillItemPath(stack, GUI_XI_DAN_ITEM_PATH);
    }

    private static boolean isBiHuoDan(ItemStack stack) {
        return isPillItemPath(stack, BI_HUO_DAN_ITEM_PATH);
    }

    private static boolean isBiDuDan(ItemStack stack) {
        return isPillItemPath(stack, BI_DU_DAN_ITEM_PATH);
    }

    private static boolean isYinXiDan(ItemStack stack) {
        return isPillItemPath(stack, YIN_XI_DAN_ITEM_PATH);
    }

    private static boolean isNingShenDan(ItemStack stack) {
        return isPillItemPath(stack, NING_SHEN_DAN_ITEM_PATH);
    }

    private static boolean isJiFengDan(ItemStack stack) {
        return isPillItemPath(stack, JI_FENG_DAN_ITEM_PATH);
    }

    private static boolean isTieGuDan(ItemStack stack) {
        return isPillItemPath(stack, TIE_GU_DAN_ITEM_PATH);
    }

    private static boolean isKuangBaoDan(ItemStack stack) {
        return isPillItemPath(stack, KUANG_BAO_DAN_ITEM_PATH);
    }

    private static boolean isCuiTiDan(ItemStack stack) {
        return isPillItemPath(stack, CUI_TI_DAN_ITEM_PATH);
    }

    private static boolean isJuQiSan(ItemStack stack) {
        return isPillItemPath(stack, JU_QI_SAN_ITEM_PATH);
    }

    private static boolean isQuShouSan(ItemStack stack) {
        return isPillItemPath(stack, QU_SHOU_SAN_ITEM_PATH);
    }

    private static boolean isShouLiangWan(ItemStack stack) {
        return isPillItemPath(stack, SHOU_LIANG_WAN_ITEM_PATH);
    }

    private static boolean isXunMaiDan(ItemStack stack) {
        return isPillItemPath(stack, XUN_MAI_DAN_ITEM_PATH);
    }

    private static boolean isSupportedPill(ItemStack stack) {
        return isCuiShengDan(stack)
            || isLingZhiYe(stack)
            || isHuTiDan(stack)
            || isHuiChunDan(stack)
            || isRunZeDan(stack)
            || isPoHuanDan(stack)
            || isYeShiDan(stack)
            || isQingShenDan(stack)
            || isXiaoHuanDan(stack)
            || isBaoShiDan(stack)
            || isBiGuDan(stack)
            || isGuiXiDan(stack)
            || isBiHuoDan(stack)
            || isBiDuDan(stack)
            || isYinXiDan(stack)
            || isNingShenDan(stack)
            || isJiFengDan(stack)
            || isTieGuDan(stack)
            || isKuangBaoDan(stack)
            || isCuiTiDan(stack)
            || isJuQiSan(stack)
            || isQuShouSan(stack)
            || isShouLiangWan(stack)
            || isXunMaiDan(stack);
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
        if (isLingZhiYe(stack)) {
            double multiplier = safeQuality.getEffectMultiplier();
            return Component.literal(
                "效果: 植物促生加速 x" + formatNumber(multiplier) + "（持续10分钟）"
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
        if (isPoHuanDan(stack)) {
            return Component.literal("效果: 立即清除失明与幻觉").withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isYeShiDan(stack)) {
            long duration = (long) (YE_SHI_DURATION_TICKS * safeQuality.getEffectMultiplier());
            double minutes = duration / (double) TICKS_PER_SECOND / SECONDS_PER_MINUTE;
            return Component.literal(
                "效果: 夜视（持续" + formatNumber(minutes) + "分钟）"
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isQingShenDan(stack)) {
            long duration = (long) (QING_SHEN_DURATION_TICKS * safeQuality.getEffectMultiplier());
            double minutes = duration / (double) TICKS_PER_SECOND / SECONDS_PER_MINUTE;
            return Component.literal(
                "效果: 缓降（持续" + formatNumber(minutes) + "分钟）"
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isXiaoHuanDan(stack)) {
            float healAmount = resolveHuiChunHealAmount(safeQuality);
            return Component.literal(
                "效果: 立即回复生命 +" + formatNumber(healAmount)
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isBaoShiDan(stack)) {
            return Component.literal(
                "效果: 恢复饱食 +" + BAO_SHI_HUNGER_RESTORE + "，恢复饱和 +" + formatNumber(BAO_SHI_SATURATION_RESTORE)
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isBiGuDan(stack)) {
            return Component.literal(
                "效果: 立即恢复饱食 +" + BI_GU_HUNGER_RESTORE + "，立即恢复饱和 +" + formatNumber(BI_GU_SATURATION_RESTORE)
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isGuiXiDan(stack)) {
            long duration = (long) (GUI_XI_DURATION_TICKS * safeQuality.getEffectMultiplier());
            double minutes = duration / (double) TICKS_PER_SECOND / SECONDS_PER_MINUTE;
            return Component.literal(
                "效果: 水下呼吸（持续" + formatNumber(minutes) + "分钟）"
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isBiHuoDan(stack)) {
            long duration = (long) (BI_HUO_DURATION_TICKS * safeQuality.getEffectMultiplier());
            double minutes = duration / (double) TICKS_PER_SECOND / SECONDS_PER_MINUTE;
            return Component.literal(
                "效果: 抗火（持续" + formatNumber(minutes) + "分钟）"
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isBiDuDan(stack)) {
            return Component.literal("效果: 立即清除中毒").withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isYinXiDan(stack)) {
            long duration = (long) (YIN_XI_DURATION_TICKS * safeQuality.getEffectMultiplier());
            double minutes = duration / (double) TICKS_PER_SECOND / SECONDS_PER_MINUTE;
            return Component.literal(
                "效果: 隐身（持续" + formatNumber(minutes) + "分钟）"
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isNingShenDan(stack)) {
            long duration = (long) (NING_SHEN_DURATION_TICKS * safeQuality.getEffectMultiplier());
            double minutes = duration / (double) TICKS_PER_SECOND / SECONDS_PER_MINUTE;
            return Component.literal(
                "效果: 幸运（持续" + formatNumber(minutes) + "分钟）"
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isJiFengDan(stack)) {
            double durationMinutes = JI_FENG_DURATION_MINUTES * safeQuality.getEffectMultiplier();
            String tooltipText = "效果: 速度提升（持续" + formatNumber(durationMinutes) + "分钟）";
            return Component.literal(tooltipText).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isTieGuDan(stack)) {
            double durationMinutes = TIE_GU_DURATION_MINUTES * safeQuality.getEffectMultiplier();
            String tooltipText = "效果: 抗性提升（持续" + formatNumber(durationMinutes) + "分钟）";
            return Component.literal(tooltipText).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isKuangBaoDan(stack)) {
            double durationMinutes = KUANG_BAO_DURATION_MINUTES * safeQuality.getEffectMultiplier();
            String tooltipText = "效果: 攻击提升（持续" + formatNumber(durationMinutes) + "分钟）";
            return Component.literal(tooltipText).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isCuiTiDan(stack)) {
            double durationMinutes = CUI_TI_DURATION_MINUTES * safeQuality.getEffectMultiplier();
            String tooltipText = "效果: 力量提升（持续" + formatNumber(durationMinutes) + "分钟）";
            return Component.literal(tooltipText).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isJuQiSan(stack)) {
            double recoverAmount = JU_QI_SAN_BASE_RECOVER_AMOUNT * safeQuality.getEffectMultiplier();
            return Component.literal(
                "效果: 立即恢复真元 +" + formatNumber(recoverAmount)
            ).withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isQuShouSan(stack)) {
            return Component.literal("效果: 驱离附近敌对生物（小范围）").withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isShouLiangWan(stack)) {
            return Component.literal("效果: 促使附近可繁殖动物进入求偶状态").withStyle(ChatFormatting.DARK_GRAY);
        }
        if (isXunMaiDan(stack)) {
            return Component.literal("效果: 扫描附近小范围矿石，命中时短暂发光提示").withStyle(ChatFormatting.DARK_GRAY);
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
