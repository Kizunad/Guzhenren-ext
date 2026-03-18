package com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordExpCalculator;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthData;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthTuning;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordStatCalculator;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;

/**
 * 飞剑属性数据类。
 * <p>
 * 包含飞剑的所有属性值：
 * <ul>
 *     <li>成长数据：品质、等级、经验（通过 {@link SwordGrowthData}）</li>
 *     <li>运动属性：速度、加速度、转向率</li>
 *     <li>战斗属性：伤害、攻击冷却</li>
 *     <li>耐久属性：当前耐久、最大耐久</li>
 * </ul>
 * </p>
 * <p>
 * 属性计算优先级：
 * <ol>
 *     <li>成长数据计算基础值（品质 + 等级）</li>
 *     <li>应用外部修正器（道痕、领域等）</li>
 *     <li>应用临时 buff/debuff</li>
 * </ol>
 * </p>
 */
public class FlyingSwordAttributes {

    // ==================== NBT 键名 ====================

    private static final String TAG_GROWTH_DATA = "GrowthData";
    private static final String TAG_DURABILITY = "Durability";
    private static final String TAG_SPEED_BASE = "SpeedBase";
    private static final String TAG_SPEED_MAX = "SpeedMax";
    private static final String TAG_ACCEL = "Accel";
    private static final String TAG_TURN_RATE = "TurnRate";
    private static final String TAG_DAMAGE = "Damage";
    private static final String TAG_ATTACK_COOLDOWN = "AttackCooldown";
    private static final String TAG_MAX_DURABILITY = "MaxDurability";
    private static final String TAG_IMPRINT = "Imprint";
    private static final String TAG_SPIRIT_DATA = "SpiritData";
    private static final String TAG_STABLE_SWORD_ID = "stableSwordId";
    private static final String TAG_BOND = "bond";

    // ==================== 默认值（凡品1级） ====================

    public static final double DEFAULT_SPEED_BASE =
        SwordGrowthTuning.BASE_SPEED_BASE;
    public static final double DEFAULT_SPEED_MAX =
        SwordGrowthTuning.BASE_SPEED_MAX;
    public static final double DEFAULT_ACCEL = SwordGrowthTuning.BASE_ACCEL;
    public static final double DEFAULT_TURN_RATE =
        SwordGrowthTuning.BASE_TURN_RATE;
    public static final double DEFAULT_DAMAGE = SwordGrowthTuning.BASE_DAMAGE;
    public static final double DEFAULT_MAX_DURABILITY =
        SwordGrowthTuning.BASE_MAX_DURABILITY;
    public static final int DEFAULT_ATTACK_COOLDOWN =
        SwordGrowthTuning.BASE_ATTACK_COOLDOWN;

    // ==================== 成长数据 ====================

    /** 飞剑成长数据（品质、等级、经验） */
    private final SwordGrowthData growthData;

    /** 剑灵数据（好感度、心情） */
    private final SwordSpiritData spiritData = new SwordSpiritData();

    /** 飞剑稳定标识（规范主键，不依赖实体 UUID）。 */
    private String stableSwordId = UUID.randomUUID().toString();

    /** 本命绑定主状态（最小字段：ownerUuid + resonance）。 */
    private final BenmingSwordBond bond = new BenmingSwordBond();

    // ==================== 运动属性 ====================

    /** 初始速度（格/tick） */
    public double speedBase = DEFAULT_SPEED_BASE;

    /** 最大速度（格/tick） */
    public double speedMax = DEFAULT_SPEED_MAX;

    /** 加速度 */
    public double accel = DEFAULT_ACCEL;

    /** 转向速率（度/tick） */
    public double turnRate = DEFAULT_TURN_RATE;

    // ==================== 战斗属性 ====================

    /** 攻击伤害 */
    public double damage = DEFAULT_DAMAGE;

    /** 攻击冷却（tick） */
    public int attackCooldown = DEFAULT_ATTACK_COOLDOWN;

    // ==================== 耐久属性 ====================

    /** 当前耐久 */
    public double durability = DEFAULT_MAX_DURABILITY;

    /** 最大耐久 */
    public double maxDurability = DEFAULT_MAX_DURABILITY;

    // ==================== 临时修正（不保存） ====================

