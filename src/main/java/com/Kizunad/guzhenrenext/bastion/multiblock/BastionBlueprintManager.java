package com.Kizunad.guzhenrenext.bastion.multiblock;

import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 蓝图注册与查询管理器。
 */
public final class BastionBlueprintManager {

    /** CODEC：用于序列化整个蓝图管理器（如未来持久化/网络同步）。 */
    public static final Codec<BastionBlueprintManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Codec.STRING, BastionBlueprint.CODEC)
            .fieldOf("blueprints")
            .forGetter(manager -> manager.blueprints)
    ).apply(instance, BastionBlueprintManager::new));

    /** 5x5 净化阵法蓝图 ID。 */
    public static final String PURIFICATION_ARRAY_ID = "bastion_purification_array_5x5";

    private final Map<String, BastionBlueprint> blueprints;

    /** 默认管理器实例（懒加载单例）。 */
    private static final class DefaultHolder {
        private static final BastionBlueprintManager INSTANCE = createDefault();

        private DefaultHolder() {
        }
    }

    private BastionBlueprintManager(Map<String, BastionBlueprint> blueprints) {
        this.blueprints = new HashMap<>(blueprints);
    }

    private BastionBlueprintManager() {
        this.blueprints = new HashMap<>();
    }

    /**
     * 初始化默认管理器实例，注册内置蓝图。
     */
    public static BastionBlueprintManager createDefault() {
        BastionBlueprintManager manager = new BastionBlueprintManager();
        manager.registerBuiltin();
        return manager;
    }

    /**
     * 查询指定 ID 的蓝图。
     *
     * @param id 蓝图唯一标识
     * @return 蓝图可选值
     */
    public Optional<BastionBlueprint> get(String id) {
        return Optional.ofNullable(blueprints.get(id));
    }

    /**
     * 获取默认管理器实例。
     * <p>
     * 使用懒加载单例，避免重复注册内置蓝图。
     * </p>
     */
    public static BastionBlueprintManager getDefault() {
        return DefaultHolder.INSTANCE;
    }

    /**
     * 获取所有蓝图 ID（只读）。
     */
    public static List<String> getAllBlueprintIds() {
        return List.copyOf(getDefault().view().keySet());
    }

    /**
     * 返回只读蓝图视图。
     */
    public Map<String, BastionBlueprint> view() {
        return Collections.unmodifiableMap(blueprints);
    }

    /**
     * 注册内置蓝图。
     */
    private void registerBuiltin() {
        register(PURIFICATION_ARRAY_ID, buildPurificationArray());
    }

    /**
     * 注册蓝图。
     */
    public void register(String id, BastionBlueprint blueprint) {
        blueprints.put(id, blueprint);
    }

    /**
     * 构建 5x5 净化阵法蓝图。
     * <p>
     * 核心位于 (0,0,0)，四角 Anchor 必须；边缘节点为可选增强。
     * </p>
     */
    private BastionBlueprint buildPurificationArray() {
        List<BastionBlueprint.BlockEntry> entries = List.of(
            // 核心（中心）
            new BastionBlueprint.BlockEntry(PositionConstants.CENTER_X, PositionConstants.CENTER_Y,
                PositionConstants.CENTER_Z, BastionBlocks.BASTION_PURIFICATION_ARRAY.get(), true),
            // 四角 Anchor
            new BastionBlueprint.BlockEntry(PositionConstants.MIN_X, PositionConstants.CENTER_Y,
                PositionConstants.MIN_Z, BastionBlocks.BASTION_ANCHOR.get(), true),
            new BastionBlueprint.BlockEntry(PositionConstants.MIN_X, PositionConstants.CENTER_Y,
                PositionConstants.MAX_Z, BastionBlocks.BASTION_ANCHOR.get(), true),
            new BastionBlueprint.BlockEntry(PositionConstants.MAX_X, PositionConstants.CENTER_Y,
                PositionConstants.MIN_Z, BastionBlocks.BASTION_ANCHOR.get(), true),
            new BastionBlueprint.BlockEntry(PositionConstants.MAX_X, PositionConstants.CENTER_Y,
                PositionConstants.MAX_Z, BastionBlocks.BASTION_ANCHOR.get(), true),
            // 边缘节点（可选）
            new BastionBlueprint.BlockEntry(PositionConstants.MIN_X, PositionConstants.CENTER_Y,
                PositionConstants.CENTER_Z, BastionBlocks.BASTION_NODE.get(), false),
            new BastionBlueprint.BlockEntry(PositionConstants.MAX_X, PositionConstants.CENTER_Y,
                PositionConstants.CENTER_Z, BastionBlocks.BASTION_NODE.get(), false),
            new BastionBlueprint.BlockEntry(PositionConstants.CENTER_X, PositionConstants.CENTER_Y,
                PositionConstants.MIN_Z, BastionBlocks.BASTION_NODE.get(), false),
            new BastionBlueprint.BlockEntry(PositionConstants.CENTER_X, PositionConstants.CENTER_Y,
                PositionConstants.MAX_Z, BastionBlocks.BASTION_NODE.get(), false)
        );

        return new BastionBlueprint(
            PURIFICATION_ARRAY_ID,
            "5x5 Purification Array",
            entries
        );
    }

    /**
     * 位置常量，避免 MagicNumber。
     */
    private static final class PositionConstants {
        private static final int MIN_X = -2;
        private static final int MAX_X = 2;
        private static final int MIN_Z = -2;
        private static final int MAX_Z = 2;
        private static final int CENTER_X = 0;
        private static final int CENTER_Y = 0;
        private static final int CENTER_Z = 0;

        private PositionConstants() {
        }
    }
}
