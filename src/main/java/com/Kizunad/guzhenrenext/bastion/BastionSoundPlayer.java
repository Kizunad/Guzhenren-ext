package com.Kizunad.guzhenrenext.bastion;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * 基地音效播放工具类。
 * <p>
 * 提供基地相关音效的统一播放接口，支持回退到原版音效。
 * </p>
 */
public final class BastionSoundPlayer {

    private BastionSoundPlayer() {
        // 工具类
    }

    // ===== 音效配置常量 =====

    /** 默认音量。 */
    private static final float DEFAULT_VOLUME = 1.0f;

    /** 默认音调。 */
    private static final float DEFAULT_PITCH = 1.0f;

    /** 封印音效音调（略低）。 */
    private static final float SEAL_PITCH = 0.8f;

    /** 升级音效音调（略高）。 */
    private static final float EVOLVE_PITCH = 1.2f;

    /** 祭献音效音调（神秘感）。 */
    private static final float SACRIFICE_PITCH = 0.9f;

    /** 警报音效音量（更大）。 */
    private static final float ALARM_VOLUME = 1.5f;

    /** 音效播放范围（方块）。 */
    private static final float SOUND_RANGE = 32.0f;

    /** 方块中心偏移量。 */
    private static final double BLOCK_CENTER_OFFSET = 0.5;

    // ===== 交互音效 =====

    /**
     * 播放封印成功音效。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void playSeal(ServerLevel level, BlockPos pos) {
        playSound(level, pos, BastionSounds.BASTION_SEAL.get(),
            SoundEvents.ENCHANTMENT_TABLE_USE, // 回退音效
            DEFAULT_VOLUME, SEAL_PITCH);
    }

    /**
     * 播放占领成功音效。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void playCapture(ServerLevel level, BlockPos pos) {
        playSound(level, pos, BastionSounds.BASTION_CAPTURE.get(),
            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, // 回退音效
            DEFAULT_VOLUME, DEFAULT_PITCH);
    }

    /**
     * 播放攻击警报音效。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void playAlarm(ServerLevel level, BlockPos pos) {
        playSound(level, pos, BastionSounds.BASTION_ALARM.get(),
            SoundEvents.BELL_RESONATE, // 回退音效
            ALARM_VOLUME, DEFAULT_PITCH);
    }

    /**
     * 播放资源祭献音效。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void playSacrifice(ServerLevel level, BlockPos pos) {
        playSound(level, pos, BastionSounds.BASTION_SACRIFICE.get(),
            SoundEvents.EXPERIENCE_ORB_PICKUP, // 回退音效
            DEFAULT_VOLUME, SACRIFICE_PITCH);
    }

    // ===== 状态变化音效 =====

    /**
     * 播放升级音效。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void playEvolve(ServerLevel level, BlockPos pos) {
        playSound(level, pos, BastionSounds.BASTION_EVOLVE.get(),
            SoundEvents.PLAYER_LEVELUP, // 回退音效
            DEFAULT_VOLUME, EVOLVE_PITCH);
    }

    /**
     * 播放销毁音效。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void playDestroy(ServerLevel level, BlockPos pos) {
        playSound(level, pos, BastionSounds.BASTION_DESTROY.get(),
            SoundEvents.GENERIC_EXPLODE.value(), // 回退音效
            DEFAULT_VOLUME, DEFAULT_PITCH);
    }

    /**
     * 播放封印解除音效。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void playUnseal(ServerLevel level, BlockPos pos) {
        playSound(level, pos, BastionSounds.BASTION_UNSEAL.get(),
            SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), // 回退音效
            DEFAULT_VOLUME, DEFAULT_PITCH);
    }

    // ===== 节点音效 =====

    /**
     * 播放节点扩张音效。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void playNodeExpand(ServerLevel level, BlockPos pos) {
        playSound(level, pos, BastionSounds.NODE_EXPAND.get(),
            SoundEvents.SCULK_BLOCK_SPREAD, // 回退音效
            DEFAULT_VOLUME, DEFAULT_PITCH);
    }

    /**
     * 播放节点衰减音效。
     *
     * @param level 服务端世界
     * @param pos   位置
     */
    public static void playNodeDecay(ServerLevel level, BlockPos pos) {
        playSound(level, pos, BastionSounds.NODE_DECAY.get(),
            SoundEvents.SCULK_BLOCK_BREAK, // 回退音效
            DEFAULT_VOLUME, DEFAULT_PITCH);
    }

    // ===== 内部方法 =====

    /**
     * 播放音效，如果自定义音效未加载则使用回退音效。
     */
    private static void playSound(
            ServerLevel level,
            BlockPos pos,
            SoundEvent customSound,
            SoundEvent fallbackSound,
            float volume,
            float pitch) {
        // 尝试播放自定义音效，如果资源不存在会静默失败
        // 同时播放回退音效确保有反馈
        SoundEvent soundToPlay = customSound != null ? customSound : fallbackSound;

        level.playSound(
            null, // 所有玩家都能听到
            pos.getX() + BLOCK_CENTER_OFFSET,
            pos.getY() + BLOCK_CENTER_OFFSET,
            pos.getZ() + BLOCK_CENTER_OFFSET,
            soundToPlay,
            SoundSource.BLOCKS,
            volume,
            pitch
        );
    }
}
