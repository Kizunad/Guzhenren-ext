package com.Kizunad.guzhenrenext.xianqiao.alchemy.service;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.blockentity.AlchemyFurnaceBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.PillItem;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.item.PillQuality;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.material.MaterialProperty;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.material.MaterialPropertyResolver;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.recipe.AlchemyRecipe;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 炼丹服务。
 * <p>
 * 本服务处理炼丹基础能力：
 * 1) 主材属性解析与丹药映射；
 * 2) 配方可用性校验；
 * 3) 输出槽可放入校验；
 * 4) 炼制成功率计算与随机判定；
 * 5) 成功后品质判定与品质写入。
 * </p>
 */
public final class AlchemyService {
    private static final int BASE_SUCCESS_RATE_PERCENT = 70;
    private static final int SUCCESS_RATE_BONUS_PER_AUXILIARY = 5;
    private static final int MAX_AUXILIARY_SLOTS = 4;
    private static final int MIN_SUCCESS_RATE_PERCENT = 0;
    private static final int MAX_SUCCESS_RATE_PERCENT = 100;
    private static final int OUTPUT_COUNT_PER_SUCCESS = 1;
    private static final double NINE_REFINEMENT_MARROW_HUNPO_COST = 2.0D;
    private static final Map<UUID, Double> TEST_REFINER_HUNPO_OVERRIDE = new HashMap<>();
    private static final Set<UUID> TEST_FORCE_NEXT_REFINE_FAILURE = new HashSet<>();

    private static final List<Integer> AUXILIARY_INPUT_SLOTS = List.of(
        AlchemyFurnaceBlockEntity.SLOT_AUX_1,
        AlchemyFurnaceBlockEntity.SLOT_AUX_2,
        AlchemyFurnaceBlockEntity.SLOT_AUX_3,
        AlchemyFurnaceBlockEntity.SLOT_AUX_4
    );

    private static final PillQuality[] PILL_QUALITIES = PillQuality.values();

    private static final int BASE_HUANG_PERCENT = 60;
    private static final int BASE_DI_PERCENT = 30;
    private static final int BASE_XUAN_PERCENT = 8;
    private static final int BASE_TIAN_PERCENT = 2;

    private static final int HUANG_REDUCTION_PER_AUXILIARY = 6;
    private static final int DI_BONUS_PER_AUXILIARY = 4;
    private static final int XUAN_BONUS_PER_AUXILIARY = 1;
    private static final int TIAN_BONUS_PER_AUXILIARY = 1;

    private static final List<AlchemyTierRecipeDefinition> TIERED_RECIPE_DEFINITIONS = createTieredRecipeDefinitions();

    private static final Map<AlchemyRecipeTier, List<AlchemyTierRecipeDefinition>> TIERED_RECIPES_BY_TIER =
        createTieredRecipesByTier(TIERED_RECIPE_DEFINITIONS);

    private AlchemyService() {
    }

    /**
     * 获取完整三阶丹方定义。
     *
     * @return 不可变丹方列表
     */
    public static List<AlchemyTierRecipeDefinition> getTieredRecipeDefinitions() {
        return TIERED_RECIPE_DEFINITIONS;
    }

    /**
     * 获取指定层级下的丹方定义。
     *
     * @param tier 丹方层级
     * @return 不可变丹方列表；tier 无效时返回空列表
     */
    public static List<AlchemyTierRecipeDefinition> getTieredRecipeDefinitionsByTier(AlchemyRecipeTier tier) {
        if (tier == null) {
            return List.of();
        }
        return TIERED_RECIPES_BY_TIER.getOrDefault(tier, List.of());
    }

