package com.Kizunad.guzhenrenext.bastion.threat;

/**
 * 基地威胁事件接口。
 * <p>
 * 威胁事件是基地在节点被拆除后的随机报复机制，用于增加拆除节点的风险和紧张感。
 * </p>
 * <p>
 * 事件类型：
 * <ul>
 *   <li>RadiationPulse - 辐射脉冲：对范围内玩家造成伤害</li>
 *   <li>HunterSpawn - 猎手生成：生成追踪玩家的守卫</li>
 *   <li>ExpansionSurge - 扩张涌动：加速节点扩张</li>
 * </ul>
 * </p>
 */
public interface IThreatEvent {

    /**
     * 返回事件的唯一标识符。
     *
     * @return 事件 ID（如 "radiation_pulse"）
     */
    String getId();

    /**
     * 返回此事件的基础权重。
     * <p>
     * 权重越高，被随机选中的概率越大。
     * </p>
     *
     * @return 基础权重值
     */
    int getBaseWeight();

    /**
     * 检查此事件是否可以在当前上下文中触发。
     * <p>
     * 可用于检查前置条件，如：
     * <ul>
     *   <li>基地转数是否达到要求</li>
     *   <li>目标玩家是否存在</li>
     *   <li>是否已经处于冷却中</li>
     * </ul>
     * </p>
     *
     * @param context 威胁事件上下文
     * @return true 如果事件可以触发
     */
    boolean canTrigger(ThreatEventContext context);

    /**
     * 执行威胁事件。
     *
     * @param context 威胁事件上下文
     */
    void execute(ThreatEventContext context);
}
