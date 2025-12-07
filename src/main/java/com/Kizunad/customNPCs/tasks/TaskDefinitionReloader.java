package com.Kizunad.customNPCs.tasks;

import com.Kizunad.customNPCs.tasks.objective.KillEntityObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.SubmitItemObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveType;
import com.Kizunad.customNPCs.tasks.reward.ItemRewardDefinition;
import com.Kizunad.customNPCs.tasks.reward.TaskRewardDefinition;
import com.Kizunad.customNPCs.tasks.reward.TaskRewardType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务数据包重载监听器。
 */
public class TaskDefinitionReloader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        TaskDefinitionReloader.class
    );
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    private static final String FOLDER = "tasks";
    private static final double DEFAULT_KILL_SPAWN_RANGE = 24.0D;

    public TaskDefinitionReloader() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(
        Map<ResourceLocation, JsonElement> objects,
        ResourceManager resourceManager,
        ProfilerFiller profiler
    ) {
        Map<ResourceLocation, TaskDefinition> loaded = new LinkedHashMap<>();
        objects.forEach((fileId, json) -> {
            try {
                JsonObject root = GsonHelper.convertToJsonObject(json, "task");
                TaskDefinition definition = parseTask(fileId, root);
                if (definition != null) {
                    loaded.put(definition.id(), definition);
                }
            } catch (Exception ex) {
                LOGGER.warn(
                    "[TaskRegistry] 无法解析任务 {}: {}",
                    fileId,
                    ex.getMessage()
                );
            }
        });
        TaskRegistry.getInstance().reload(loaded);
    }

    private TaskDefinition parseTask(ResourceLocation fileId, JsonObject root) {
        ResourceLocation id = fileId;
        if (root.has("id")) {
            ResourceLocation override = ResourceLocation.tryParse(
                GsonHelper.getAsString(root, "id")
            );
            if (override != null) {
                id = override;
            }
        }
        String title = GsonHelper.getAsString(root, "title", id.toString());
        String description = GsonHelper.getAsString(root, "description", "");
        TaskType type = TaskType.fromString(
            GsonHelper.getAsString(root, "type", "side")
        );

        List<String> dependencies = new ArrayList<>();
        if (root.has("dependencies")) {
            JsonArray depArray = GsonHelper.getAsJsonArray(
                root,
                "dependencies"
            );
            for (JsonElement e : depArray) {
                dependencies.add(e.getAsString());
            }
        }
        for (String modId : dependencies) {
            boolean loaded = ModList.get().isLoaded(modId);
            LOGGER.debug(
                "Task {} checking dependency {}: {}",
                id,
                modId,
                loaded
            );
            if (!loaded) {
                LOGGER.info(
                    "Task {} skipped due to missing dependency: {}",
                    id,
                    modId
                );
                return null;
            }
        }

        List<TaskObjectiveDefinition> objectives = parseObjectives(
            GsonHelper.getAsJsonArray(root, "objectives")
        );
        if (objectives.isEmpty()) {
            throw new IllegalStateException("任务缺少 objectives");
        }
        List<TaskRewardDefinition> rewards = parseRewards(
            GsonHelper.getAsJsonArray(root, "rewards")
        );
        if (rewards.isEmpty()) {
            throw new IllegalStateException("任务缺少 rewards");
        }
        return new TaskDefinition(
            id,
            title,
            description,
            type,
            dependencies,
            objectives,
            rewards
        );
    }

    private List<TaskObjectiveDefinition> parseObjectives(
        JsonArray arrayElement
    ) {
        List<TaskObjectiveDefinition> result = new ArrayList<>();
        if (arrayElement == null) {
            return result;
        }
        for (JsonElement element : arrayElement) {
            JsonObject obj = GsonHelper.convertToJsonObject(
                element,
                "objective"
            );
            TaskObjectiveType type = TaskObjectiveType.fromString(
                GsonHelper.getAsString(obj, "type", "submit_item")
            );
            if (type == TaskObjectiveType.SUBMIT_ITEM) {
                result.add(parseSubmitItemObjective(obj));
            } else if (type == TaskObjectiveType.KILL_ENTITY) {
                result.add(parseKillEntityObjective(obj));
            }
        }
        return result;
    }

    private KillEntityObjectiveDefinition parseKillEntityObjective(
        JsonObject obj
    ) {
        ResourceLocation entityId = ResourceLocation.tryParse(
            GsonHelper.getAsString(obj, "entity")
        );
        if (entityId == null) {
            throw new IllegalArgumentException(
                "kill_entity objective 缺少 entity"
            );
        }
        EntityType<?> rawType = BuiltInRegistries.ENTITY_TYPE.getOptional(
            entityId
        ).orElseThrow(() ->
            new IllegalArgumentException("未知实体: " + entityId)
        );
        Class<?> baseClass = rawType.getBaseClass();
        boolean isLiving = baseClass != null &&
            LivingEntity.class.isAssignableFrom(baseClass);
        if (!isLiving) {
            LOGGER.warn(
                "kill_entity 目标 {} 基类 {} 非 LivingEntity，仍尝试加载（将在生成时校验）",
                entityId,
                baseClass
            );
        }
        @SuppressWarnings("unchecked")
        EntityType<? extends LivingEntity> entityType = (EntityType<
            ? extends LivingEntity
        >) rawType;

        int required = GsonHelper.getAsInt(obj, "count", 1);
        double radius = GsonHelper.getAsDouble(
            obj,
            "spawn_range",
            DEFAULT_KILL_SPAWN_RANGE
        );
        String name = GsonHelper.getAsString(obj, "name", "");
        JsonObject attributes = obj.has("attributes")
            ? GsonHelper.getAsJsonObject(obj, "attributes")
            : null;
        Double maxHealth = attributes != null && attributes.has("max_health")
            ? GsonHelper.getAsDouble(attributes, "max_health", 0.0D)
            : null;
        Double moveSpeed = null;
        if (attributes != null) {
            if (attributes.has("move_speed")) {
                moveSpeed = GsonHelper.getAsDouble(attributes, "move_speed", 0.0D);
            } else if (attributes.has("movement_speed")) {
                moveSpeed = GsonHelper.getAsDouble(
                    attributes,
                    "movement_speed",
                    0.0D
                );
            }
        }
        Double attackDamage = attributes != null && attributes.has("attack_damage")
            ? GsonHelper.getAsDouble(attributes, "attack_damage", 0.0D)
            : null;
        if (maxHealth != null && maxHealth <= 0.0D) {
            maxHealth = null;
        }
        if (moveSpeed != null && moveSpeed <= 0.0D) {
            moveSpeed = null;
        }
        if (attackDamage != null && attackDamage <= 0.0D) {
            attackDamage = null;
        }
        var nameComponent = name.isEmpty() ? null : Component.literal(name);
        Map<EquipmentSlot, ItemStack> armor = parseArmor(attributes);
        return new KillEntityObjectiveDefinition(
            entityType,
            required,
            radius,
            nameComponent,
            maxHealth,
            moveSpeed,
            attackDamage,
            armor
        );
    }

    private Map<EquipmentSlot, ItemStack> parseArmor(
        @Nullable JsonObject attributes
    ) {
        if (attributes == null || !attributes.has("armor")) {
            return java.util.Collections.emptyMap();
        }
        JsonObject armorObj = GsonHelper.getAsJsonObject(attributes, "armor");
        Map<EquipmentSlot, ItemStack> map = new EnumMap<>(EquipmentSlot.class);
        parseArmorSlot(armorObj, "head", EquipmentSlot.HEAD, map);
        parseArmorSlot(armorObj, "chest", EquipmentSlot.CHEST, map);
        parseArmorSlot(armorObj, "legs", EquipmentSlot.LEGS, map);
        parseArmorSlot(armorObj, "feet", EquipmentSlot.FEET, map);
        return map;
    }

    private void parseArmorSlot(
        JsonObject obj,
        String key,
        EquipmentSlot slot,
        Map<EquipmentSlot, ItemStack> map
    ) {
        if (!obj.has(key)) {
            return;
        }
        ResourceLocation itemId = ResourceLocation.tryParse(
            GsonHelper.getAsString(obj, key)
        );
        if (itemId == null) {
            throw new IllegalArgumentException("armor." + key + " 非法物品 ID");
        }
        Item item = BuiltInRegistries.ITEM
            .getOptional(itemId)
            .orElseThrow(() ->
                new IllegalArgumentException("未知盔甲物品: " + itemId)
            );
        map.put(slot, new ItemStack(item));
    }

    private SubmitItemObjectiveDefinition parseSubmitItemObjective(
        JsonObject obj
    ) {
        ResourceLocation itemId = ResourceLocation.tryParse(
            GsonHelper.getAsString(obj, "item")
        );
        if (itemId == null) {
            throw new IllegalArgumentException(
                "submit_item objective 缺少 item"
            );
        }
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElseThrow(() ->
            new IllegalArgumentException("未知物品: " + itemId)
        );
        CompoundTag nbt = parseNbt(obj, "nbt");
        Holder<Potion> potion = extractPotion(nbt);
        nbt = stripPotionKey(nbt);
        int count = GsonHelper.getAsInt(obj, "count", 1);
        float value = GsonHelper.getAsFloat(obj, "base_value_snapshot", 0.0F);
        return new SubmitItemObjectiveDefinition(
            item,
            nbt,
            potion,
            count,
            value
        );
    }

    private List<TaskRewardDefinition> parseRewards(JsonArray arrayElement) {
        List<TaskRewardDefinition> result = new ArrayList<>();
        if (arrayElement == null) {
            return result;
        }
        for (JsonElement element : arrayElement) {
            JsonObject obj = GsonHelper.convertToJsonObject(element, "reward");
            TaskRewardType type = TaskRewardType.fromString(
                GsonHelper.getAsString(obj, "type", "item")
            );
            if (type == TaskRewardType.ITEM) {
                result.add(parseItemReward(obj));
            }
        }
        return result;
    }

    private TaskRewardDefinition parseItemReward(JsonObject obj) {
        ResourceLocation itemId = ResourceLocation.tryParse(
            GsonHelper.getAsString(obj, "item")
        );
        if (itemId == null) {
            throw new IllegalArgumentException("item reward 缺少物品 ID");
        }
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElseThrow(() ->
            new IllegalArgumentException("未知奖励物品: " + itemId)
        );
        int count = Math.max(1, GsonHelper.getAsInt(obj, "count", 1));
        CompoundTag nbt = parseNbt(obj, "nbt");
        ItemStack stack = createItemStackWithComponents(item, count, nbt);
        return new ItemRewardDefinition(stack);
    }

    private CompoundTag parseNbt(JsonObject obj, String key) {
        if (!obj.has(key)) {
            return null;
        }
        String raw = GsonHelper.getAsString(obj, key);
        try {
            return TagParser.parseTag(raw);
        } catch (CommandSyntaxException e) {
            LOGGER.warn("解析 NBT 失败: {}", raw);
            return null;
        }
    }

    private Holder<Potion> extractPotion(@Nullable CompoundTag nbt) {
        if (nbt == null || !nbt.contains("Potion")) {
            return null;
        }
        ResourceLocation potionId = ResourceLocation.tryParse(
            nbt.getString("Potion")
        );
        if (potionId == null) {
            return null;
        }
        return BuiltInRegistries.POTION.getHolder(potionId).orElse(null);
    }

    private CompoundTag stripPotionKey(@Nullable CompoundTag nbt) {
        if (nbt == null || !nbt.contains("Potion")) {
            return nbt;
        }
        CompoundTag copy = nbt.copy();
        copy.remove("Potion");
        return copy.isEmpty() ? null : copy;
    }

    private ItemStack createItemStackWithComponents(
        Item item,
        int count,
        @Nullable CompoundTag nbt
    ) {
        Holder<Potion> potion = extractPotion(nbt);
        CompoundTag remaining = stripPotionKey(nbt);
        ItemStack stack;
        if (potion != null && isPotionItem(item)) {
            stack = PotionContents.createItemStack(item, potion);
            stack.setCount(count);
        } else {
            stack = new ItemStack(item, count);
        }
        if (remaining != null && !remaining.isEmpty()) {
            stack.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(remaining.copy())
            );
        }
        return stack;
    }

    private boolean isPotionItem(Item item) {
        return (
            item == Items.POTION ||
            item == Items.LINGERING_POTION ||
            item == Items.SPLASH_POTION ||
            item == Items.TIPPED_ARROW
        );
    }
}
