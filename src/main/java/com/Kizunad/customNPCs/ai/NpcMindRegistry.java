package com.Kizunad.customNPCs.ai;

import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.actions.common.BlockWithShieldAction;
import com.Kizunad.customNPCs.ai.actions.common.RangedAttackItemAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.decision.goals.CookGoal;
import com.Kizunad.customNPCs.ai.decision.goals.CraftItemGoal;
import com.Kizunad.customNPCs.ai.decision.goals.DefendGoal;
import com.Kizunad.customNPCs.ai.decision.goals.EquipArmorGoal;
import com.Kizunad.customNPCs.ai.decision.goals.HealGoal;
import com.Kizunad.customNPCs.ai.decision.goals.FleeGoal;
import com.Kizunad.customNPCs.ai.decision.goals.IdleGoal;
import com.Kizunad.customNPCs.ai.decision.goals.SatiateGoal;
import com.Kizunad.customNPCs.ai.decision.goals.SeekShelterGoal;
import com.Kizunad.customNPCs.ai.decision.goals.HuntGoal;
import com.Kizunad.customNPCs.ai.decision.goals.SurvivalGoal;
import com.Kizunad.customNPCs.ai.decision.goals.WatchClosestEntityGoal;
import com.Kizunad.customNPCs.ai.sensors.AuditorySensor;
import com.Kizunad.customNPCs.ai.sensors.DamageSensor;
import com.Kizunad.customNPCs.ai.sensors.ISensor;
import com.Kizunad.customNPCs.ai.sensors.SafetySensor;
import com.Kizunad.customNPCs.ai.sensors.VisionSensor;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * NpcMind 组件注册表
 * <p>
 * 负责集中管理可用的 Goal / Sensor / Action 工厂，并提供统一的初始化入口，
 * 方便其他模组或后续功能进行扩展，避免到处硬编码。
 */
public final class NpcMindRegistry {

    private static final Map<String, Supplier<IGoal>> GOAL_FACTORIES =
        new LinkedHashMap<>();
    private static final Map<String, Supplier<ISensor>> SENSOR_FACTORIES =
        new LinkedHashMap<>();
    private static final Map<String, Supplier<IAction>> ACTION_FACTORIES =
        new LinkedHashMap<>();

    private NpcMindRegistry() {
        // 工具类，禁止实例化
    }

    static {
        // 注册内置组件，保持与现有默认行为一致
        registerGoal("survival", SurvivalGoal::new);
        registerGoal("watch_closest_entity", WatchClosestEntityGoal::new);
        registerGoal("idle", IdleGoal::new);
        registerGoal("equip_armor", EquipArmorGoal::new);
        registerGoal("satiate", SatiateGoal::new);
        registerGoal("flee", FleeGoal::new);
        registerGoal("defend", DefendGoal::new);
        registerGoal("seek_shelter", SeekShelterGoal::new);
        registerGoal("heal", HealGoal::new);
        registerGoal("cook", CookGoal::new);
        registerGoal("hunt", HuntGoal::new);
        registerGoal("craft_item", CraftItemGoal::new);

        registerSensor("vision", VisionSensor::new);
        registerSensor("damage", DamageSensor::new);
        registerSensor("auditory", AuditorySensor::new);
        registerSensor("safety", SafetySensor::new);

        registerAction("block_with_shield", BlockWithShieldAction::new);
        registerAction("ranged_attack", () -> new RangedAttackItemAction(null));
    }

    /**
     * 注册 Goal 工厂（名称唯一）
     * @param name 目标标识
     * @param factory 目标创建工厂
     */
    public static void registerGoal(String name, Supplier<IGoal> factory) {
        register(name, factory, GOAL_FACTORIES, "Goal");
    }

    /**
     * 注册 Sensor 工厂（名称唯一）
     * @param name 传感器标识
     * @param factory 传感器创建工厂
     */
    public static void registerSensor(String name, Supplier<ISensor> factory) {
        register(name, factory, SENSOR_FACTORIES, "Sensor");
    }

    /**
     * 注册 Action 工厂（名称唯一）
     * @param name 动作标识
     * @param factory 动作创建工厂
     */
    public static void registerAction(String name, Supplier<IAction> factory) {
        register(name, factory, ACTION_FACTORIES, "Action");
    }