    /** 速度临时倍率（领域、buff 等） */
    private transient double speedMultiplier = 1.0;

    /** 伤害临时倍率 */
    private transient double damageMultiplier = 1.0;

    /**
     * 共鸣战斗映射最小冷却。
     * <p>
     * 说明：即便攻相提供追击/追打优势，也不能把冷却压到 0，避免出现异常高频攻击。
     * </p>
     */
    private static final int RESONANCE_MIN_ATTACK_COOLDOWN_TICKS = 1;

    /** 召回稳定性速度下限，避免极低速时出现除零或不稳定结果。 */
    private static final double RECALL_STABILITY_SPEED_FLOOR = 0.05D;

    /**
     * 缺省共鸣档位。
     * <p>
     * 对应“未缔结 / 未同步 / 未知分型”场景，所有倍率均保持基线，确保旧档与异常输入安全回退。
     * </p>
     */
    private static final ResonanceMappedProfile BASELINE_RESONANCE_PROFILE =
        new ResonanceMappedProfile(1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D);

    private static final double OFFENSE_DAMAGE_MULTIPLIER = 1.22D;
    private static final double OFFENSE_SPEED_MULTIPLIER = 1.08D;
    private static final double OFFENSE_PURSUIT_SPEED_MULTIPLIER = 1.12D;
    private static final double OFFENSE_ATTACK_COOLDOWN_MULTIPLIER = 0.86D;
    private static final double OFFENSE_GUARD_DURABILITY_COST_MULTIPLIER = 1.18D;
    private static final double OFFENSE_RECALL_STABILITY_MULTIPLIER = 0.88D;

    private static final double DEFENSE_DAMAGE_MULTIPLIER = 0.92D;
    private static final double DEFENSE_SPEED_MULTIPLIER = 0.94D;
    private static final double DEFENSE_PURSUIT_SPEED_MULTIPLIER = 0.90D;
    private static final double DEFENSE_ATTACK_COOLDOWN_MULTIPLIER = 1.12D;
    private static final double DEFENSE_GUARD_DURABILITY_COST_MULTIPLIER = 0.82D;
    private static final double DEFENSE_RECALL_STABILITY_MULTIPLIER = 1.20D;

    private static final double SPIRIT_DAMAGE_MULTIPLIER = 1.05D;
    private static final double SPIRIT_SPEED_MULTIPLIER = 1.06D;
    private static final double SPIRIT_PURSUIT_SPEED_MULTIPLIER = 1.08D;
    private static final double SPIRIT_ATTACK_COOLDOWN_MULTIPLIER = 0.95D;
    private static final double SPIRIT_GUARD_DURABILITY_COST_MULTIPLIER = 0.95D;
    private static final double SPIRIT_RECALL_STABILITY_MULTIPLIER = 1.10D;

    /**
     * 三相共鸣统一映射表。
     * <p>
     * 这里集中定义“手感差异”倍率，避免 Combat/HUD/调用方各自硬编码分型逻辑。
     * </p>
     */
    private static final Map<FlyingSwordResonanceType, ResonanceMappedProfile>
        RESONANCE_PROFILE_MAP = createResonanceProfileMap();

    private final com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprint imprint =
        new com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprint();

    // ==================== 构造 ====================

    public com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprint getImprint() {
        return imprint;
    }

    /**
     * 创建默认属性（凡品1级）。
     */
    public FlyingSwordAttributes() {
        this.growthData = SwordGrowthData.create(SwordQuality.COMMON);
        recalculateFromGrowth();
    }

    /**
     * 创建指定品质的属性。
     *
     * @param quality 初始品质
     */
    public FlyingSwordAttributes(@Nullable SwordQuality quality) {
        this.growthData = SwordGrowthData.create(quality);
        recalculateFromGrowth();
    }

    /**
     * 创建指定品质和等级的属性。
     *
     * @param quality 初始品质
     * @param level   初始等级
     */
    public FlyingSwordAttributes(@Nullable SwordQuality quality, int level) {
        this.growthData = SwordGrowthData.create(quality, level);
        recalculateFromGrowth();
    }

    /**
     * 从成长数据创建属性。
     *
     * @param growthData 成长数据（会复制）
     */
    public FlyingSwordAttributes(SwordGrowthData growthData) {
        this.growthData =
            growthData != null
                ? growthData.copy()
                : SwordGrowthData.create(SwordQuality.COMMON);
        recalculateFromGrowth();
    }

