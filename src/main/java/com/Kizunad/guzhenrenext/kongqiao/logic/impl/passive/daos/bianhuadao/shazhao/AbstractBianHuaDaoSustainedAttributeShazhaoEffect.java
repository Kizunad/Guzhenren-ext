package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 变化道“持续型属性修饰”杀招通用实现（每秒扣费 + 道痕缩放）。
 * <p>
 * - 消耗字段：zhenyuan_base_cost_per_second / niantou_cost_per_second / jingli_cost_per_second / hunpo_cost_per_second<br>
 * - 道痕倍率：{@link DaoHenCalculator#calculateSelfMultiplier(LivingEntity, DaoHenHelper.DaoType)}（并用
 * {@link DaoHenEffectScalingHelper} 做非伤害类裁剪）<br>
 * - 属性修饰：使用 transient modifier，失效时撤销<br>
 * </p>
 */
public abstract class AbstractBianHuaDaoSustainedAttributeShazhaoEffect
    implements IShazhaoEffect {

    protected record AttributeSpec(
        Holder<Attribute> attribute,
        AttributeModifier.Operation operation,
        String amountKey,
        double defaultAmount,
        ResourceLocation modifierId
    ) {}

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.BIAN_HUA_DAO;

    private final List<AttributeSpec> specs;

    protected AbstractBianHuaDaoSustainedAttributeShazhaoEffect(
        final List<AttributeSpec> specs
    ) {
        this.specs = specs == null ? List.of() : List.copyOf(specs);
    }

    @Override
    public void onSecond(final LivingEntity user, final ShazhaoData data) {
        if (user == null || data == null || user.level().isClientSide()) {
            return;
        }

        if (!ShazhaoCostHelper.tryConsumeSustain(user, data)) {
            removeAllModifiers(user);
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DAO_TYPE
        );
        applyAllModifiers(user, data, selfMultiplier);
    }

    @Override
    public void onInactive(final LivingEntity user) {
        if (user == null || user.level().isClientSide()) {
            return;
        }
        removeAllModifiers(user);
    }

    private void applyAllModifiers(
        final LivingEntity user,
        final ShazhaoData data,
        final double selfMultiplier
    ) {
        for (AttributeSpec spec : specs) {
            if (spec == null || spec.attribute == null || spec.modifierId == null) {
                continue;
            }
            final AttributeInstance attr = user.getAttribute(spec.attribute);
            if (attr == null) {
                continue;
            }

            final double baseAmount = Math.max(
                0.0,
                ShazhaoMetadataHelper.getDouble(
                    data,
                    spec.amountKey,
                    spec.defaultAmount
                )
            );
            final double amount = DaoHenEffectScalingHelper.scaleValue(
                baseAmount,
                selfMultiplier
            );

            final AttributeModifier existing = attr.getModifier(spec.modifierId);
            if (existing != null && Double.compare(existing.amount(), amount) == 0) {
                continue;
            }
            if (existing != null) {
                attr.removeModifier(spec.modifierId);
            }
            if (amount > 0.0) {
                attr.addTransientModifier(
                    new AttributeModifier(spec.modifierId, amount, spec.operation)
                );
            }
        }
    }

    private void removeAllModifiers(final LivingEntity user) {
        for (AttributeSpec spec : specs) {
            if (spec == null || spec.attribute == null || spec.modifierId == null) {
                continue;
            }
            final AttributeInstance attr = user.getAttribute(spec.attribute);
            if (attr != null && attr.getModifier(spec.modifierId) != null) {
                attr.removeModifier(spec.modifierId);
            }
        }
    }
}

