package com.Kizunad.customNPCs.ai.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;

/**
 * 实体验证工具 - 判定敌友关系。
 */
public final class EntityRelationUtil {

    private EntityRelationUtil() {}

    /**
     * 判断目标是否为友方。
     * 规则：同队伍、相同实体类型且非怪物，或目标就是自身。
     */
    public static boolean isAlly(LivingEntity observer, LivingEntity target) {
        if (observer == null || target == null) {
            return false;
        }
        if (observer == target) {
            return true;
        }

        var observerTeam = observer.getTeam();
        var targetTeam = target.getTeam();
        if (observerTeam != null && targetTeam != null) {
            if (observerTeam.isAlliedTo(targetTeam)) {
                return true;
            }
        }

        return (
            observer.getType() == target.getType() &&
            !(target instanceof Monster)
        );
    }

    /**
     * 判断目标是否为敌对。
     * 规则：怪物、曾攻击过观察者、或当前正在被观察者攻击。
     */
    public static boolean isHostileTo(
        LivingEntity observer,
        LivingEntity target
    ) {
        if (observer == null || target == null) {
            return false;
        }
        if (isAlly(observer, target)) {
            return false;
        }
        if (target instanceof Monster) {
            return true;
        }
        if (target.getLastHurtByMob() == observer) {
            return true;
        }
        return observer.getLastHurtByMob() == target;
    }
}
