package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 火道通用被动：持续性维持（多资源）+ 提升 Guzhenren 字段上限（仅用于高转）。
 * <p>
 * 通过 metadata 配置：
 * <ul>
 *   <li>zhenyuan_base_cost_per_second / niantou_cost_per_second / jingli_cost_per_second / hunpo_cost_per_second</li>
 *   <li>字段加成：由构造器注入各字段及其基础加成 meta key</li>
 *   <li>可选：hunpo_resistance_restore（每秒提升魂魄抗性值，受上限约束）</li>
 * </ul>
 * </p>
 */
public class HuoDaoSustainedVariableCapEffect implements IGuEffect {
    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_NIANTOU_COST_PER_SECOND =
        "niantou_cost_per_second";
    private static final String META_JINGLI_COST_PER_SECOND =
        "jingli_cost_per_second";
    private static final String META_HUNPO_COST_PER_SECOND =
        "hunpo_cost_per_second";

    private static final String META_HUNPO_RESISTANCE_RESTORE =
        "hunpo_resistance_restore";

    private static final double DEFAULT_COST = 0.0;
    private static final double DEFAULT_AMOUNT = 0.0;

    private final String usageId;
    private final List<CapSpec> caps;

    public HuoDaoSustainedVariableCapEffect(
        final String usageId,
        final List<CapSpec> caps
    ) {
        this.usageId = usageId;
        this.caps = caps == null ? List.of() : List.copyOf(caps);
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public void onSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            setActive(user, false);
            clearAll(user);
            return;
        }

        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double niantouCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_NIANTOU_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double jingliCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_JINGLI_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double hunpoCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HUNPO_COST_PER_SECOND,
                DEFAULT_COST
            )
        );

        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            setActive(user, false);
            clearAll(user);
            return;
        }

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUO_DAO
        );
        applyCaps(user, usageInfo, multiplier);
        applyHunpoResistance(user, usageInfo, multiplier);

        setActive(user, true);
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }
        setActive(user, false);
        clearAll(user);
    }

    private void applyCaps(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        for (CapSpec cap : caps) {
            final double base = Math.max(
                0.0,
                UsageMetadataHelper.getDouble(
                    usageInfo,
                    cap.amountMetaKey(),
                    DEFAULT_AMOUNT
                )
            );
            final double amount = base * Math.max(0.0, multiplier);
            GuzhenrenVariableModifierService.setAdditiveModifier(
                user,
                cap.variableKey(),
                usageId,
                amount
            );
        }
    }

    private void applyHunpoResistance(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        final double baseRestore = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HUNPO_RESISTANCE_RESTORE,
                DEFAULT_AMOUNT
            )
        );
        if (Double.compare(baseRestore, 0.0) == 0) {
            return;
        }
        final double restore = baseRestore * Math.max(0.0, multiplier);
        HunPoHelper.modifyResistance(user, restore);
    }

    private void clearAll(final LivingEntity user) {
        for (CapSpec cap : caps) {
            GuzhenrenVariableModifierService.removeModifier(
                user,
                cap.variableKey(),
                usageId
            );
        }
    }

    private void setActive(final LivingEntity user, final boolean active) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives == null) {
            return;
        }
        if (active) {
            actives.add(usageId);
            return;
        }
        actives.remove(usageId);
    }

    /**
     * 字段上限加成描述。
     *
     * @param variableKey   {@link GuzhenrenVariableModifierService} 中的字段键
     * @param amountMetaKey 从 metadata 读取基础加成的 key（字符串数值）
     */
    public record CapSpec(String variableKey, String amountMetaKey) {}
}

