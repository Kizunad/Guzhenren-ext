package com.Kizunad.guzhenrenext.kongqiao.logic;

import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 蛊虫效果逻辑接口。
 * <p>
 * 所有的蛊虫功能（主动/被动）都应实现此接口，并通过 {@link GuEffectRegistry} 注册。
 * </p>
 */
public interface IGuEffect {

    /**
     * 获取此效果对应的 UsageID。
     * 必须与 NianTouData JSON 中的 usageID 一致。
     */
    String getUsageId();

    /**
     * 被动效果：每 Tick 调用。
     * <p>
     * 当物品存放在空窍中时，系统会每 Tick 调用此方法。
     * 可以在此处实现属性加成、被动回血、光环等逻辑。
     * </p>
     *
     * @param user      持有空窍的实体
     * @param stack     蛊虫物品堆
     * @param usageInfo 该用途的配置数据（包含消耗、参数等）
     */
    default void onTick(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
    }

    /**
     * 主动效果：当玩家激活时调用。
     * <p>
     * 通常由 UI 按钮点击或快捷键触发。
     * </p>
     *
     * @param user      持有空窍的实体
     * @param stack     蛊虫物品堆
     * @param usageInfo 该用途的配置数据
     * @return true 表示执行成功（可能会扣除消耗），false 表示执行失败
     */
    default boolean onActivate(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        return false;
    }

    /**
     * 当物品刚刚进入空窍（被装备）时调用一次。
     * 用于初始化一些状态。
     */
    default void onEquip(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
    }

    /**
     * 当物品离开空窍（被取出）时调用一次。
     * 用于清除被动带来的属性修饰符等。
     */
    default void onUnequip(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
    }

    /**
     * 攻击触发：当持有者攻击目标时调用。
     * <p>
     * 可用于增加伤害、附加效果（中毒、燃烧）等。
     * </p>
     *
     * @param attacker  攻击者（持有空窍的实体）
     * @param target    受害者
     * @param damage    原始伤害值
     * @param stack     蛊虫物品堆
     * @param usageInfo 该用途的配置数据
     * @return 修改后的伤害值。如果不修改，请返回原 damage。
     */
    default float onAttack(LivingEntity attacker, LivingEntity target, float damage, ItemStack stack, NianTouData.Usage usageInfo) {
        return damage;
    }

    /**
     * 受伤触发：当持有者受到伤害时调用。
     * <p>
     * 可用于减免伤害、反弹伤害、触发护盾等。
     * </p>
     *
     * @param victim    受害者（持有空窍的实体）
     * @param source    伤害来源
     * @param damage    原始伤害值
     * @param stack     蛊虫物品堆
     * @param usageInfo 该用途的配置数据
     * @return 修改后的伤害值。如果不修改，请返回原 damage。
     */
    default float onHurt(LivingEntity victim, DamageSource source, float damage, ItemStack stack, NianTouData.Usage usageInfo) {
        return damage;
    }
}
