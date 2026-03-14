package com.Kizunad.guzhenrenext.plan2;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Plan2 内容注册统一编排入口。
 * <p>
 * 目标：
 * 1) 将四类别（生物/植物/丹药/灵材）在“实体/方块/物品/菜单/数据加载”上的挂载路径集中到单一清单；
 * 2) 在注册前执行统一校验，避免空注册项、重复 key、类别缺失；
 * 3) 由主入口显式调用，保证初始化时序清晰可追踪。
 * </p>
 */
public final class Plan2RegistrationEntrypoint {

    /**
     * 四类别枚举。
     */
    public enum Category {
        CREATURE,
        PLANT,
        PILL,
        MATERIAL
    }

    /**
     * 启动链路阶段。
     */
    public enum StartupStage {
        MOD_REGISTRATION,
        RELOAD_LISTENER_REGISTRATION
    }

    /**
     * 四类别统一装配槽位。
     * <p>
     * 当前阶段允许“槽位先行”：即具体内容未全部实现时，先固定挂载点与校验约束，
     * 后续只需在既定槽位补内容，不再新增隐式入口。
     * </p>
     */
    public record CategoryAssembly(
        Category category,
        String entityKey,
        String blockKey,
        String itemKey,
        String menuKey,
        String dataReloadPath
    ) {
    }

    /**
     * 默认四类别装配清单（唯一真实入口）。
     */
    private static final List<CategoryAssembly> DEFAULT_ASSEMBLIES = List.of(
        new CategoryAssembly(
            Category.CREATURE,
            "plan2.creature.entity",
            "plan2.creature.block",
            "plan2.creature.item",
            "plan2.creature.menu",
            "plan2/creature"
        ),
        new CategoryAssembly(
            Category.PLANT,
            "plan2.plant.entity",
            "plan2.plant.block",
            "plan2.plant.item",
            "plan2.plant.menu",
            "plan2/plant"
        ),
        new CategoryAssembly(
            Category.PILL,
            "plan2.pill.entity",
            "plan2.pill.block",
            "plan2.pill.item",
            "plan2.pill.menu",
            "plan2/pill"
        ),
        new CategoryAssembly(
            Category.MATERIAL,
            "plan2.material.entity",
            "plan2.material.block",
            "plan2.material.item",
            "plan2.material.menu",
            "plan2/material"
        )
    );

    private Plan2RegistrationEntrypoint() {
    }

    /**
     * 返回默认装配清单副本，用于测试夹具与外部校验，不暴露内部可变状态。
     *
     * @return 默认四类别装配清单副本
     */
    public static List<CategoryAssembly> copyDefaultAssemblies() {
        return new ArrayList<>(DEFAULT_ASSEMBLIES);
    }

    /**
     * 校验四类别装配清单：
     * 1) 类别完整；
     * 2) 所有槽位非空；
     * 3) 所有 key/path 全局唯一。
     *
     * @param assemblies 待校验装配清单
     */
    public static void validateAssembliesOrThrow(final List<CategoryAssembly> assemblies) {
        if (assemblies == null || assemblies.isEmpty()) {
            throw new IllegalStateException("plan2 注册装配清单为空。");
        }

        EnumSet<Category> seenCategories = EnumSet.noneOf(Category.class);
        Set<String> uniqueKeys = new HashSet<>();

        for (CategoryAssembly assembly : assemblies) {
            if (assembly == null) {
                throw new IllegalStateException("plan2 注册装配项存在空条目。");
            }
            seenCategories.add(assembly.category());

            ensureNonBlank("entityKey", assembly.entityKey(), assembly.category());
            ensureNonBlank("blockKey", assembly.blockKey(), assembly.category());
            ensureNonBlank("itemKey", assembly.itemKey(), assembly.category());
            ensureNonBlank("menuKey", assembly.menuKey(), assembly.category());
            ensureNonBlank("dataReloadPath", assembly.dataReloadPath(), assembly.category());

            ensureUnique(uniqueKeys, assembly.entityKey(), "entityKey", assembly.category());
            ensureUnique(uniqueKeys, assembly.blockKey(), "blockKey", assembly.category());
            ensureUnique(uniqueKeys, assembly.itemKey(), "itemKey", assembly.category());
            ensureUnique(uniqueKeys, assembly.menuKey(), "menuKey", assembly.category());
            ensureUnique(uniqueKeys, assembly.dataReloadPath(), "dataReloadPath", assembly.category());
        }

        if (seenCategories.size() != Category.values().length) {
            EnumSet<Category> missing = EnumSet.allOf(Category.class);
            missing.removeAll(seenCategories);
            throw new IllegalStateException("plan2 注册类别缺失: " + missing);
        }
    }

    /**
     * 校验启动链路是否完整覆盖“注册阶段 + 重载监听阶段”。
     *
     * @param stages 已执行阶段集合
     */
    public static void validateStartupChainOrThrow(final Set<StartupStage> stages) {
        if (stages == null || stages.isEmpty()) {
            throw new IllegalStateException("plan2 启动链为空，缺少注册阶段。");
        }
        EnumSet<StartupStage> missing = EnumSet.allOf(StartupStage.class);
        missing.removeAll(stages);
        if (!missing.isEmpty()) {
            throw new IllegalStateException("plan2 启动链缺失阶段: " + missing);
        }
    }

    private static void ensureNonBlank(
        final String field,
        final String value,
        final Category category
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "plan2 注册项为空: category=" + category + ", field=" + field
            );
        }
    }

    private static void ensureUnique(
        final Set<String> uniqueKeys,
        final String key,
        final String field,
        final Category category
    ) {
        if (!uniqueKeys.add(key)) {
            throw new IllegalStateException(
                "plan2 注册 key 重复: category=" + category + ", field=" + field + ", key=" + key
            );
        }
    }
}
