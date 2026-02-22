package com.Kizunad.guzhenrenext.xianqiao.spirit;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.level.Level;

/**
 * 仙窍中的仙鸡实体。
 *
 * <p>该实体复用原版 {@link Chicken} 行为，仅提供仙窍系统内的可注册与可生成能力，
 * 不额外叠加业务逻辑，确保与当前任务目标保持一致。
 */
public class XianChickenEntity extends Chicken {

    /**
     * 构造仙鸡实体。
     *
     * @param entityType 实体类型
     * @param level 当前世界
     */
    public XianChickenEntity(EntityType<? extends Chicken> entityType, Level level) {
        super(entityType, level);
    }
}
