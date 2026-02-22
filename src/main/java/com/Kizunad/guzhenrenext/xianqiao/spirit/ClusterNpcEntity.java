package com.Kizunad.guzhenrenext.xianqiao.spirit;

import com.Kizunad.guzhenrenext.config.CommonConfig;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkApi;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuData;
import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuItem;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.Kizunad.guzhenrenext.xianqiao.service.ApertureBoundaryService;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClusterNpcEntity extends Mob implements MenuProvider {

    /** NBT：集群数量字段键。 */
    private static final String KEY_CLUSTER_COUNT = "cluster_count";

    /** NBT：当前工作类型字段键。 */
    private static final String KEY_WORK_TYPE = "work_type";

    /** NBT：基础效率字段键。 */
    private static final String KEY_EFFICIENCY_BASE = "efficiency_base";

    /** NBT：生产缓存中的小数累积值字段键。 */
    private static final String KEY_PRODUCTION_BUFFER = "production_buffer";

    /** NBT：待消费产出总量字段键。 */
    private static final String KEY_PENDING_OUTPUT = "pending_output";

    /** NBT：生产步进计时器字段键。 */
    private static final String KEY_PRODUCTION_TICK_COUNTER = "production_tick_counter";

    /** NBT：种族字段键。 */
    private static final String KEY_RACE = "race";

    /** 种族常量：毛民。 */
    public static final String HAIRY_MAN = "hairy_man";

    /** 种族常量：石民。 */
    public static final String ROCKMAN = "rockman";

    /** 种族常量：小人。 */
    public static final String MINIMAN = "miniman";

    /** 种族常量：墨人。 */
    public static final String INKMAN = "inkman";

    /** 生产逻辑固定步进：每 20 tick 执行一次产出尝试。 */
    private static final int PRODUCTION_STEP_INTERVAL_TICKS = 20;

    /** 临时默认倍数：道痕加成（Task9 前占位，当前固定 1.0）。 */
    private static final double DEFAULT_DAO_MARK_MULTIPLIER = 1.0D;

    /** 临时默认倍数：时间流速加成（Task10 前占位，当前固定 1.0）。 */
    private static final double DEFAULT_TIME_SPEED_MULTIPLIER = 1.0D;

    /** 临时默认倍数：种族匹配加成（未匹配时固定 1.0）。 */
    private static final double DEFAULT_RACIAL_MULTIPLIER = 1.0D;

    /** 种族匹配时的生产倍率（Task4 需求固定 1.5）。 */
    private static final double RACIAL_MATCH_MULTIPLIER = 1.5D;

    /**
     * 种族倍率硬上限：用于兜底限制异常倍率，避免产出突增带来 overflow/TPS 风险。
     * 约束要求需大于等于 1.5，当前取 2.0 作为保守上限。
     */
    private static final double RACIAL_MULTIPLIER_CAP = 2.0D;

    /** 道痕值换算分母：公式中的 DaoMarks / 1000.0。 */
    private static final double DAO_MARK_DIVISOR = 1000.0D;

    /** 道痕换算系数：公式中的 * 0.1。 */
    private static final double DAO_MARK_MULTIPLIER_SCALE = 0.1D;

    /** 道痕倍率硬上限：用于抑制异常道痕导致的产出突增。 */
    private static final double DAO_MARK_MULTIPLIER_CAP = 5.0D;

    /** 仙窍边界缓冲值（方块）：用于提高边界附近归属判定的稳定性。 */
    private static final int APERTURE_BOUNDARY_BUFFER = 16;

    private static final double MAX_HEALTH = 20.0D;

    private static final double MOVE_SPEED = 0.2D;

    private static final double FOLLOW_RANGE = 12.0D;

    private static final double KNOCKBACK_RESISTANCE = 0.2D;

    // --- ContainerData Magic Numbers ---
    private static final int DATA_INDEX_PENDING_LOW = 0;
    private static final int DATA_INDEX_PENDING_HIGH = 1;
    private static final int DATA_INDEX_EFFICIENCY = 2;
    private static final int DATA_INDEX_WORK_TYPE = 3;
    private static final int DATA_COUNT = 4;
    private static final long MASK_LOW_32_BIT = 0xFFFFFFFFL;
    private static final int SHIFT_HIGH_32_BIT = 32;
    private static final int EFFICIENCY_SCALE = 100;

    /** 粒子生成概率分母（1/10）。 */
    private static final int PARTICLE_CHANCE = 10;

    /** 粒子位置偏移基数（居中）。 */
    private static final double PARTICLE_OFFSET_BASE = 0.5D;

    private static final EntityDataAccessor<Integer> DATA_CLUSTER_COUNT =
        SynchedEntityData.defineId(ClusterNpcEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<String> DATA_WORK_TYPE =
        SynchedEntityData.defineId(ClusterNpcEntity.class, EntityDataSerializers.STRING);

    private int clusterCount = 1;

    private String workType = "idle";

    private double efficiencyBase = 1.0D;

    /**
     * 生产缓冲区：用于保存每次生产后不足 1 的小数产出，避免精度损失。
     * 当累计值达到或超过 1 时，才会转化为可消费的整数产出。
     */
    private double productionBuffer = 0.0D;

    /**
     * 待消费产出总量。
     */
    private long pendingOutput = 0L;

    /**
     * 生产步进计时器。
     */
    private int productionTickCounter = 0;

    /**
     * 种族字段。
     * 仅用于当前阶段种族信息持久化与读取，后续任务将基于该字段追加加成逻辑。
     */
    private String race = MINIMAN;

    /**
     * NPC 背包：仅包含一个用于放置储物蛊的槽位。
     */
    private final SimpleContainer inventory = new SimpleContainer(1);

    /**
     * 数据同步对象，用于 Menu。
     */
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_INDEX_PENDING_LOW -> (int) (pendingOutput & MASK_LOW_32_BIT);
                case DATA_INDEX_PENDING_HIGH -> (int) (pendingOutput >>> SHIFT_HIGH_32_BIT);
                case DATA_INDEX_EFFICIENCY -> (int) (efficiencyBase * EFFICIENCY_SCALE);
                case DATA_INDEX_WORK_TYPE -> 0; // WorkType ordinal placeholder
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Server-side logic drives values, setter ignored for now
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ClusterNpcEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, MAX_HEALTH)
            .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
            .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
            .add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE);
    }

    @Override
    public Component getName() {
        return Component.literal(String.format("Farmer x%d [%s]",
            this.clusterCount,
            "idle".equals(this.workType) ? "Idle" : "Active"));
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(this, (buf) -> buf.writeInt(this.getId()));
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ClusterNpcMenu(containerId, playerInventory, this, this.data);
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public int getClusterCount() {
        return this.entityData.get(DATA_CLUSTER_COUNT);
    }

    public void setClusterCount(int clusterCount) {
        this.clusterCount = Math.max(1, clusterCount);
        this.entityData.set(DATA_CLUSTER_COUNT, this.clusterCount);
    }

    public String getWorkType() {
        return this.entityData.get(DATA_WORK_TYPE);
    }

    public void setWorkType(String workType) {
        this.workType = workType == null || workType.isBlank() ? "idle" : workType;
        this.entityData.set(DATA_WORK_TYPE, this.workType);
    }

    /**
     * 获取当前种族标识。
     *
     * @return 合法种族值（hairy_man/rockman/miniman/inkman）
     */
    public String getRace() {
        return race;
    }

    /**
     * 设置当前种族标识。
     * 对空值、空白值与非法值统一兜底为 miniman，保证后续逻辑读取稳定。
     *
     * @param raceInput 传入种族标识
     */
    public void setRace(String raceInput) {
        this.race = normalizeRace(raceInput);
    }

    /**
     * 将输入种族规范化为受支持集合。
     * <p>
     * 支持值：hairy_man / rockman / miniman / inkman。
     * 其余情况（null、空白、非法）均回退 miniman。
     * </p>
     *
     * @param raceInput 输入种族
     * @return 规范化后的种族
     */
    private String normalizeRace(String raceInput) {
        if (raceInput == null || raceInput.isBlank()) {
            return MINIMAN;
        }
        return switch (raceInput) {
            case HAIRY_MAN, ROCKMAN, MINIMAN, INKMAN -> raceInput;
            default -> MINIMAN;
        };
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_CLUSTER_COUNT.equals(key)) {
            this.clusterCount = this.entityData.get(DATA_CLUSTER_COUNT);
        } else if (DATA_WORK_TYPE.equals(key)) {
            this.workType = this.entityData.get(DATA_WORK_TYPE);
        }
    }

    public double getEfficiencyBase() {
        return efficiencyBase;
    }

    public void setEfficiencyBase(double efficiencyBase) {
        this.efficiencyBase = Math.max(0.0D, efficiencyBase);
    }

    /**
     * 获取待消费的整数产出总量。
     * 该值仅表示“已物化但尚未被外部系统消耗”的数量。
     *
     * @return 待消费产出总量（非负）
     */
    public long getPendingOutput() {
        return pendingOutput;
    }

    /**
     * 安全消费待产出数量：仅扣减 [0, pendingOutput] 范围内的值，避免出现负数。
     *
     * @param amount 请求消费数量
     * @return 实际消费数量
     */
    public long consumePendingOutput(long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        final long consumed = Math.min(amount, pendingOutput);
        pendingOutput -= consumed;
        return consumed;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            if (!"idle".equals(getWorkType())) {
                spawnWorkParticles();
            }
            return;
        }
        productionTickCounter++;
        if (productionTickCounter >= PRODUCTION_STEP_INTERVAL_TICKS) {
            productionTickCounter = 0;
            performProductionStep();
            pushOutputToStorageGu();
        }
    }

    /**
     * 在客户端生成工作粒子效果。
     * 当 workType 不为 idle 时，在实体周围生成微量的快乐村民粒子，表示正在工作。
     */
    private void spawnWorkParticles() {
        if (this.random.nextInt(PARTICLE_CHANCE) == 0) {
            double offsetX = (this.random.nextDouble() - PARTICLE_OFFSET_BASE) * this.getBbWidth();
            double offsetY = this.random.nextDouble() * this.getBbHeight();
            double offsetZ = (this.random.nextDouble() - PARTICLE_OFFSET_BASE) * this.getBbWidth();
            this.level().addParticle(
                ParticleTypes.HAPPY_VILLAGER,
                this.getX() + offsetX,
                this.getY() + offsetY,
                this.getZ() + offsetZ,
                0.0D, 0.0D, 0.0D
            );
        }
    }

    /**
     * 执行一次固定步进生产。
     * 公式：outputRaw = cluster_count * efficiency_base * dao_mark_multiplier
     *      * time_speed_multiplier * racial_multiplier
     * 当前阶段仅累积到 pendingOutput，不做槽位推送。
     */
    private void performProductionStep() {
        final double outputRaw = clusterCount
            * efficiencyBase
            * getDaoMarkMultiplier()
            * getTimeSpeedMultiplier()
            * getRacialProductionMultiplier();
        productionBuffer += outputRaw;
        final long materializedOutput = (long) Math.floor(productionBuffer);
        if (materializedOutput <= 0L) {
            return;
        }
        final long cap = CommonConfig.INSTANCE.clusterNpcPendingOutputHardCap.get();
        final long room = pendingOutput >= cap ? 0L : cap - pendingOutput;
        final long delta = Math.min(materializedOutput, room);
        pendingOutput += delta;
        productionBuffer -= delta;
    }

    /**
     * 计算当前生产链路的种族倍率。
     * <p>
     * Task4 采用“最小可验证映射”：当前代码/测试仅出现 farming 作为非 idle 的明确任务值，
     * 因此仅在 {@code workType=farming} 且 {@code race=miniman} 时判定为“种族匹配”，返回 1.5；
     * 其余场景返回 1.0。
     * </p>
     * <p>
     * 结果会被硬上限钳制到 {@code [1.0, RACIAL_MULTIPLIER_CAP]}，用于抑制异常倍率风险。
     * </p>
     *
     * @return 钳制后的种族倍率
     */
    private double getRacialProductionMultiplier() {
        final double rawMultiplier = isCurrentWorkTypeRaceMatched()
            ? RACIAL_MATCH_MULTIPLIER
            : DEFAULT_RACIAL_MULTIPLIER;
        return Math.min(Math.max(rawMultiplier, DEFAULT_RACIAL_MULTIPLIER), RACIAL_MULTIPLIER_CAP);
    }

    /**
     * 判断当前 workType 与 race 是否命中最小映射规则。
     * <p>
     * 保守策略：仅识别 farming；未来若引入更多 workType 再增量扩展映射，不影响当前默认行为。
     * </p>
     *
     * @return 命中映射返回 true，否则 false
     */
    private boolean isCurrentWorkTypeRaceMatched() {
        if ("farming".equals(workType)) {
            return MINIMAN.equals(race);
        }
        return false;
    }

    /**
     * 尝试将产出推送到储物蛊中。
     */
    private void pushOutputToStorageGu() {
        if (pendingOutput <= 0L) {
            return;
        }
        ItemStack stack = inventory.getItem(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof StorageGuItem)) {
            return;
        }

        StorageGuItem item = (StorageGuItem) stack.getItem();
        StorageGuData.StorageGuHandler handler = item.getStorageHandler(stack);

        long inserted = handler.insert(XianqiaoItems.HEAVENLY_FRAGMENT.getId(), pendingOutput);

        if (inserted > 0) {
            consumePendingOutput(inserted);
        }
    }

    /**
     * 计算当前生产道痕倍率。
     * <p>
     * Task7 约定公式：
     * {@code multiplier = 1.0 + (daoMarks / 1000.0) * 0.1}。
     * </p>
     * <p>
     * 倍率会被硬钳制到 {@code [1.0, 5.0]} 区间，避免异常数据导致产出链路失稳。
     * </p>
     * <p>
     * 上下文解析策略：
     * 1) 仅在服务端维度执行；
     * 2) 先通过边界命中解析当前 NPC 的归属 owner；
     * 3) 再通过 {@link ApertureWorldData} 验证 owner/aperture 上下文存在；
     * 4) 最后使用 {@link DaoMarkApi} 读取对应 {@link DaoType} 道痕值。
     * </p>
     * <p>
     * 任意异常或上下文缺失场景均回退 1.0，保证生产链路不中断。
     * </p>
     *
     * @return 道痕倍率（硬钳制到 [1.0, 5.0]）
     */
    private double getDaoMarkMultiplier() {
        try {
            if (!(level() instanceof ServerLevel serverLevel) || "idle".equals(workType)) {
                return DEFAULT_DAO_MARK_MULTIPLIER;
            }
            Player ownerPlayer = findApertureOwnerPlayer(serverLevel);
            if (ownerPlayer == null) {
                return DEFAULT_DAO_MARK_MULTIPLIER;
            }
            ApertureWorldData worldData = ApertureWorldData.get(serverLevel);
            ApertureInfo ownerAperture = worldData.getAperture(ownerPlayer.getUUID());
            if (ownerAperture == null) {
                return DEFAULT_DAO_MARK_MULTIPLIER;
            }

            DaoType daoType = resolveDaoTypeForWorkType();
            int daoMarkValue = DaoMarkApi.getAura(serverLevel, blockPosition(), daoType);
            double rawMultiplier = DEFAULT_DAO_MARK_MULTIPLIER
                + (daoMarkValue / DAO_MARK_DIVISOR) * DAO_MARK_MULTIPLIER_SCALE;
            return Math.min(
                Math.max(rawMultiplier, DEFAULT_DAO_MARK_MULTIPLIER),
                DAO_MARK_MULTIPLIER_CAP
            );
        } catch (RuntimeException exception) {
            return DEFAULT_DAO_MARK_MULTIPLIER;
        }
    }

    /**
     * 将当前 workType 映射到用于生产倍率的道类型。
     * <p>
     * 最小确定性映射规则：
     * - farming -> WOOD（作物生产）；
     * - alchemy/refining -> FIRE（炼丹/炼制）；
     * - 其他 -> WOOD（保守默认，避免空映射打断生产链）。
     * </p>
     *
     * @return 对应 DaoType
     */
    private DaoType resolveDaoTypeForWorkType() {
        final String normalizedWorkType = workType == null
            ? ""
            : workType.toLowerCase(Locale.ROOT);
        return switch (normalizedWorkType) {
            case "farming" -> DaoType.WOOD;
            case "alchemy", "refining" -> DaoType.FIRE;
            default -> DaoType.WOOD;
        };
    }

    /**
     * 读取当前集群 NPC 所在仙窍的时间流速倍率。
     * <p>
     * 该方法已接入真实数据链路：
     * 1) 仅在服务端维度解析；
     * 2) 先识别归属玩家；
     * 3) 再从归属仙窍读取 {@code timeSpeed}。
     * </p>
     * <p>
     * 任意不可解析场景（非服务端、owner 缺失、aperture 缺失）均回退 1.0，
     * 以保证生产公式在边界状态下仍保持稳定。
     * </p>
     *
     * @return 可解析时返回所在仙窍的 timeSpeed，失败时回退 1.0
     */
    private double getTimeSpeedMultiplier() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return DEFAULT_TIME_SPEED_MULTIPLIER;
        }
        Player ownerPlayer = findApertureOwnerPlayer(serverLevel);
        if (ownerPlayer == null) {
            return DEFAULT_TIME_SPEED_MULTIPLIER;
        }
        ApertureWorldData worldData = ApertureWorldData.get(serverLevel);
        ApertureInfo ownerAperture = worldData.getAperture(ownerPlayer.getUUID());
        if (ownerAperture == null) {
            return DEFAULT_TIME_SPEED_MULTIPLIER;
        }
        return ownerAperture.timeSpeed();
    }

    /**
     * 解析当前集群 NPC 所在仙窍的归属玩家。
     * <p>
     * 识别策略与资源控制器保持一致：
     * 1) 遍历当前维度在线玩家；
     * 2) 读取其仙窍边界；
     * 3) 先做严格边界命中，再做带缓冲的区块命中；
     * 4) 命中即判定为 owner。
     * </p>
     *
     * @param serverLevel 当前服务端维度
     * @return 命中的归属玩家，找不到时返回 null
     */
    @Nullable
    private Player findApertureOwnerPlayer(ServerLevel serverLevel) {
        ApertureWorldData worldData = ApertureWorldData.get(serverLevel);
        for (Player player : serverLevel.players()) {
            ApertureInfo info = worldData.getAperture(player.getUUID());
            if (info == null) {
                continue;
            }
            boolean insideAperture = ApertureBoundaryService.containsBlock(info, blockPosition());
            boolean insideBufferedAperture = ApertureBoundaryService.containsChunkWithBlockBuffer(
                info,
                SectionPos.blockToSectionCoord(blockPosition().getX()),
                SectionPos.blockToSectionCoord(blockPosition().getZ()),
                APERTURE_BOUNDARY_BUFFER
            );
            if (insideAperture || insideBufferedAperture) {
                return player;
            }
        }
        return null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CLUSTER_COUNT, 1);
        builder.define(DATA_WORK_TYPE, "idle");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(KEY_CLUSTER_COUNT, clusterCount);
        compound.putString(KEY_WORK_TYPE, workType);
        compound.putDouble(KEY_EFFICIENCY_BASE, efficiencyBase);
        compound.putDouble(KEY_PRODUCTION_BUFFER, productionBuffer);
        compound.putLong(KEY_PENDING_OUTPUT, pendingOutput);
        compound.putInt(KEY_PRODUCTION_TICK_COUNTER, productionTickCounter);
        compound.putString(KEY_RACE, race);
        compound.put("Inventory", inventory.createTag(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(KEY_CLUSTER_COUNT, Tag.TAG_INT)) {
            setClusterCount(compound.getInt(KEY_CLUSTER_COUNT));
        }
        if (compound.contains(KEY_WORK_TYPE, Tag.TAG_STRING)) {
            setWorkType(compound.getString(KEY_WORK_TYPE));
        }
        if (compound.contains(KEY_EFFICIENCY_BASE, Tag.TAG_DOUBLE)) {
            setEfficiencyBase(compound.getDouble(KEY_EFFICIENCY_BASE));
        }
        productionBuffer = 0.0D;
        if (compound.contains(KEY_PRODUCTION_BUFFER, Tag.TAG_DOUBLE)) {
            productionBuffer = Math.max(0.0D, compound.getDouble(KEY_PRODUCTION_BUFFER));
        }
        pendingOutput = 0L;
        if (compound.contains(KEY_PENDING_OUTPUT, Tag.TAG_LONG)) {
            pendingOutput = Math.max(0L, compound.getLong(KEY_PENDING_OUTPUT));
        }
        productionTickCounter = 0;
        if (compound.contains(KEY_PRODUCTION_TICK_COUNTER, Tag.TAG_INT)) {
            productionTickCounter = Math.max(0, compound.getInt(KEY_PRODUCTION_TICK_COUNTER));
            productionTickCounter = productionTickCounter % PRODUCTION_STEP_INTERVAL_TICKS;
        }
        if (compound.contains(KEY_RACE, Tag.TAG_STRING)) {
            setRace(compound.getString(KEY_RACE));
        } else {
            setRace(MINIMAN);
        }
        if (compound.contains("Inventory", Tag.TAG_LIST)) {
            inventory.fromTag(compound.getList("Inventory", Tag.TAG_COMPOUND), this.registryAccess());
        }
    }
}