    /**
     * 为指定的 NpcMind 初始化已注册的默认 Goal 与 Sensor。
     * <p>
     * 若目标/传感器已存在（按名称判定），则不会重复注册。
     *
     * @param mind NpcMind 实例
     * @return 是否有新增的组件被注册
     */
    public static boolean initializeMind(INpcMind mind) {
        Objects.requireNonNull(mind, "mind 不能为空");

        boolean updated = false;

        for (ISensor sensor : createSensors()) {
            if (!hasSensor(mind, sensor.getName())) {
                mind.getSensorManager().registerSensor(sensor);
                updated = true;
            }
        }

        for (IGoal goal : createGoals()) {
            if (!mind.getGoalSelector().containsGoal(goal.getName())) {
                mind.getGoalSelector().registerGoal(goal);
                updated = true;
            }
        }

        return updated;
    }

    /**
     * 按名称创建 Goal 实例。
     * @param name Goal 名称
     * @return Goal 实例，未注册则返回 null
     */
    public static IGoal createGoal(String name) {
        Supplier<IGoal> factory = GOAL_FACTORIES.get(name);
        return factory == null ? null : factory.get();
    }

    /**
     * 按名称创建 Sensor 实例。
     * @param name Sensor 名称
     * @return Sensor 实例，未注册则返回 null
     */
    public static ISensor createSensor(String name) {
        Supplier<ISensor> factory = SENSOR_FACTORIES.get(name);
        return factory == null ? null : factory.get();
    }

    /**
     * 按名称创建 Action 实例。
     * @param name Action 名称
     * @return Action 实例，未注册则返回 null
     */
    public static IAction createAction(String name) {
        Supplier<IAction> factory = ACTION_FACTORIES.get(name);
        return factory == null ? null : factory.get();
    }

    /**
     * 获取已注册的 Goal 名称列表（只读）。
     */
    public static List<String> getRegisteredGoalNames() {
        synchronized (GOAL_FACTORIES) {
            return Collections.unmodifiableList(
                new ArrayList<>(GOAL_FACTORIES.keySet())
            );
        }
    }

    /**
     * 获取已注册的 Sensor 名称列表（只读）。
     */
    public static List<String> getRegisteredSensorNames() {
        synchronized (SENSOR_FACTORIES) {
            return Collections.unmodifiableList(
                new ArrayList<>(SENSOR_FACTORIES.keySet())
            );
        }
    }

    /**
     * 获取已注册的 Action 名称列表（只读）。
     */
    public static List<String> getRegisteredActionNames() {
        synchronized (ACTION_FACTORIES) {
            return Collections.unmodifiableList(
                new ArrayList<>(ACTION_FACTORIES.keySet())
            );
        }
    }

    private static <T> void register(
        String name,
        Supplier<T> factory,
        Map<String, Supplier<T>> registry,
        String typeName
    ) {
        Objects.requireNonNull(name, typeName + " 名称不能为空");
        Objects.requireNonNull(factory, typeName + " 工厂不能为空");
        String key = name.trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                typeName + " 名称不能为空字符串"
            );
        }

        synchronized (registry) {
            if (registry.containsKey(key)) {
                throw new IllegalArgumentException(
                    typeName + " 已注册: " + key
                );
            }
            registry.put(key, factory);
        }
    }

    private static List<IGoal> createGoals() {
        List<IGoal> goals = new ArrayList<>();
        synchronized (GOAL_FACTORIES) {
            for (Supplier<IGoal> factory : GOAL_FACTORIES.values()) {
                goals.add(factory.get());
            }
        }
        return goals;
    }

    private static List<ISensor> createSensors() {
        List<ISensor> sensors = new ArrayList<>();
        synchronized (SENSOR_FACTORIES) {
            for (Supplier<ISensor> factory : SENSOR_FACTORIES.values()) {
                sensors.add(factory.get());
            }
        }
        return sensors;
    }

    private static boolean hasSensor(INpcMind mind, String sensorName) {
        return mind
            .getSensorManager()
            .getSensors()
            .stream()
            .anyMatch(sensor -> sensor.getName().equals(sensorName));
    }
}