    /**
     * 解析当前容器的可用配方。
     *
     * @param container 炼丹炉容器
     * @return 可用配方；无效主材时返回空
     */
    public static Optional<AlchemyRecipe> findRecipe(Container container) {
        if (container == null) {
            return Optional.empty();
        }
        ItemStack mainStack = container.getItem(AlchemyFurnaceBlockEntity.SLOT_MAIN);
        Optional<MaterialProperty> propertyOptional = MaterialPropertyResolver.resolve(mainStack);
        if (propertyOptional.isEmpty()) {
            return Optional.empty();
        }
        Item outputItem = mapOutputByMainProperty(propertyOptional.get());
        int auxiliaryCount = countAuxiliaryMaterials(container);
        int enhancementValue = auxiliaryCount;
        return Optional.of(new AlchemyRecipe(propertyOptional.get(), outputItem, auxiliaryCount, enhancementValue));
    }

    /**
     * 执行一次炼制。
     * <p>
     * 执行条件：
     * 1) 主材可解析出合法属性；
     * 2) 输出槽为空，或与目标丹药同类且未满；
     * 3) 满足前两条后必定消耗输入材料，再按成功率决定是否产出。
     * </p>
     *
     * @param container 炼丹炉容器
     * @return 完成一次合法炼制尝试返回 true（无论成功/失败），否则返回 false
     */
    public static boolean tryRefine(Container container) {
        return tryRefine(container, null);
    }

    public static boolean tryRefine(Container container, @Nullable ServerPlayer refiner) {
        Optional<TierRefinePlan> tierRefinePlanOptional = findTierRefinePlan(container);
        if (tierRefinePlanOptional.isPresent()) {
            return tryRefineByTierPlan(container, tierRefinePlanOptional.get(), refiner);
        }

        return tryRefineByLegacyRecipe(container, refiner);
    }

    /**
     * 执行“已命中三级丹方”的炼制流程。
     * <p>
     * 该分支严格按命中槽位消耗材料：
     * 1) 主材槽 0 作为锚点，命中后必消耗；
     * 2) 仅消耗为满足本次丹方而命中的辅材槽；
     * 3) 未参与本次丹方匹配的辅材槽不受影响。
     * </p>
     *
     * @param container 炼丹炉容器
     * @param tierRefinePlan 已命中的三级丹方计划
     * @return 完成一次合法炼制尝试返回 true；无法尝试返回 false
     */
    private static boolean tryRefineByTierPlan(
        Container container,
        TierRefinePlan tierRefinePlan,
        @Nullable ServerPlayer refiner
    ) {
        ItemStack currentOutput = container.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT);
        ItemStack recipeOutput = tierRefinePlan.createOutputStack();
        if (!canAttemptRefineWithOutputSlot(currentOutput, recipeOutput)) {
            return false;
        }

        boolean refineSucceeded = rollRefineSuccess(tierRefinePlan.auxiliaryCountForSuccessRate(), refiner);
        if (!refineSucceeded) {
            consumeInputMaterialsBySlots(container, tierRefinePlan.matchedInputSlots());
            placeNineRefinementMarrowCrystalOnRefineFailure(container, refiner);
            container.setChanged();
            return true;
        }

        PillQuality rolledQuality = rollPillQuality(tierRefinePlan.auxiliaryCountForSuccessRate());
        PillQuality boundedQuality = clampQualityToRange(
            rolledQuality,
            tierRefinePlan.definition().minimumOutputQuality(),
            tierRefinePlan.definition().maximumOutputQuality()
        );
        PillItem.writeQuality(recipeOutput, boundedQuality);
        if (!canPlaceToOutputSlot(currentOutput, recipeOutput)) {
            consumeInputMaterialsBySlots(container, tierRefinePlan.matchedInputSlots());
            container.setChanged();
            return true;
        }