    // ==================== 成长数据访问 ====================

    /**
     * 获取成长数据。
     * <p>
     * 注意：返回的是内部引用，修改会影响属性。
     * 如需安全读取，请使用 {@link #getGrowthDataCopy()}。
     * </p>
     */
    public SwordGrowthData getGrowthData() {
        return growthData;
    }

    /**
     * 获取剑灵数据（直接引用，修改会反映到本实例）。
     *
     * @return 剑灵数据引用
     */
    public SwordSpiritData getSpiritData() {
        return spiritData;
    }

    public String getStableSwordId() {
        return stableSwordId;
    }

    public void setStableSwordId(@Nullable String stableSwordId) {
        this.stableSwordId = normalizeStableSwordId(stableSwordId);
    }

    public BenmingSwordBond getBond() {
        return bond;
    }

    /**
     * 获取成长数据的副本。
     */
    public SwordGrowthData getGrowthDataCopy() {
        return growthData.copy();
    }

    /**
     * 获取当前品质。
     */
    public SwordQuality getQuality() {
        return growthData.getQuality();
    }

    /**
     * 获取当前等级。
     */
    public int getLevel() {
        return growthData.getLevel();
    }

    /**
     * 获取当前经验。
     */
    public int getExperience() {
        return growthData.getExperience();
    }

    /**
     * 获取升到下一级所需的经验。
     *
     * @return 所需经验，已满级返回 0
     */
    public int getExpForNextLevel() {
        int expToNext = SwordExpCalculator.calculateExpToNextLevel(
            growthData.getLevel(),
            growthData.getQuality()
        );
        if (expToNext == Integer.MAX_VALUE) {
            return 0; // 满级
        }
        return expToNext;
    }

    // ==================== 属性重算 ====================

    /**
     * 根据成长数据重新计算所有属性。
     * <p>
     * 在以下情况下应调用：
     * <ul>
     *     <li>升级后</li>
     *     <li>突破后</li>
     *     <li>从 NBT 恢复后</li>
     * </ul>
     * </p>
     */
    public void recalculateFromGrowth() {
        SwordStatCalculator.SwordStats stats = growthData.getCurrentStats();

        this.damage = stats.damage;
        this.speedMax = stats.speedMax;
        this.speedBase = stats.speedBase;
        this.accel = stats.accel;
        this.turnRate = stats.turnRate;
        this.maxDurability = stats.maxDurability;
        this.attackCooldown = stats.attackCooldown;

        // 确保当前耐久不超过最大值
        if (this.durability > this.maxDurability) {
            this.durability = this.maxDurability;
        }
    }

    // ==================== 经验与升级 ====================

    /**
     * 添加经验（自动升级和属性重算）。
     *
     * @param amount 经验量
     * @return 升级结果
     */
    public SwordGrowthData.ExpAddResult addExperience(int amount) {
        SwordGrowthData.ExpAddResult result = growthData.addExperience(amount);

        // 如果升级了，重算属性
        if (result.levelsGained > 0) {
            recalculateFromGrowth();
        }

        return result;
    }

    /**
     * 尝试突破到下一品质。
     *
     * @return 突破结果
     */
    public SwordExpCalculator.BreakthroughResult tryBreakthrough() {
        var result = growthData.tryBreakthrough();

        // 如果突破成功，重算属性
        if (result.success) {
            recalculateFromGrowth();
        }

        return result;
    }

    // ==================== 有效属性（应用临时修正） ====================

    /**
     * 获取有效最大速度（应用临时修正）。
     */
    public double getEffectiveSpeedMax() {
        return speedMax * speedMultiplier;
    }

    /**
     * 获取有效初始速度（应用临时修正）。
     */
    public double getEffectiveSpeedBase() {
        return speedBase * speedMultiplier;
    }

    /**
     * 获取有效加速度（应用临时修正）。
     */
    public double getEffectiveAccel() {
        // 加速度修正较小，使用平方根
        return accel * Math.sqrt(speedMultiplier);
    }

    /**
     * 获取有效伤害（应用临时修正）。
     */
    public double getEffectiveDamage() {
        return damage * damageMultiplier;
    }

