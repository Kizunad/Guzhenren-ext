package com.Kizunad.guzhenrenext.bastion.entity;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地守卫工厂 - 根据道途类型创建基地守卫实体。
 * <p>
 * 此工厂支持两种模式：
 * <ul>
 *   <li>如果 CustomNPCs 模块可用，使用自定义 NPC</li>
 *   <li>否则回退到原版怪物</li>
 * </ul>
 * 通过反射实现模块解耦，避免编译时依赖。
 * </p>
 */
public final class BastionGuardianFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionGuardianFactory.class);

    private BastionGuardianFactory() {
        // 工具类
    }

    // ===== NPC 系统检测 =====

    /** CustomNPC 实体类型 Holder 是否可用。 */
    private static final boolean NPC_SYSTEM_AVAILABLE;
    /** NPC 创建方法句柄（反射获取）。 */
    private static final MethodHandle NPC_CREATE_METHOD;
    /** NPC 属性设置方法句柄。 */
    private static final MethodHandle NPC_SET_STRENGTH;
    private static final MethodHandle NPC_SET_HEALTH;
    private static final MethodHandle NPC_SET_SPEED;
    private static final MethodHandle NPC_SET_DEFENSE;
    private static final MethodHandle NPC_SET_SENSOR;
    private static final MethodHandle NPC_REFRESH_ATTRIBUTES;
    private static final MethodHandle NPC_SET_SKIN;

    static {
        boolean available = false;
        MethodHandle createMethod = null;
        MethodHandle setStrength = null;
        MethodHandle setHealth = null;
        MethodHandle setSpeed = null;
        MethodHandle setDefense = null;
        MethodHandle setSensor = null;
        MethodHandle refreshAttrs = null;
        MethodHandle setSkin = null;

        try {
            // 尝试加载 CustomNPC 类
            Class<?> modEntitiesClass = Class.forName(
                "com.Kizunad.customNPCs.entity.ModEntities"
            );
            Class<?> npcEntityClass = Class.forName(
                "com.Kizunad.customNPCs.entity.CustomNpcEntity"
            );

            // 获取 CUSTOM_NPC 静态字段
            var customNpcField = modEntitiesClass.getField("CUSTOM_NPC");
            Object holder = customNpcField.get(null);

            if (holder != null) {
                // 获取 DeferredHolder.get() 方法
                var getMethod = holder.getClass().getMethod("get");
                Object entityType = getMethod.invoke(holder);

                if (entityType != null) {
                    // 获取 EntityType.create(Level) 方法
                    var lookup = MethodHandles.lookup();
                    createMethod = lookup.findVirtual(
                        EntityType.class,
                        "create",
                        MethodType.methodType(Entity.class, net.minecraft.world.level.Level.class)
                    );
                    // 绑定到具体的 EntityType 实例
                    createMethod = createMethod.bindTo(entityType);

                    // 获取属性设置方法
                    setStrength = lookup.findVirtual(
                        npcEntityClass, "setStrengthBonus",
                        MethodType.methodType(void.class, float.class)
                    );
                    setHealth = lookup.findVirtual(
                        npcEntityClass, "setHealthBonus",
                        MethodType.methodType(void.class, float.class)
                    );
                    setSpeed = lookup.findVirtual(
                        npcEntityClass, "setSpeedBonus",
                        MethodType.methodType(void.class, float.class)
                    );
                    setDefense = lookup.findVirtual(
                        npcEntityClass, "setDefenseBonus",
                        MethodType.methodType(void.class, float.class)
                    );
                    setSensor = lookup.findVirtual(
                        npcEntityClass, "setSensorBonus",
                        MethodType.methodType(void.class, float.class)
                    );
                    refreshAttrs = lookup.findVirtual(
                        npcEntityClass, "refreshGrowthAttributes",
                        MethodType.methodType(void.class)
                    );
                    setSkin = lookup.findVirtual(
                        npcEntityClass, "setSkinTexture",
                        MethodType.methodType(void.class, String.class)
                    );

                    available = true;
                    LOGGER.info("CustomNPC 系统已加载，基地守卫将使用自定义 NPC");
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.info("CustomNPC 模块未加载，基地守卫将使用原版怪物");
        } catch (Exception e) {
            LOGGER.warn("CustomNPC 系统初始化失败，回退到原版怪物: {}", e.getMessage());
        }

        NPC_SYSTEM_AVAILABLE = available;
        NPC_CREATE_METHOD = createMethod;
        NPC_SET_STRENGTH = setStrength;
        NPC_SET_HEALTH = setHealth;
        NPC_SET_SPEED = setSpeed;
        NPC_SET_DEFENSE = setDefense;
        NPC_SET_SENSOR = setSensor;
        NPC_REFRESH_ATTRIBUTES = refreshAttrs;
        NPC_SET_SKIN = setSkin;
    }

    // ===== 属性配置常量 =====

    /**
     * 守卫属性配置。
     */
    private static final class GuardianStats {
        // 基础属性（初始值）
        static final float BASE_STRENGTH = 5.0f;
        static final float BASE_HEALTH = 20.0f;
        static final float BASE_SPEED = 1.0f;
        static final float BASE_DEFENSE = 2.0f;
        static final float BASE_SENSOR = 10.0f;

        // 转数缩放因子（每转增加的比例）
        static final float TIER_STRENGTH_SCALE = 3.0f;
        static final float TIER_HEALTH_SCALE = 15.0f;
        static final float TIER_SPEED_SCALE = 0.3f;
        static final float TIER_DEFENSE_SCALE = 1.5f;
        static final float TIER_SENSOR_SCALE = 5.0f;

        // 道途特化倍率
        static final float ZHI_DAO_SENSOR_MULT = 1.5f;
        static final float HUN_DAO_SPEED_MULT = 1.3f;
        static final float MU_DAO_HEALTH_MULT = 1.4f;
        static final float LI_DAO_STRENGTH_MULT = 1.5f;
        static final float LI_DAO_DEFENSE_MULT = 1.3f;

        // 生成参数
        static final double BLOCK_CENTER_OFFSET = 0.5;
        static final float FULL_ROTATION_DEGREES = 360.0f;

        private GuardianStats() {
        }
    }

    // ===== 公开 API =====

    /**
     * 检查 NPC 系统是否可用。
     *
     * @return true 如果 CustomNPC 模块已加载
     */
    public static boolean isNpcSystemAvailable() {
        return NPC_SYSTEM_AVAILABLE;
    }

    /**
     * 为指定基地创建一个守卫实体。
     * <p>
     * 如果 CustomNPC 系统可用，创建 NPC；否则创建原版怪物。
     * </p>
     *
     * @param level    服务端世界
     * @param bastion  基地数据
     * @param spawnPos 生成位置
     * @return 已配置的守卫实体，如果创建失败则返回 null
     */
    public static Mob createGuardian(
            ServerLevel level,
            BastionData bastion,
            BlockPos spawnPos) {
        if (NPC_SYSTEM_AVAILABLE) {
            return createNpcGuardian(level, bastion, spawnPos);
        } else {
            return createVanillaGuardian(level, bastion, spawnPos);
        }
    }

    // ===== NPC 守卫创建 =====

    /**
     * 使用 CustomNPC 系统创建守卫。
     */
    private static Mob createNpcGuardian(
            ServerLevel level,
            BastionData bastion,
            BlockPos spawnPos) {
        try {
            // 通过反射创建 NPC
            Object npc = NPC_CREATE_METHOD.invoke(level);
            if (npc == null) {
                LOGGER.warn("NPC 创建失败，回退到原版怪物");
                return createVanillaGuardian(level, bastion, spawnPos);
            }

            Mob mob = (Mob) npc;

            // 设置位置和朝向
            mob.moveTo(
                spawnPos.getX() + GuardianStats.BLOCK_CENTER_OFFSET,
                spawnPos.getY(),
                spawnPos.getZ() + GuardianStats.BLOCK_CENTER_OFFSET,
                level.random.nextFloat() * GuardianStats.FULL_ROTATION_DEGREES,
                0.0f
            );

            // 配置属性
            configureNpcStats(mob, bastion.primaryDao(), bastion.tier());

            // 调用 finalizeSpawn
            mob.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.SPAWNER,
                null
            );

            return mob;
        } catch (Throwable e) {
            LOGGER.error("NPC 守卫创建异常: {}", e.getMessage());
            return createVanillaGuardian(level, bastion, spawnPos);
        }
    }

    /**
     * 配置 NPC 属性（通过反射）。
     */
    private static void configureNpcStats(Mob npc, BastionDao dao, int tier) {
        // 计算基础属性
        float strength = GuardianStats.BASE_STRENGTH
            + (tier - 1) * GuardianStats.TIER_STRENGTH_SCALE;
        float health = GuardianStats.BASE_HEALTH
            + (tier - 1) * GuardianStats.TIER_HEALTH_SCALE;
        float speed = GuardianStats.BASE_SPEED
            + (tier - 1) * GuardianStats.TIER_SPEED_SCALE;
        float defense = GuardianStats.BASE_DEFENSE
            + (tier - 1) * GuardianStats.TIER_DEFENSE_SCALE;
        float sensor = GuardianStats.BASE_SENSOR
            + (tier - 1) * GuardianStats.TIER_SENSOR_SCALE;

        // 应用道途特化倍率
        switch (dao) {
            case ZHI_DAO -> sensor *= GuardianStats.ZHI_DAO_SENSOR_MULT;
            case HUN_DAO -> speed *= GuardianStats.HUN_DAO_SPEED_MULT;
            case MU_DAO -> health *= GuardianStats.MU_DAO_HEALTH_MULT;
            case LI_DAO -> {
                strength *= GuardianStats.LI_DAO_STRENGTH_MULT;
                defense *= GuardianStats.LI_DAO_DEFENSE_MULT;
            }
        }

        try {
            // 通过反射设置属性
            NPC_SET_STRENGTH.invoke(npc, strength);
            NPC_SET_HEALTH.invoke(npc, health);
            NPC_SET_SPEED.invoke(npc, speed);
            NPC_SET_DEFENSE.invoke(npc, defense);
            NPC_SET_SENSOR.invoke(npc, sensor);
            NPC_REFRESH_ATTRIBUTES.invoke(npc);

            // 设置皮肤（基于道途）
            String skinPath = getDaoSkinPath(dao);
            NPC_SET_SKIN.invoke(npc, skinPath);
        } catch (Throwable e) {
            LOGGER.warn("NPC 属性设置失败: {}", e.getMessage());
        }
    }

    /**
     * 获取道途对应的皮肤路径。
     */
    private static String getDaoSkinPath(BastionDao dao) {
        return switch (dao) {
            case ZHI_DAO -> "customnpcs:textures/entity/npc/zhi_dao_guardian.png";
            case HUN_DAO -> "customnpcs:textures/entity/npc/hun_dao_guardian.png";
            case MU_DAO -> "customnpcs:textures/entity/npc/mu_dao_guardian.png";
            case LI_DAO -> "customnpcs:textures/entity/npc/li_dao_guardian.png";
        };
    }

    // ===== 原版守卫创建 =====

    /**
     * 使用原版怪物创建守卫（回退方案）。
     */
    private static Mob createVanillaGuardian(
            ServerLevel level,
            BastionData bastion,
            BlockPos spawnPos) {
        EntityType<? extends Mob> entityType = selectVanillaEntityType(bastion.primaryDao());
        Mob entity = entityType.create(level);
        if (entity == null) {
            return null;
        }

        entity.moveTo(
            spawnPos.getX() + GuardianStats.BLOCK_CENTER_OFFSET,
            spawnPos.getY(),
            spawnPos.getZ() + GuardianStats.BLOCK_CENTER_OFFSET,
            level.random.nextFloat() * GuardianStats.FULL_ROTATION_DEGREES,
            0.0f
        );
        entity.finalizeSpawn(
            level,
            level.getCurrentDifficultyAt(spawnPos),
            MobSpawnType.SPAWNER,
            null
        );

        return entity;
    }

    /**
     * 根据道途选择原版怪物类型。
     */
    private static EntityType<? extends Mob> selectVanillaEntityType(BastionDao dao) {
        return switch (dao) {
            case ZHI_DAO -> EntityType.WITCH;       // 智道 - 女巫
            case HUN_DAO -> EntityType.PHANTOM;     // 魂道 - 幻翼
            case MU_DAO -> EntityType.VINDICATOR;   // 木道 - 卫道士
            case LI_DAO -> EntityType.RAVAGER;      // 力道 - 劫掠兽
        };
    }
}
