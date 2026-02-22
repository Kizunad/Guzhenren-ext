package com.Kizunad.guzhenrenext.config;

import org.apache.commons.lang3.tuple.Pair;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 通用配置（COMMON）。
 * <p>
 * 该配置在服务端与客户端都会加载，用于约束仙窍集群 NPC 的核心运行边界。
 * 当前仅包含 pendingOutput 的硬上限配置，防止长时间运行导致待推送计数异常膨胀。
 * </p>
 */
public class CommonConfig {

    /** pendingOutput 硬上限默认值：足够大，同时保留安全边界。 */
    public static final long DEFAULT_CLUSTER_NPC_PENDING_OUTPUT_HARD_CAP = 1_000_000L;

    public static final CommonConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    /**
     * 集群 NPC 待推送产出硬上限。
     * <p>
     * 该值用于限制 {@code ClusterNpcEntity#pendingOutput} 的最大可达值，
     * 防止在极端长时运行或异常倍率组合下出现不可控累计。
     * </p>
     */
    public final ModConfigSpec.LongValue clusterNpcPendingOutputHardCap;

    static {
        Pair<CommonConfig, ModConfigSpec> pair = new ModConfigSpec.Builder()
            .configure(CommonConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    private CommonConfig(ModConfigSpec.Builder builder) {
        builder.push("common");

        clusterNpcPendingOutputHardCap = builder
            .comment("Cluster NPC pending output hard cap. Prevents unbounded accumulation.")
            .defineInRange(
                "clusterNpcPendingOutputHardCap",
                DEFAULT_CLUSTER_NPC_PENDING_OUTPUT_HARD_CAP,
                1L,
                Long.MAX_VALUE
            );

        builder.pop();
    }
}