        consumeInputMaterialsBySlots(container, tierRefinePlan.matchedInputSlots());
        placeOutput(container, recipeOutput);
        container.setChanged();
        return true;
    }

    /**
     * 执行“兼容旧逻辑”的炼制流程。
     *
     * @param container 炼丹炉容器
     * @return 完成一次合法炼制尝试返回 true；无法尝试返回 false
     */
    private static boolean tryRefineByLegacyRecipe(Container container, @Nullable ServerPlayer refiner) {
        Optional<AlchemyRecipe> recipeOptional = findRecipe(container);
        if (recipeOptional.isEmpty()) {
            return false;
        }
        AlchemyRecipe recipe = recipeOptional.get();
        ItemStack currentOutput = container.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT);
        ItemStack recipeOutput = recipe.createOutputStack();
        if (!canAttemptRefineWithOutputSlot(currentOutput, recipeOutput)) {
            return false;
        }

        boolean refineSucceeded = rollRefineSuccess(recipe.auxiliaryCount(), refiner);
        if (!refineSucceeded) {
            consumeLegacyInputMaterials(container);
            placeNineRefinementMarrowCrystalOnRefineFailure(container, refiner);
            container.setChanged();
            return true;
        }

        PillQuality rolledQuality = rollPillQuality(recipe.auxiliaryCount());
        PillItem.writeQuality(recipeOutput, rolledQuality);
        if (!canPlaceToOutputSlot(currentOutput, recipeOutput)) {
            // 当品质组件与现有产出堆叠不兼容时，本次已进入成功分支但无法落入产出槽。
            // 为保持“合法尝试必消耗输入材料”的单次语义一致性：
            // 1) 仍消耗输入；
            // 2) 不产出丹药；
            // 3) 返回 true，表示本次尝试已完成（但产出失败）。
            consumeLegacyInputMaterials(container);
            container.setChanged();
            return true;
        }

        consumeLegacyInputMaterials(container);
        placeOutput(container, recipeOutput);
        container.setChanged();
        return true;
    }

    /**
     * 解析当前容器是否命中三阶丹方。
     * <p>
     * 匹配规则：
     * 1) 输入池固定为槽位 0~4；
     * 2) 槽位 0（主材槽）必须命中丹方主材之一，作为锚点；
     * 3) 槽位 1~4（辅材槽）仅用于匹配其余要件（主材剩余项 + 前置丹药要求）；
     * 4) 全部要件满足则返回“本次命中的具体槽位”供后续精确扣减。
     * </p>
     *
     * @param container 炼丹炉容器
     * @return 命中时返回炼制计划，否则返回空
     */
    private static Optional<TierRefinePlan> findTierRefinePlan(Container container) {
        if (container == null) {
            return Optional.empty();
        }
        ItemStack mainStack = container.getItem(AlchemyFurnaceBlockEntity.SLOT_MAIN);
        if (mainStack.isEmpty()) {
            return Optional.empty();
        }
        Item mainItem = mainStack.getItem();

        for (AlchemyTierRecipeDefinition definition : TIERED_RECIPE_DEFINITIONS) {
            if (!definition.mainIngredients().contains(mainItem)) {
                continue;
            }
            Optional<TierRefinePlan> planOptional = tryBuildTierRefinePlanByDefinition(container, definition, mainItem);
            if (planOptional.isPresent()) {
                return planOptional;
            }
        }
        return Optional.empty();
    }

    /**
     * 基于指定丹方定义构建炼制计划。
     *
     * @param container 炼丹炉容器
     * @param definition 丹方定义
     * @param mainItem 主材槽锚点物品
     * @return 构建成功返回计划，否则返回空
     */
    private static Optional<TierRefinePlan> tryBuildTierRefinePlanByDefinition(
        Container container,
        AlchemyTierRecipeDefinition definition,
        Item mainItem
    ) {
        List<Item> remainingRequirements = new ArrayList<>(definition.mainIngredients());
        if (!removeSingleItem(remainingRequirements, mainItem)) {
            return Optional.empty();
        }
        definition.requiredPreviousPill().ifPresent(remainingRequirements::add);

        List<Integer> matchedAuxiliarySlots = new ArrayList<>();
        for (int slot : AUXILIARY_INPUT_SLOTS) {
            ItemStack auxiliaryStack = container.getItem(slot);
            if (auxiliaryStack.isEmpty()) {
                continue;
            }
            Item auxiliaryItem = auxiliaryStack.getItem();
            if (removeSingleItem(remainingRequirements, auxiliaryItem)) {
                matchedAuxiliarySlots.add(slot);
            }
        }

        if (!remainingRequirements.isEmpty()) {
            return Optional.empty();
        }

        List<Integer> matchedInputSlots = new ArrayList<>();
        matchedInputSlots.add(AlchemyFurnaceBlockEntity.SLOT_MAIN);
        matchedInputSlots.addAll(matchedAuxiliarySlots);
        return Optional.of(
            new TierRefinePlan(definition, List.copyOf(matchedInputSlots), matchedAuxiliarySlots.size())
        );
    }

    /**
     * 从列表中移除一个目标物品。
     *
     * @param items 目标列表
     * @param target 待移除物品
     * @return 移除成功返回 true，否则 false
     */
    private static boolean removeSingleItem(List<Item> items, Item target) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == target) {
                items.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * 执行有界批量炼制。
     * <p>
     * 规则说明：
     * 1) 请求批次大小最小按 1 处理，避免 0 或负数导致语义歧义；
     * 2) 最多执行请求次数，每次都复用 {@link #tryRefine(Container)} 的单次语义；
     * 3) 只要某次无法继续发起合法尝试（返回 false），立即提前停止；
     * 4) 返回值仅统计“成功产出丹药”的次数，不统计失败但已消耗材料的尝试。
     * </p>
     *
     * @param container 炼丹炉容器
     * @param requestedBatchSize 请求批次大小
     * @return 实际成功产出次数（>= 0）
     */
    public static int tryRefineBatch(Container container, int requestedBatchSize) {
        return tryRefineBatch(container, requestedBatchSize, null);
    }

    public static int tryRefineBatch(Container container, int requestedBatchSize, @Nullable ServerPlayer refiner) {
        int normalizedBatchSize = Math.max(1, requestedBatchSize);
        int successfulCount = 0;
        for (int i = 0; i < normalizedBatchSize; i++) {
            int outputCountBefore = container.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT).getCount();
            boolean attempted = tryRefine(container, refiner);
            if (!attempted) {
                break;
            }
            int outputCountAfter = container.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT).getCount();
            if (outputCountAfter > outputCountBefore) {
                successfulCount++;
            }
        }
        return successfulCount;
    }

    /**
     * 计算当前成功率百分比。
     *
     * @param container 炼丹炉容器
     * @return 成功率百分比（0-100）
     */
    public static int calculateSuccessRatePercent(Container container) {
        if (container == null) {
            return BASE_SUCCESS_RATE_PERCENT;
        }
        return calculateSuccessRatePercentByAuxiliaryCount(countAuxiliaryMaterials(container));
    }

    /**
     * 基于有效辅材数量计算成功率。
     *
     * @param auxiliaryCount 有效辅材数量
     * @return 成功率百分比
     */
    public static int calculateSuccessRatePercentByAuxiliaryCount(int auxiliaryCount) {
        int normalizedAuxiliaryCount = Math.max(0, Math.min(auxiliaryCount, MAX_AUXILIARY_SLOTS));
        int calculatedRate = BASE_SUCCESS_RATE_PERCENT + normalizedAuxiliaryCount * SUCCESS_RATE_BONUS_PER_AUXILIARY;
        return Math.max(MIN_SUCCESS_RATE_PERCENT, Math.min(calculatedRate, MAX_SUCCESS_RATE_PERCENT));
    }

    /**
     * 检查产出槽是否可放入目标产物。
     *
     * @param currentOutput 当前产出槽堆栈
     * @param recipeOutput 目标产出堆栈（固定 1 个）
     * @return 可放入返回 true
     */
    public static boolean canPlaceToOutputSlot(ItemStack currentOutput, ItemStack recipeOutput) {
        if (recipeOutput.isEmpty()) {
            return false;
        }
        if (currentOutput.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(currentOutput, recipeOutput)) {
            return false;
        }
        return currentOutput.getCount() < currentOutput.getMaxStackSize();
    }

    /**
     * 检查当前产出槽是否允许发起一次炼制尝试。
     * <p>
     * 该方法仅用于前置快速校验：
     * 1) 产出槽为空：允许；
     * 2) 产出槽为同类丹药且未满：允许；
     * 3) 否则不允许。
     * </p>
     * <p>
     * 注意：该阶段不会比较品质组件。品质组件比较在成功分支中、品质写入后通过
     * {@link #canPlaceToOutputSlot(ItemStack, ItemStack)} 进行最终校验。
     * </p>
     *
     * @param currentOutput 当前产出槽堆栈
     * @param recipeOutput 配方目标产出堆栈
     * @return 允许尝试返回 true
     */
    private static boolean canAttemptRefineWithOutputSlot(ItemStack currentOutput, ItemStack recipeOutput) {
        if (recipeOutput.isEmpty()) {
            return false;
        }
        if (currentOutput.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItem(currentOutput, recipeOutput)) {
            return false;
        }
        return currentOutput.getCount() < currentOutput.getMaxStackSize();
    }

    /**
     * 统计 4 个辅材槽中非空槽位数量。
     *
     * @param container 炼丹炉容器
     * @return 有效辅材槽位数
     */
    public static int countAuxiliaryMaterials(Container container) {
        int count = 0;
        for (int slot = AlchemyFurnaceBlockEntity.SLOT_AUX_1; slot <= AlchemyFurnaceBlockEntity.SLOT_AUX_4; slot++) {
            if (!container.getItem(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static List<AlchemyTierRecipeDefinition> createTieredRecipeDefinitions() {
        List<AlchemyTierRecipeDefinition> definitions = new ArrayList<>();

        definitions.add(
            new AlchemyTierRecipeDefinition(
                AlchemyRecipeTier.TIER1,
                List.of(Items.WHEAT, Items.IRON_INGOT),
                Optional.empty(),
                FarmingItems.CUI_SHENG_DAN.get(),
                PillQuality.HUANG,
                PillQuality.DI
            )
        );
        definitions.add(
            new AlchemyTierRecipeDefinition(
                AlchemyRecipeTier.TIER1,
                List.of(Items.SLIME_BALL, Items.IRON_INGOT),
                Optional.empty(),
                FarmingItems.RUN_ZE_DAN.get(),
                PillQuality.HUANG,
                PillQuality.DI
            )
        );

        definitions.add(
            new AlchemyTierRecipeDefinition(
                AlchemyRecipeTier.TIER2,
                List.of(Items.GOLD_INGOT),
                Optional.of(FarmingItems.CUI_SHENG_DAN.get()),
                FarmingItems.HUI_CHUN_DAN.get(),
                PillQuality.DI,
                PillQuality.XUAN
            )
        );
        definitions.add(
            new AlchemyTierRecipeDefinition(
                AlchemyRecipeTier.TIER2,
                List.of(Items.ENDER_PEARL),
                Optional.of(FarmingItems.RUN_ZE_DAN.get()),
                FarmingItems.HU_TI_DAN.get(),
                PillQuality.DI,
                PillQuality.XUAN
            )
        );

        definitions.add(
            new AlchemyTierRecipeDefinition(
                AlchemyRecipeTier.TIER3,
                List.of(Items.NETHER_STAR),
                Optional.of(FarmingItems.HUI_CHUN_DAN.get()),
                FarmingItems.HUI_CHUN_DAN.get(),
                PillQuality.XUAN,
                PillQuality.TIAN
            )
        );
        definitions.add(
            new AlchemyTierRecipeDefinition(
                AlchemyRecipeTier.TIER3,
                List.of(XianqiaoItems.HEAVENLY_FRAGMENT.get()),
                Optional.of(FarmingItems.HU_TI_DAN.get()),
                FarmingItems.HU_TI_DAN.get(),
                PillQuality.XUAN,
                PillQuality.TIAN
            )
        );

        return List.copyOf(definitions);
    }

    private static Map<AlchemyRecipeTier, List<AlchemyTierRecipeDefinition>> createTieredRecipesByTier(
        List<AlchemyTierRecipeDefinition> definitions
    ) {
        EnumMap<AlchemyRecipeTier, List<AlchemyTierRecipeDefinition>> grouped = new EnumMap<>(AlchemyRecipeTier.class);
        for (AlchemyRecipeTier tier : AlchemyRecipeTier.values()) {
            grouped.put(tier, new ArrayList<>());
        }
        for (AlchemyTierRecipeDefinition definition : definitions) {
            grouped.get(definition.tier()).add(definition);
        }

        EnumMap<AlchemyRecipeTier, List<AlchemyTierRecipeDefinition>> immutableGrouped =
            new EnumMap<>(AlchemyRecipeTier.class);
        for (AlchemyRecipeTier tier : AlchemyRecipeTier.values()) {
            immutableGrouped.put(tier, List.copyOf(grouped.get(tier)));
        }
        return Map.copyOf(immutableGrouped);
    }

    private static Item mapOutputByMainProperty(MaterialProperty property) {
        return switch (property) {
            case WOOD -> FarmingItems.CUI_SHENG_DAN.get();
            case SOFT -> FarmingItems.HU_TI_DAN.get();
            case VITAL -> FarmingItems.HUI_CHUN_DAN.get();
            case MOIST -> FarmingItems.RUN_ZE_DAN.get();
        };
    }

    private static boolean rollRefineSuccess(int auxiliaryCount, @Nullable ServerPlayer refiner) {
        if (consumeForcedRefineFailureForTest(refiner)) {
            return false;
        }
        int successRate = calculateSuccessRatePercentByAuxiliaryCount(auxiliaryCount);
        int randomPercent = ThreadLocalRandom.current().nextInt(MAX_SUCCESS_RATE_PERCENT);
        return randomPercent < successRate;
    }

    /**
     * 测试专用：强制下一次炼制命中“失败分支”。
     * <p>
     * 仅用于 GameTest 构造确定性夹具，避免通过随机重试等待失败分支；
     * 正常玩法路径不会主动写入该标记，因此线上语义保持不变。
     * </p>
     *
     * @param refiner 发起炼制的玩家
     */
    public static void forceNextRefineFailureForTest(ServerPlayer refiner) {
        if (refiner == null) {
            return;
        }
        TEST_FORCE_NEXT_REFINE_FAILURE.add(refiner.getUUID());
    }

    private static boolean consumeForcedRefineFailureForTest(@Nullable ServerPlayer refiner) {
        if (refiner == null) {
            return false;
        }
        return TEST_FORCE_NEXT_REFINE_FAILURE.remove(refiner.getUUID());
    }

    /**
     * 基于辅材数量执行一次品质随机判定。
     * <p>
     * 基础概率：黄 60 / 地 30 / 玄 8 / 天 2。
     * 每个辅材都会提升地/玄/天概率，并等量压缩黄品概率。
     * </p>
     *
     * @param auxiliaryCount 有效辅材数量
     * @return 本次判定得到的品质
     */
    private static PillQuality rollPillQuality(int auxiliaryCount) {
        int normalizedAuxiliaryCount = Math.max(0, Math.min(auxiliaryCount, MAX_AUXILIARY_SLOTS));

        int huangPercent = BASE_HUANG_PERCENT - normalizedAuxiliaryCount * HUANG_REDUCTION_PER_AUXILIARY;
        int diPercent = BASE_DI_PERCENT + normalizedAuxiliaryCount * DI_BONUS_PER_AUXILIARY;
        int xuanPercent = BASE_XUAN_PERCENT + normalizedAuxiliaryCount * XUAN_BONUS_PER_AUXILIARY;
        int tianPercent = BASE_TIAN_PERCENT + normalizedAuxiliaryCount * TIAN_BONUS_PER_AUXILIARY;

        int totalPercent = huangPercent + diPercent + xuanPercent + tianPercent;
        int correction = MAX_SUCCESS_RATE_PERCENT - totalPercent;
        huangPercent += correction;

        int randomPercent = ThreadLocalRandom.current().nextInt(MAX_SUCCESS_RATE_PERCENT);
        int diThreshold = huangPercent + diPercent;
        int xuanThreshold = diThreshold + xuanPercent;
        if (randomPercent < huangPercent) {
            return PillQuality.HUANG;
        }
        if (randomPercent < diThreshold) {
            return PillQuality.DI;
        }
        if (randomPercent < xuanThreshold) {
            return PillQuality.XUAN;
        }
        return PillQuality.TIAN;
    }

    /**
     * 将随机得到的品质钳制到丹方允许区间内。
     *
     * @param candidate 候选品质
     * @param minimumQuality 最小允许品质
     * @param maximumQuality 最大允许品质
     * @return 区间内的最终品质
     */
    private static PillQuality clampQualityToRange(
        PillQuality candidate,
        PillQuality minimumQuality,
        PillQuality maximumQuality
    ) {
        int boundedOrdinal = Math.max(
            minimumQuality.ordinal(),
            Math.min(candidate.ordinal(), maximumQuality.ordinal())
        );
        return PILL_QUALITIES[boundedOrdinal];
    }

    /**
     * 兼容旧逻辑：消耗主材与全部非空辅材。
     *
     * @param container 炼丹炉容器
     */
    private static void consumeLegacyInputMaterials(Container container) {
        container.removeItem(AlchemyFurnaceBlockEntity.SLOT_MAIN, 1);
        for (int slot = AlchemyFurnaceBlockEntity.SLOT_AUX_1; slot <= AlchemyFurnaceBlockEntity.SLOT_AUX_4; slot++) {
            ItemStack auxiliaryStack = container.getItem(slot);
            if (!auxiliaryStack.isEmpty()) {
                container.removeItem(slot, 1);
            }
        }
    }

    /**
     * 按命中的具体槽位精确消耗输入。
     * <p>
     * 使用去重集合防止同槽位被重复扣减。
     * </p>
     *
     * @param container 炼丹炉容器
     * @param matchedInputSlots 命中的输入槽位集合
     */
    private static void consumeInputMaterialsBySlots(Container container, List<Integer> matchedInputSlots) {
        Set<Integer> uniqueSlots = new LinkedHashSet<>(matchedInputSlots);
        for (int slot : uniqueSlots) {
            container.removeItem(slot, 1);
        }
    }

    /**
     * 在“炼制判定失败”的合法尝试分支尝试产出九转髓晶。
     * <p>
     * 该逻辑是 Task23/M-D05 的最小落地合同，遵循 fail-closed 原则：
     * 1) 仅在既有炼丹主流程已判定为“本次尝试合法 + 炼制失败 + 已执行消耗”的分支调用；
     * 2) 仅当当前产出槽可放入九转髓晶时才实际放入；
     * 3) 若产出槽不兼容或已满，则本次不产出任何额外物品，不创建旁路掉落。
     * </p>
     *
     * @param container 炼丹炉容器
     */
    private static void placeNineRefinementMarrowCrystalOnRefineFailure(
        Container container,
        @Nullable ServerPlayer refiner
    ) {
        if (refiner == null) {
            return;
        }
        ItemStack failureOutput = new ItemStack(XianqiaoItems.JIU_ZHUAN_SUI_JING.get(), OUTPUT_COUNT_PER_SUCCESS);
        ItemStack currentOutput = container.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT);
        if (!canPlaceToOutputSlot(currentOutput, failureOutput)) {
            return;
        }
        if (!consumeRefinerHunPo(refiner, NINE_REFINEMENT_MARROW_HUNPO_COST)) {
            return;
        }
        placeOutput(container, failureOutput);
    }

    private static boolean consumeRefinerHunPo(ServerPlayer refiner, double amount) {
        double safeAmount = Math.max(0.0D, amount);
        if (safeAmount <= 0.0D) {
            return true;
        }
        double currentAmount = readRefinerHunPo(refiner);
        if (currentAmount < safeAmount) {
            return false;
        }
        if (TEST_REFINER_HUNPO_OVERRIDE.containsKey(refiner.getUUID())) {
            TEST_REFINER_HUNPO_OVERRIDE.put(refiner.getUUID(), Math.max(0.0D, currentAmount - safeAmount));
        }
        try {
            HunPoHelper.modify(refiner, -safeAmount);
        } catch (Throwable ignored) {
        }
        return true;
    }

    private static double readRefinerHunPo(ServerPlayer refiner) {
        if (TEST_REFINER_HUNPO_OVERRIDE.containsKey(refiner.getUUID())) {
            return TEST_REFINER_HUNPO_OVERRIDE.get(refiner.getUUID());
        }
        return HunPoHelper.getAmount(refiner);
    }

    public static void seedHunPoAmountForTest(ServerPlayer refiner, double amount) {
        double safeAmount = Math.max(0.0D, amount);
        TEST_REFINER_HUNPO_OVERRIDE.put(refiner.getUUID(), safeAmount);
        try {
            double currentAmount = HunPoHelper.getAmount(refiner);
            HunPoHelper.modify(refiner, safeAmount - currentAmount);
        } catch (Throwable ignored) {
        }
    }

    public static double readHunPoAmountForTest(ServerPlayer refiner) {
        return readRefinerHunPo(refiner);
    }

    private static void placeOutput(Container container, ItemStack recipeOutput) {
        ItemStack outputStack = container.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT);
        if (outputStack.isEmpty()) {
            container.setItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT, recipeOutput.copy());
            return;
        }
        outputStack.grow(recipeOutput.getCount());
        container.setItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT, outputStack);
    }

    /**
     * 命中三级丹方后的一次炼制计划。
     *
     * @param definition 命中的丹方定义
     * @param matchedInputSlots 本次命中的输入槽位（含主材槽）
     * @param auxiliaryCountForSuccessRate 本次用于成功率/品质计算的辅材计数
     */
    private record TierRefinePlan(
        AlchemyTierRecipeDefinition definition,
        List<Integer> matchedInputSlots,
        int auxiliaryCountForSuccessRate
    ) {

        private ItemStack createOutputStack() {
            return new ItemStack(definition.outputPill(), OUTPUT_COUNT_PER_SUCCESS);
        }
    }

    public enum AlchemyRecipeTier {
        TIER1,
        TIER2,
        TIER3
    }

    public record AlchemyTierRecipeDefinition(
        AlchemyRecipeTier tier,
        List<Item> mainIngredients,
        Optional<Item> requiredPreviousPill,
        Item outputPill,
        PillQuality minimumOutputQuality,
        PillQuality maximumOutputQuality
    ) {

        public AlchemyTierRecipeDefinition {
            if (tier == null) {
                throw new IllegalArgumentException("tier cannot be null");
            }
            if (mainIngredients == null || mainIngredients.isEmpty()) {
                throw new IllegalArgumentException("mainIngredients cannot be null or empty");
            }
            for (Item ingredient : mainIngredients) {
                if (ingredient == null) {
                    throw new IllegalArgumentException("mainIngredients cannot contain null");
                }
            }
            if (requiredPreviousPill == null) {
                throw new IllegalArgumentException("requiredPreviousPill cannot be null");
            }
            if (requiredPreviousPill.isPresent() && requiredPreviousPill.get() == null) {
                throw new IllegalArgumentException("requiredPreviousPill cannot contain null");
            }
            if (outputPill == null) {
                throw new IllegalArgumentException("outputPill cannot be null");
            }
            if (minimumOutputQuality == null || maximumOutputQuality == null) {
                throw new IllegalArgumentException("quality range cannot be null");
            }
            if (minimumOutputQuality.ordinal() > maximumOutputQuality.ordinal()) {
                throw new IllegalArgumentException("invalid quality range: minimum > maximum");
            }

            mainIngredients = List.copyOf(mainIngredients);
        }
    }
}