    /**
     * 获取带共鸣映射的有效伤害。
     *
     * @param resonanceRaw 共鸣原始字符串（可为空/未知）
     * @return 最终伤害值（含既有 multiplier + 三相映射）
     */
    public double getEffectiveDamage(@Nullable String resonanceRaw) {
        ResonanceMappedProfile profile = resolveResonanceProfile(resonanceRaw);
        return getEffectiveDamage() * profile.damageMultiplier();
    }

    /**
     * 获取带共鸣映射的有效速度上限。
     *
     * @param resonanceRaw 共鸣原始字符串（可为空/未知）
     * @return 最终速度上限（含既有 multiplier + 三相映射）
     */
    public double getEffectiveSpeedMax(@Nullable String resonanceRaw) {
        ResonanceMappedProfile profile = resolveResonanceProfile(resonanceRaw);
        return getEffectiveSpeedMax() * profile.speedMultiplier();
    }

    /**
     * 获取带共鸣映射的追击速度。
     * <p>
     * 语义：用于战斗追击段落的速度倾向，和一般巡航速度区分开。
     * </p>
     *
     * @param resonanceRaw 共鸣原始字符串（可为空/未知）
     * @return 最终追击速度
     */
    public double getEffectivePursuitSpeed(@Nullable String resonanceRaw) {
        ResonanceMappedProfile profile = resolveResonanceProfile(resonanceRaw);
        return getEffectiveSpeedMax(resonanceRaw) * profile.pursuitSpeedMultiplier();
    }

    /**
     * 获取带共鸣映射的攻击冷却。
     * <p>
     * 攻相更偏追打（冷却更短），御相偏稳守（冷却更长），灵相居中。
     * </p>
     *
     * @param resonanceRaw 共鸣原始字符串（可为空/未知）
     * @return 映射后的攻击冷却 tick
     */
    public int getEffectiveAttackCooldown(@Nullable String resonanceRaw) {
        ResonanceMappedProfile profile = resolveResonanceProfile(resonanceRaw);
        long mapped = Math.round(attackCooldown * profile.attackCooldownMultiplier());
        return (int) Math.max(RESONANCE_MIN_ATTACK_COOLDOWN_TICKS, mapped);
    }

    /**
     * 获取护持耐久消耗倍率。
     * <p>
     * 倍率越低表示越“扛打”（同样护持动作消耗更少耐久），倍率越高表示更激进易损。
     * </p>
     *
     * @param resonanceRaw 共鸣原始字符串（可为空/未知）
     * @return 护持耐久消耗倍率
     */
    public double getGuardDurabilityCostMultiplier(@Nullable String resonanceRaw) {
        return resolveResonanceProfile(resonanceRaw).guardDurabilityCostMultiplier();
    }

    /**
     * 根据护持倍率计算实际耐久消耗。
     *
     * @param baseCost     基础消耗
     * @param resonanceRaw 共鸣原始字符串（可为空/未知）
     * @return 映射后耐久消耗（负值输入会回退为 0）
     */
    public double mapGuardDurabilityCost(double baseCost, @Nullable String resonanceRaw) {
        double normalized = Math.max(0.0D, baseCost);
        return normalized * getGuardDurabilityCostMultiplier(resonanceRaw);
    }

    /**
     * 获取召回稳定性分值。
     * <p>
     * 基线由当前转向能力与速度比值构成，再叠加三相修正。
     * 分值越高表示召回时更稳，更不容易出现“高速甩尾”手感。
     * </p>
     *
     * @param resonanceRaw 共鸣原始字符串（可为空/未知）
     * @return 召回稳定性分值（非负）
     */
    public double getRecallStabilityScore(@Nullable String resonanceRaw) {
        ResonanceMappedProfile profile = resolveResonanceProfile(resonanceRaw);
        double baseStability = Math.max(0.0D, turnRate)
            * Math.max(0.0D, getEffectiveSpeedBase())
            / Math.max(RECALL_STABILITY_SPEED_FLOOR, getEffectiveSpeedMax());
        return baseStability * profile.recallStabilityMultiplier();
    }

    private static ResonanceMappedProfile resolveResonanceProfile(@Nullable String resonanceRaw) {
        return FlyingSwordResonanceType.resolve(resonanceRaw)
            .map(type -> RESONANCE_PROFILE_MAP.getOrDefault(type, BASELINE_RESONANCE_PROFILE))
            .orElse(BASELINE_RESONANCE_PROFILE);
    }

