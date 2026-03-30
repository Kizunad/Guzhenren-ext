package com.Kizunad.guzhenrenext.kongqiao;

import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import java.util.List;

/**
 * 空窍生命周期状态归属契约。
 * <p>
 * Task 1 只负责冻结“状态归谁管”的边界，避免后续实现把不同语义的状态混写到同一个附件里。
 * 当代码需要回答“某个状态的权威真相来源是谁”时，应以本契约为唯一准绳。
 * </p>
 * <p>
 * 本类不会提前实现 Task 2 的玩法激活逻辑，也不会提前落地 Task 4 的稳定态字段；
 * 它只声明归属、边界与最低要求。
 * </p>
 */
public final class KongqiaoLifecycleStateContract {

    private static final List<StabilityRequirement> MINIMUM_STABILITY_REQUIREMENTS =
        List.of(
            new StabilityRequirement(
                "burstPressure",
                "服务端权威的瞬时爆发压力值，用于承接主动爆发与短时超压。"
            ),
            new StabilityRequirement(
                "fatigueDebt",
                "服务端权威的恢复债务，用于表达失稳/超压后的延迟代价。"
            ),
            new StabilityRequirement(
                "overloadTier",
                "当前超压等级，用于统一驱动失稳、封槽与恢复流程。"
            ),
            new StabilityRequirement(
                "forcedDisabledUsageIds",
                "被稳定态系统强制熄火的用途集合，不能拿玩家偏好开关代替。"
            ),
            new StabilityRequirement(
                "sealedSlots",
                "被封锁的空窍槽位状态，不能从背包空位或 UI 展示反推。"
            ),
            new StabilityRequirement(
                "lastDecayGameTime",
                "服务端最近一次衰减/恢复结算时间戳，用于恢复节奏判定。"
            )
        );

    private KongqiaoLifecycleStateContract() {}

    public enum StateCategory {
        ATTACHMENT_EXISTENCE(
            KongqiaoData.class,
            true,
            "KongqiaoAttachmentEvents.ensureAttachment",
            "技术层面的空窍附件是否存在。",
            "只代表附件已创建且基础背包可访问，不等同于玩家已经进入空窍玩法。"
        ),

        GAMEPLAY_ACTIVATION(
            KongqiaoData.class,
            false,
            "Task 2 在 KongqiaoData 下新增显式激活态",
            "玩家是否已经正式进入空窍玩法循环。",
            "当前尚未实现；在显式激活态落地前，禁止从附件存在、解锁、偏好、运行态或 UI 反推。"
        ),

        UNLOCK_STATE(
            NianTouUnlocks.class,
            true,
            "NianTouUnlocks",
            "念头用途解锁、杀招解锁、鉴定进程与相关提示消息。",
            "不得把解锁状态塞入 KongqiaoData、TweakConfig 或 ActivePassives。"
        ),

        PREFERENCE_STATE(
            TweakConfig.class,
            true,
            "TweakConfig",
            "玩家对被动开关与轮盘技能列表/顺序的偏好。",
            "它只表达“想不想开”，不表达“能不能跑”或“现在是否正在跑”。"
        ),

        RUNTIME_ACTIVE_STATE(
            ActivePassives.class,
            true,
            "ActivePassives",
            "运行时此刻真正处于激活中的用途 ID 集合。",
            "它只记录结果快照，不补写解锁、偏好、玩法激活或稳定态真相。"
        ),

        STABILITY_STATE(
            KongqiaoData.class,
            false,
            "Task 4 在 KongqiaoData 下新增稳定态字段与同步",
            "压力、疲劳、超压、强制熄火、封槽等稳定性真相。",
            "当前只冻结最小字段要求，不提前新增持久化、同步或运行时计算逻辑。"
        );

        private final Class<?> ownerType;
        private final boolean implementedNow;
        private final String currentAnchor;
        private final String scope;
        private final String boundaryRule;

        StateCategory(
            final Class<?> ownerType,
            final boolean implementedNow,
            final String currentAnchor,
            final String scope,
            final String boundaryRule
        ) {
            this.ownerType = ownerType;
            this.implementedNow = implementedNow;
            this.currentAnchor = currentAnchor;
            this.scope = scope;
            this.boundaryRule = boundaryRule;
        }

        public Class<?> getOwnerType() {
            return ownerType;
        }

        public boolean isImplementedNow() {
            return implementedNow;
        }

        public String getCurrentAnchor() {
            return currentAnchor;
        }

        public String getScope() {
            return scope;
        }

        public String getBoundaryRule() {
            return boundaryRule;
        }
    }

    public record StabilityRequirement(String fieldName, String meaning) {}

    public static List<StabilityRequirement> minimumStabilityRequirements() {
        return MINIMUM_STABILITY_REQUIREMENTS;
    }
}
