package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 刀道高转被动：持续维持后提升 Guzhenren 字段上限，并提供少量资源恢复/减伤。
 * <p>
 * 仅用于三~五转的“上限字段”需求，数值量级控制在几百为主。
 * </p>
 */
public final class DaoDaoSustainedCapRegenGuardEffect
    extends AbstractDaoDaoSustainedEffect {

    public static final String META_REGEN_ZHENYUAN = "regen_zhenyuan";
    public static final String META_DAMAGE_REDUCTION_RATIO =
        "damage_reduction_ratio";

    private static final double MAX_REDUCTION_RATIO = 0.55;

    private final List<CapSpec> caps;

    public DaoDaoSustainedCapRegenGuardEffect(
        final String usageId,
        final List<CapSpec> caps
    ) {
        super(usageId, DaoHenHelper.DaoType.DAO_DAO);
        this.caps = caps == null ? List.of() : List.copyOf(caps);
    }

    @Override
    protected void onSustainSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo,
        final double daoHenMultiplier
    ) {
        final double multiplier = DaoHenEffectScalingHelper.clampMultiplier(
            daoHenMultiplier
        );

        for (CapSpec cap : caps) {
            final double base = Math.max(
                0.0,
                UsageMetadataHelper.getDouble(
                    usageInfo,
                    cap.amountMetaKey,
                    0.0
                )
            );
            final double scaled = base * multiplier;
            GuzhenrenVariableModifierService.setAdditiveModifier(
                user,
                cap.variableKey,
                getUsageId(),
                scaled
            );
        }

        final double baseZhenyuanRegen = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_REGEN_ZHENYUAN, 0.0)
        );
        if (baseZhenyuanRegen > 0.0) {
            ZhenYuanHelper.modify(user, baseZhenyuanRegen * multiplier);
        }
    }

    @Override
    protected void onInactive(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        for (CapSpec cap : caps) {
            GuzhenrenVariableModifierService.removeModifier(
                user,
                cap.variableKey,
                getUsageId()
            );
        }
    }

    @Override
    public float onHurt(
        final LivingEntity victim,
        final DamageSource source,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (victim == null || usageInfo == null) {
            return damage;
        }
        if (!isActive(victim)) {
            return damage;
        }

        final double baseRatio = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_DAMAGE_REDUCTION_RATIO,
                0.0
            )
        );
        if (baseRatio <= 0.0) {
            return damage;
        }

        final double multiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(
                victim,
                DaoHenHelper.DaoType.DAO_DAO
            )
        );
        final double scaledRatio = Math.min(
            MAX_REDUCTION_RATIO,
            baseRatio * multiplier
        );
        return (float) (damage * (1.0 - Math.max(0.0, scaledRatio)));
    }

    /**
     * 字段上限加成描述。
     *
     * @param variableKey   {@link GuzhenrenVariableModifierService} 中的字段键
     * @param amountMetaKey 从 metadata 读取基础加成的 key（字符串数值）
     */
    public record CapSpec(String variableKey, String amountMetaKey) {
        public CapSpec {
            Objects.requireNonNull(variableKey, "variableKey");
            Objects.requireNonNull(amountMetaKey, "amountMetaKey");
        }
    }
}