    private static Map<FlyingSwordResonanceType, ResonanceMappedProfile> createResonanceProfileMap() {
        EnumMap<FlyingSwordResonanceType, ResonanceMappedProfile> map =
            new EnumMap<>(FlyingSwordResonanceType.class);

        // 攻（烈）：强化输出与追击节奏，但牺牲护持/召回稳定性。
        map.put(
            FlyingSwordResonanceType.OFFENSE,
            new ResonanceMappedProfile(
                OFFENSE_DAMAGE_MULTIPLIER,
                OFFENSE_SPEED_MULTIPLIER,
                OFFENSE_PURSUIT_SPEED_MULTIPLIER,
                OFFENSE_ATTACK_COOLDOWN_MULTIPLIER,
                OFFENSE_GUARD_DURABILITY_COST_MULTIPLIER,
                OFFENSE_RECALL_STABILITY_MULTIPLIER
            )
        );

        // 御（稳）：强化护持与召回稳定，牺牲一部分伤害与压制节奏。
        map.put(
            FlyingSwordResonanceType.DEFENSE,
            new ResonanceMappedProfile(
                DEFENSE_DAMAGE_MULTIPLIER,
                DEFENSE_SPEED_MULTIPLIER,
                DEFENSE_PURSUIT_SPEED_MULTIPLIER,
                DEFENSE_ATTACK_COOLDOWN_MULTIPLIER,
                DEFENSE_GUARD_DURABILITY_COST_MULTIPLIER,
                DEFENSE_RECALL_STABILITY_MULTIPLIER
            )
        );

        // 灵（巧）：偏机动与效率的中间档，兼顾追击与稳定。
        map.put(
            FlyingSwordResonanceType.SPIRIT,
            new ResonanceMappedProfile(
                SPIRIT_DAMAGE_MULTIPLIER,
                SPIRIT_SPEED_MULTIPLIER,
                SPIRIT_PURSUIT_SPEED_MULTIPLIER,
                SPIRIT_ATTACK_COOLDOWN_MULTIPLIER,
                SPIRIT_GUARD_DURABILITY_COST_MULTIPLIER,
                SPIRIT_RECALL_STABILITY_MULTIPLIER
            )
        );

        return map;
    }

    private record ResonanceMappedProfile(
        double damageMultiplier,
        double speedMultiplier,
        double pursuitSpeedMultiplier,
        double attackCooldownMultiplier,
        double guardDurabilityCostMultiplier,
        double recallStabilityMultiplier
    ) {}

    // ==================== 临时修正操作 ====================

    /**
     * 设置速度临时倍率。
     *
     * @param multiplier 倍率（1.0 为无修正）
     */
    public void setSpeedMultiplier(double multiplier) {
        this.speedMultiplier = Math.max(
            SwordGrowthTuning.MULTIPLIER_MIN,
            multiplier
        );
    }

    /**
     * 获取速度临时倍率。
     */
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * 设置伤害临时倍率。
     *
     * @param multiplier 倍率（1.0 为无修正）
     */
    public void setDamageMultiplier(double multiplier) {
        this.damageMultiplier = Math.max(
            SwordGrowthTuning.MULTIPLIER_MIN,
            multiplier
        );
    }

    /**
     * 获取伤害临时倍率。
     */
    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    /**
     * 重置所有临时修正。
     */
    public void resetMultipliers() {
        this.speedMultiplier = 1.0;
        this.damageMultiplier = 1.0;
    }

    // ==================== 耐久操作 ====================

    /**
     * 消耗耐久。
     *
     * @param amount 消耗量
     * @return 是否耐久耗尽
     */
    public boolean consumeDurability(double amount) {
        if (amount <= 0) {
            return false;
        }
        this.durability = Math.max(0, this.durability - amount);
        return this.durability <= 0;
    }

    /**
     * 恢复耐久。
     *
     * @param amount 恢复量
     */
    public void restoreDurability(double amount) {
        if (amount <= 0) {
            return;
        }
        this.durability = Math.min(
            this.maxDurability,
            this.durability + amount
        );
    }

