package com.Kizunad.guzhenrenext.bastion.skill;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;

/**
 * 基地高转技能执行上下文。
 * <p>
 * 该上下文由 BastionHighTierSkillService 构建，并传递给实现了 {@link IBastionSkillEffect}
 * 的效果逻辑。
 * </p>
 */
public record BastionSkillContext(
    ServerLevel level,
    BastionData bastion,
    long gameTime,
    double bonusMultiplier,
    List<ServerPlayer> targets,
    List<Mob> guardians,
    Map<String, String> metadata,
    RandomSource random
) {
}
