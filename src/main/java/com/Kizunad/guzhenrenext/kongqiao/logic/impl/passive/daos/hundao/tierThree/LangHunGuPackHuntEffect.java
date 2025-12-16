package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 狼魂蛊：被动·群猎。
 * <p>
 * 周围每存在一个归属玩家（Owner 为玩家）的驯服实体，为玩家提供额外攻击力。
 * </p>
 */
public class LangHunGuPackHuntEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:langhungu_passive_pack_hunt";

    private static final double DEFAULT_RADIUS = 12.0;
    private static final double DEFAULT_ATTACK_BONUS_PER_ENTITY = 10.0;
    private static final ResourceLocation ATTACK_MODIFIER_ID =
        ResourceLocation.parse("guzhenren:langhungu_pack_hunt_attack");

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onSecond(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        double radius = getMetaDouble(usageInfo, "radius", DEFAULT_RADIUS);
        int ownedCount = countOwnedEntities(user, radius);
        if (ownedCount <= 0) {
            removeAttackModifier(user);
            return;
        }

        double perEntity = getMetaDouble(
            usageInfo,
            "attack_bonus_per_entity",
            DEFAULT_ATTACK_BONUS_PER_ENTITY
        );
        double multiplier = DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.HUN_DAO);
        double bonus = ownedCount * perEntity * multiplier;
        applyAttackModifier(user, bonus);
    }

    @Override
    public void onUnequip(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        removeAttackModifier(user);
    }

    private int countOwnedEntities(LivingEntity user, double radius) {
        UUID ownerUuid = user.getUUID();
        AABB area = user.getBoundingBox().inflate(radius, radius, radius);

        int count = 0;
        for (LivingEntity entity : user.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (!(entity instanceof OwnableEntity ownable)) {
                continue;
            }
            UUID entityOwner = ownable.getOwnerUUID();
            if (entityOwner != null && entityOwner.equals(ownerUuid)) {
                count++;
            }
        }
        return count;
    }

    private void applyAttackModifier(LivingEntity user, double amount) {
        AttributeInstance attr = user.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr == null) {
            return;
        }
        AttributeModifier modifier = attr.getModifier(ATTACK_MODIFIER_ID);
        if (modifier == null || Double.compare(modifier.amount(), amount) != 0) {
            if (modifier != null) {
                attr.removeModifier(ATTACK_MODIFIER_ID);
            }
            attr.addTransientModifier(
                new AttributeModifier(
                    ATTACK_MODIFIER_ID,
                    amount,
                    AttributeModifier.Operation.ADD_VALUE
                )
            );
        }
    }

    private void removeAttackModifier(LivingEntity user) {
        AttributeInstance attr = user.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null && attr.getModifier(ATTACK_MODIFIER_ID) != null) {
            attr.removeModifier(ATTACK_MODIFIER_ID);
        }
    }

    private static double getMetaDouble(NianTouData.Usage usage, String key, double defaultValue) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}

