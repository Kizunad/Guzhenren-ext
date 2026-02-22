package com.Kizunad.guzhenrenext.xianqiao.spirit;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

/**
 * 仙窍中的仙牛实体。
 *
 * <p>该实体仅用于提供可生成、可渲染、可交互的基础牛类行为，
 * 继承原版 {@link Cow} 并复用其 AI、属性与掉落等默认逻辑，
 * 避免在本任务中引入额外业务语义，确保实现最小侵入。
 */
public class XianCowEntity extends Cow {

    /**
     * 构造仙牛实体。
     *
     * @param entityType 实体类型
     * @param level 当前世界
     */
    public XianCowEntity(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
    }
}
