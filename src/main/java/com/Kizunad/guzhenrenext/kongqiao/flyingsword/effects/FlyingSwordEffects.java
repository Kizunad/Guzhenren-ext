package com.Kizunad.guzhenrenext.kongqiao.flyingsword.effects;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 飞剑特效工具类。
 * <p>
 * 提供飞剑系统的视觉和音效反馈：
 * <ul>
 *     <li>攻击命中特效</li>
 *     <li>击杀特效</li>
 *     <li>升级特效</li>
 *     <li>突破特效</li>
 * </ul>
 * </p>
 */
public final class FlyingSwordEffects {

    // ===== 攻击特效参数 =====

    /** 攻击命中粒子数量。 */
    private static final int HIT_PARTICLE_COUNT = 8;

    /** 攻击命中粒子扩散范围。 */
    private static final double HIT_PARTICLE_SPREAD = 0.3;

    /** 攻击命中粒子速度。 */
    private static final double HIT_PARTICLE_SPEED = 0.1;

    /** 击杀粒子数量。 */
    private static final int KILL_PARTICLE_COUNT = 20;

    /** 击杀粒子扩散范围。 */
    private static final double KILL_PARTICLE_SPREAD = 0.5;

    /** 暴击粒子数量。 */
    private static final int CRIT_PARTICLE_COUNT = 12;

    // ===== 升级特效参数 =====

    /** 升级粒子数量（每级）。 */
    private static final int LEVEL_UP_PARTICLE_PER_LEVEL = 5;

    /** 升级粒子最大数量。 */
    private static final int LEVEL_UP_PARTICLE_MAX = 30;

    /** 升级粒子扩散范围。 */
    private static final double LEVEL_UP_PARTICLE_SPREAD = 0.4;

    /** 升级粒子速度。 */
    private static final double LEVEL_UP_PARTICLE_SPEED = 0.15;

    // ===== 突破特效参数 =====

    /** 突破粒子数量。 */
    private static final int BREAKTHROUGH_PARTICLE_COUNT = 50;

    /** 突破粒子扩散范围。 */
    private static final double BREAKTHROUGH_PARTICLE_SPREAD = 0.6;

    /** 突破粒子速度。 */
    private static final double BREAKTHROUGH_PARTICLE_SPEED = 0.2;

    /** 突破光柱粒子数量。 */
    private static final int BREAKTHROUGH_PILLAR_PARTICLE_COUNT = 30;

    /** 突破光柱高度。 */
    private static final double BREAKTHROUGH_PILLAR_HEIGHT = 3.0;

    /** 光柱粒子扩散范围。 */
    private static final double PILLAR_PARTICLE_SPREAD = 0.1;

    /** 光柱粒子速度。 */
    private static final double PILLAR_PARTICLE_SPEED = 0.02;

    /** 烟花粒子扩散范围。 */
    private static final double FIREWORK_PARTICLE_SPREAD = 0.15;

    /** 烟花粒子速度。 */
    private static final double FIREWORK_PARTICLE_SPEED = 0.01;

    /** 召回/模式切换粒子速度。 */
    private static final double MISC_PARTICLE_SPEED = 0.05;

    // ===== 音量参数 =====

    /** 攻击音效音量。 */
    private static final float HIT_SOUND_VOLUME = 0.6F;

    /** 击杀音效音量。 */
    private static final float KILL_SOUND_VOLUME = 0.8F;

    /** 升级音效音量。 */
    private static final float LEVEL_UP_SOUND_VOLUME = 1.0F;

    /** 突破音效音量。 */
    private static final float BREAKTHROUGH_SOUND_VOLUME = 1.2F;

    /** 高音调。 */
    private static final float HIGH_PITCH = 1.2F;

    /** 雷鸣音调。 */
    private static final float THUNDER_PITCH = 1.5F;

    /** 音效音调随机范围。 */
    private static final float PITCH_VARIANCE = 0.2F;

    /** 基础音调。 */
    private static final float BASE_PITCH = 1.0F;

    /** 粒子高度因子。 */
    private static final double PARTICLE_HEIGHT_FACTOR = 0.5;

    /** 音调偏移因子。 */
    private static final float PITCH_OFFSET_FACTOR = 0.5F;

    /** 等级音调缩放因子。 */
    private static final float LEVEL_PITCH_SCALE = 100.0F;