    /**
     * 完全恢复耐久。
     */
    public void fullRestoreDurability() {
        this.durability = this.maxDurability;
    }

    /**
     * 获取耐久百分比。
     *
     * @return 0.0 ~ 1.0
     */
    public double getDurabilityPercent() {
        if (maxDurability <= 0) {
            return 0.0;
        }
        return Math.min(1.0, durability / maxDurability);
    }

    /**
     * 检查耐久是否耗尽。
     */
    public boolean isDurabilityDepleted() {
        return durability <= 0;
    }

    // ==================== 序列化 ====================

    /**
     * 序列化为 NBT。
     *
     * @return NBT 标签
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();

        stableSwordId = normalizeStableSwordId(stableSwordId);

        // 成长数据
        tag.put(TAG_GROWTH_DATA, growthData.toNBT());

        // 当前耐久（需要单独保存，不由成长数据决定）
        tag.putDouble(TAG_DURABILITY, durability);

        // 属性值（通常由成长数据计算得出，但也保存以便快速加载）
        tag.putDouble(TAG_SPEED_BASE, speedBase);
        tag.putDouble(TAG_SPEED_MAX, speedMax);
        tag.putDouble(TAG_ACCEL, accel);
        tag.putDouble(TAG_TURN_RATE, turnRate);
        tag.putDouble(TAG_DAMAGE, damage);
        tag.putDouble(TAG_MAX_DURABILITY, maxDurability);
        tag.putInt(TAG_ATTACK_COOLDOWN, attackCooldown);

        // 剑灵数据
        tag.put(TAG_SPIRIT_DATA, spiritData.toNBT());

        tag.putString(TAG_STABLE_SWORD_ID, stableSwordId);
        tag.put(TAG_BOND, bond.toNBT());

        if (!imprint.isEmpty()) {
            tag.put(TAG_IMPRINT, imprint.toNBT());
        }

        return tag;
    }

    /**
     * 从 NBT 反序列化。
     *
     * @param tag NBT 标签
     * @return 属性实例
     */
    public static FlyingSwordAttributes fromNBT(@Nullable CompoundTag tag) {
        if (tag == null) {
            return new FlyingSwordAttributes();
        }

        // 读取成长数据
        SwordGrowthData growthData = null;
        if (tag.contains(TAG_GROWTH_DATA)) {
            growthData = SwordGrowthData.fromNBT(
                tag.getCompound(TAG_GROWTH_DATA)
            );
        }

        FlyingSwordAttributes attrs = new FlyingSwordAttributes(growthData);

        if (tag.contains(TAG_STABLE_SWORD_ID, net.minecraft.nbt.Tag.TAG_STRING)) {
            attrs.stableSwordId = normalizeStableSwordId(
                tag.getString(TAG_STABLE_SWORD_ID)
            );
        }

        if (tag.contains(TAG_BOND, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            attrs.bond.copyFrom(BenmingSwordBond.fromNBT(tag.getCompound(TAG_BOND)));
        } else {
            attrs.bond.resetToUnbound();
        }

        // 读取当前耐久
        if (tag.contains(TAG_DURABILITY)) {
            attrs.durability = tag.getDouble(TAG_DURABILITY);
        }

        // 读取剑灵数据
        if (tag.contains(TAG_SPIRIT_DATA)) {
            SwordSpiritData loaded = SwordSpiritData.fromNBT(
                tag.getCompound(TAG_SPIRIT_DATA)
            );
            attrs.spiritData.setAffinity(loaded.getAffinity());
            attrs.spiritData.setMood(loaded.getMood());
        }
 
        if (tag.contains(TAG_IMPRINT, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            var imprintTag = tag.getCompound(TAG_IMPRINT);
            attrs.imprint.setMarks(
                com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprint
                    .fromNBT(imprintTag)
                    .getMarks()
            );
        }

        return attrs;
    }

    /**
     * 从 NBT 读取到当前实例（用于实体的 readAdditionalSaveData）。
     *
     * @param tag NBT 标签
     */
    public void readFromNBT(@Nullable CompoundTag tag) {
        if (tag == null) {
            return;
        }

        if (tag.contains(TAG_STABLE_SWORD_ID, net.minecraft.nbt.Tag.TAG_STRING)) {
            this.stableSwordId = normalizeStableSwordId(
                tag.getString(TAG_STABLE_SWORD_ID)
            );
        } else {
            this.stableSwordId = normalizeStableSwordId(this.stableSwordId);
        }

        if (tag.contains(TAG_BOND, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            this.bond.copyFrom(BenmingSwordBond.fromNBT(tag.getCompound(TAG_BOND)));
        } else {
            this.bond.resetToUnbound();
        }

        // 读取成长数据
        if (tag.contains(TAG_GROWTH_DATA)) {
            SwordGrowthData loaded = SwordGrowthData.fromNBT(
                tag.getCompound(TAG_GROWTH_DATA)
            );
            // 复制数据到内部 growthData
            growthData.setQualityRaw(loaded.getQuality());
            growthData.setLevelRaw(loaded.getLevel());
            growthData.setExperienceRaw(loaded.getExperience());
        }

        // 重算属性
        recalculateFromGrowth();

        // 读取当前耐久
        if (tag.contains(TAG_DURABILITY)) {
            this.durability = tag.getDouble(TAG_DURABILITY);
        }

        // 读取剑灵数据
        if (tag.contains(TAG_SPIRIT_DATA)) {
            SwordSpiritData loaded = SwordSpiritData.fromNBT(
                tag.getCompound(TAG_SPIRIT_DATA)
            );
            spiritData.setAffinity(loaded.getAffinity());
            spiritData.setMood(loaded.getMood());
        }

        if (tag.contains(TAG_IMPRINT, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            var imprintTag = tag.getCompound(TAG_IMPRINT);
            imprint.setMarks(
                com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprint
                    .fromNBT(imprintTag)
                    .getMarks()
            );
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 创建深拷贝。
     */
    public FlyingSwordAttributes copy() {
        return fromNBT(toNBT());
    }

    private static String normalizeStableSwordId(@Nullable String id) {
        if (id == null || id.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return id;
    }

    public static class BenmingSwordBond {

        private static final String TAG_OWNER_UUID = "ownerUuid";
        private static final String TAG_RESONANCE = "resonance";

        private String ownerUuid = "";
        private double resonance = 0.0;

        public String getOwnerUuid() {
            return ownerUuid;
        }

        public void setOwnerUuid(@Nullable String ownerUuid) {
            this.ownerUuid = ownerUuid == null ? "" : ownerUuid;
        }

        public double getResonance() {
            return resonance;
        }

        public void setResonance(double resonance) {
            this.resonance = resonance;
        }

        public void resetToUnbound() {
            this.ownerUuid = "";
            this.resonance = 0.0;
        }

        public void copyFrom(@Nullable BenmingSwordBond other) {
            if (other == null) {
                resetToUnbound();
                return;
            }
            this.ownerUuid = other.ownerUuid;
            this.resonance = other.resonance;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString(TAG_OWNER_UUID, ownerUuid);
            tag.putDouble(TAG_RESONANCE, resonance);
            return tag;
        }

        public static BenmingSwordBond fromNBT(@Nullable CompoundTag tag) {
            BenmingSwordBond data = new BenmingSwordBond();
            if (tag == null) {
                return data;
            }
            if (tag.contains(TAG_OWNER_UUID, net.minecraft.nbt.Tag.TAG_STRING)) {
                data.ownerUuid = tag.getString(TAG_OWNER_UUID);
            }
            if (tag.contains(TAG_RESONANCE)) {
                data.resonance = tag.getDouble(TAG_RESONANCE);
            }
            return data;
        }
    }

    /**
     * 获取属性摘要字符串。
     */
    public String getSummary() {
        return String.format(
            "%s Lv%d | 伤害:%.1f 速度:%.2f 耐久:%.0f/%.0f | 战力:%s",
            growthData.getQuality().getDisplayName(),
            growthData.getLevel(),
            getEffectiveDamage(),
            getEffectiveSpeedMax(),
            durability,
            maxDurability,
            SwordStatCalculator.formatPowerRating(
                growthData.getCurrentPowerRating()
            )
        );
    }

    @Override
    public String toString() {
        return String.format(
            "FlyingSwordAttributes{%s, speedMax=%.3f, damage=%.2f, dura=%.0f/%.0f}",
            growthData,
            speedMax,
            damage,
            durability,
            maxDurability
        );
    }
}