    /** 等级音调增量。 */
    private static final float LEVEL_PITCH_INCREMENT = 0.5F;

    /** 最大音调。 */
    private static final float MAX_PITCH = 2.0F;

    private FlyingSwordEffects() {}

    // ===== 攻击特效 =====

    /**
     * 播放攻击命中特效。
     *
     * @param sword  飞剑实体
     * @param target 被攻击目标
     * @param damage 造成的伤害
     */
    public static void playHitEffect(
        FlyingSwordEntity sword,
        LivingEntity target,
        float damage
    ) {
        if (!(sword.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 hitPos = target
            .position()
            .add(0, target.getBbHeight() * PARTICLE_HEIGHT_FACTOR, 0);

        // 命中粒子：剑气斩击效果
        level.sendParticles(
            ParticleTypes.SWEEP_ATTACK,
            hitPos.x,
            hitPos.y,
            hitPos.z,
            1,
            0,
            0,
            0,
            0
        );

        // 暴击粒子
        level.sendParticles(
            ParticleTypes.CRIT,
            hitPos.x,
            hitPos.y,
            hitPos.z,
            HIT_PARTICLE_COUNT,
            HIT_PARTICLE_SPREAD,
            HIT_PARTICLE_SPREAD,
            HIT_PARTICLE_SPREAD,
            HIT_PARTICLE_SPEED
        );

        // 伤害指示粒子
        level.sendParticles(
            ParticleTypes.DAMAGE_INDICATOR,
            hitPos.x,
            hitPos.y,
            hitPos.z,
            (int) Math.min(damage / 2, CRIT_PARTICLE_COUNT),
            HIT_PARTICLE_SPREAD,
            HIT_PARTICLE_SPREAD,
            HIT_PARTICLE_SPREAD,
            0.0
        );

        // 命中音效
        float pitch =
            BASE_PITCH +
            (level.random.nextFloat() - PITCH_OFFSET_FACTOR) * PITCH_VARIANCE;
        level.playSound(
            null,
            sword.getX(),
            sword.getY(),
            sword.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP,
            SoundSource.PLAYERS,
            HIT_SOUND_VOLUME,
            pitch
        );
    }

    /**
     * 播放击杀特效。
     *
     * @param sword  飞剑实体
     * @param target 被击杀目标
     */
    public static void playKillEffect(
        FlyingSwordEntity sword,
        LivingEntity target
    ) {
        if (!(sword.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 killPos = target
            .position()
            .add(0, target.getBbHeight() * PARTICLE_HEIGHT_FACTOR, 0);

        // 击杀爆发粒子
        level.sendParticles(
            ParticleTypes.EXPLOSION,
            killPos.x,
            killPos.y,
            killPos.z,
            1,
            0,
            0,
            0,
            0
        );

        // 灵魂粒子（剑道特色）
        level.sendParticles(
            ParticleTypes.SOUL,
            killPos.x,
            killPos.y,
            killPos.z,
            KILL_PARTICLE_COUNT,
            KILL_PARTICLE_SPREAD,
            KILL_PARTICLE_SPREAD,
            KILL_PARTICLE_SPREAD,
            HIT_PARTICLE_SPEED
        );

        // 附魔光辉粒子
        level.sendParticles(
            ParticleTypes.ENCHANT,
            killPos.x,
            killPos.y,
            killPos.z,
            KILL_PARTICLE_COUNT,
            KILL_PARTICLE_SPREAD,
            KILL_PARTICLE_SPREAD,
            KILL_PARTICLE_SPREAD,
            HIT_PARTICLE_SPEED
        );

        // 击杀音效
        level.playSound(
            null,
            sword.getX(),
            sword.getY(),
            sword.getZ(),
            SoundEvents.PLAYER_ATTACK_CRIT,
            SoundSource.PLAYERS,
            KILL_SOUND_VOLUME,
            BASE_PITCH
        );
    }

    // ===== 升级特效 =====

    /**
     * 播放升级特效。
     *
     * @param sword        飞剑实体
     * @param levelsGained 升级数量
     * @param newLevel     新等级
     */
    public static void playLevelUpEffect(
        FlyingSwordEntity sword,
        int levelsGained,
        int newLevel
    ) {
        if (!(sword.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 swordPos = sword.position();

        // 根据升级数量调整粒子数量
        int particleCount = Math.min(
            levelsGained * LEVEL_UP_PARTICLE_PER_LEVEL,
            LEVEL_UP_PARTICLE_MAX
        );

        // 经验获取粒子（绿色）
        level.sendParticles(
            ParticleTypes.HAPPY_VILLAGER,
            swordPos.x,
            swordPos.y,
            swordPos.z,
            particleCount,
            LEVEL_UP_PARTICLE_SPREAD,
            LEVEL_UP_PARTICLE_SPREAD,
            LEVEL_UP_PARTICLE_SPREAD,
            LEVEL_UP_PARTICLE_SPEED
        );

        // 附魔光辉粒子
        level.sendParticles(
            ParticleTypes.ENCHANT,
            swordPos.x,
            swordPos.y,
            swordPos.z,
            particleCount,
            LEVEL_UP_PARTICLE_SPREAD,
            LEVEL_UP_PARTICLE_SPREAD,
            LEVEL_UP_PARTICLE_SPREAD,
            LEVEL_UP_PARTICLE_SPEED
        );

        // 升级音效
        float pitch =
            BASE_PITCH + (newLevel / LEVEL_PITCH_SCALE) * LEVEL_PITCH_INCREMENT; // 等级越高音调越高
        level.playSound(
            null,
            sword.getX(),
            sword.getY(),
            sword.getZ(),
            SoundEvents.PLAYER_LEVELUP,
            SoundSource.PLAYERS,
            LEVEL_UP_SOUND_VOLUME,
            Math.min(pitch, MAX_PITCH)
        );

        // 经验球音效
        level.playSound(
            null,
            sword.getX(),
            sword.getY(),
            sword.getZ(),
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            SoundSource.PLAYERS,
            HIT_SOUND_VOLUME,
            BASE_PITCH + level.random.nextFloat() * PITCH_VARIANCE
        );
    }

    // ===== 突破特效 =====

    /**
     * 播放突破特效。
     *
     * @param sword      飞剑实体
     * @param oldQuality 旧品质
     * @param newQuality 新品质
     */
    public static void playBreakthroughEffect(
        FlyingSwordEntity sword,
        SwordQuality oldQuality,
        SwordQuality newQuality
    ) {
        if (!(sword.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 swordPos = sword.position();

        // 突破爆发粒子
        level.sendParticles(
            ParticleTypes.FLASH,
            swordPos.x,
            swordPos.y,
            swordPos.z,
            1,
            0,
            0,
            0,
            0
        );

        // 龙息粒子（突破气势）
        level.sendParticles(
            ParticleTypes.DRAGON_BREATH,
            swordPos.x,
            swordPos.y,
            swordPos.z,
            BREAKTHROUGH_PARTICLE_COUNT,
            BREAKTHROUGH_PARTICLE_SPREAD,
            BREAKTHROUGH_PARTICLE_SPREAD,
            BREAKTHROUGH_PARTICLE_SPREAD,
            BREAKTHROUGH_PARTICLE_SPEED
        );

        // 末影粒子（神秘感）
        level.sendParticles(
            ParticleTypes.PORTAL,
            swordPos.x,
            swordPos.y,
            swordPos.z,
            BREAKTHROUGH_PARTICLE_COUNT,
            BREAKTHROUGH_PARTICLE_SPREAD,
            BREAKTHROUGH_PARTICLE_SPREAD,
            BREAKTHROUGH_PARTICLE_SPREAD,
            BREAKTHROUGH_PARTICLE_SPEED
        );

        // 光柱效果
        spawnBreakthroughPillar(level, swordPos);

        // 突破音效组合
        // 1. 雷鸣声（气势）
        level.playSound(
            null,
            sword.getX(),
            sword.getY(),
            sword.getZ(),
            SoundEvents.LIGHTNING_BOLT_THUNDER,
            SoundSource.PLAYERS,
            BREAKTHROUGH_SOUND_VOLUME,
            THUNDER_PITCH
        );

        // 2. 信标激活音效
        level.playSound(
            null,
            sword.getX(),
            sword.getY(),
            sword.getZ(),
            SoundEvents.BEACON_ACTIVATE,
            SoundSource.PLAYERS,
            BREAKTHROUGH_SOUND_VOLUME,
            BASE_PITCH
        );

        // 3. 龙吼（高品质突破）
        if (newQuality.getTier() >= SwordQuality.KING.getTier()) {
            level.playSound(
                null,
                sword.getX(),
                sword.getY(),
                sword.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.PLAYERS,
                HIT_SOUND_VOLUME,
                THUNDER_PITCH
            );
        }
    }

    /**
     * 生成突破光柱粒子。
     */
    private static void spawnBreakthroughPillar(
        ServerLevel level,
        Vec3 basePos
    ) {
        // 从底部向上生成光柱
        for (int i = 0; i < BREAKTHROUGH_PILLAR_PARTICLE_COUNT; i++) {
            double height =
                ((double) i / BREAKTHROUGH_PILLAR_PARTICLE_COUNT) *
                BREAKTHROUGH_PILLAR_HEIGHT;

            level.sendParticles(
                ParticleTypes.END_ROD,
                basePos.x,
                basePos.y + height,
                basePos.z,
                1,
                PILLAR_PARTICLE_SPREAD,
                0,
                PILLAR_PARTICLE_SPREAD,
                PILLAR_PARTICLE_SPEED
            );

            level.sendParticles(
                ParticleTypes.FIREWORK,
                basePos.x,
                basePos.y + height,
                basePos.z,
                1,
                FIREWORK_PARTICLE_SPREAD,
                0,
                FIREWORK_PARTICLE_SPREAD,
                FIREWORK_PARTICLE_SPEED
            );
        }
    }

    // ===== 其他特效 =====

    /**
     * 播放飞剑生成特效。
     *
     * @param sword 飞剑实体
     */
    public static void playSpawnEffect(FlyingSwordEntity sword) {
        if (!(sword.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 swordPos = sword.position();

        // 生成粒子
        level.sendParticles(
            ParticleTypes.END_ROD,
            swordPos.x,
            swordPos.y,
            swordPos.z,
            KILL_PARTICLE_COUNT,
            LEVEL_UP_PARTICLE_SPREAD,
            LEVEL_UP_PARTICLE_SPREAD,
            LEVEL_UP_PARTICLE_SPREAD,
            LEVEL_UP_PARTICLE_SPEED
        );

        // 生成音效
        level.playSound(
            null,
            sword.getX(),
            sword.getY(),
            sword.getZ(),
            SoundEvents.ITEM_PICKUP,
            SoundSource.PLAYERS,
            HIT_SOUND_VOLUME,
            HIGH_PITCH
        );
    }

    /**
     * 播放飞剑召回特效。
     *
     * @param sword 飞剑实体
     * @param owner 主人
     */
    public static void playRecallEffect(FlyingSwordEntity sword, Player owner) {
        if (!(sword.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 swordPos = sword.position();

        // 召回粒子（向主人方向）
        level.sendParticles(
            ParticleTypes.ENCHANT,
            swordPos.x,
            swordPos.y,
            swordPos.z,
            HIT_PARTICLE_COUNT,
            HIT_PARTICLE_SPREAD,
            HIT_PARTICLE_SPREAD,
            HIT_PARTICLE_SPREAD,
            MISC_PARTICLE_SPEED
        );

        // 召回音效
        level.playSound(
            null,
            sword.getX(),
            sword.getY(),
            sword.getZ(),
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.PLAYERS,
            HIT_SOUND_VOLUME,
            THUNDER_PITCH
        );
    }

    /**
     * 播放飞剑模式切换特效。
     *
     * @param sword 飞剑实体
     */
    public static void playModeSwitchEffect(FlyingSwordEntity sword) {
        if (!(sword.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 swordPos = sword.position();

        // 模式切换粒子
        level.sendParticles(
            ParticleTypes.INSTANT_EFFECT,
            swordPos.x,
            swordPos.y,
            swordPos.z,
            HIT_PARTICLE_COUNT,
            HIT_PARTICLE_SPREAD,
            HIT_PARTICLE_SPREAD,
            HIT_PARTICLE_SPREAD,
            MISC_PARTICLE_SPEED
        );

        // 模式切换音效
        level.playSound(
            null,
            sword.getX(),
            sword.getY(),
            sword.getZ(),
            SoundEvents.UI_BUTTON_CLICK.value(),
            SoundSource.PLAYERS,
            HIT_SOUND_VOLUME,
            HIGH_PITCH
        );
    }
}
