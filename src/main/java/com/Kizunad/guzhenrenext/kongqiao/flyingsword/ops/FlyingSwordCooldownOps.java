package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordCooldownAttachment;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;

/**
 * 飞剑冷却操作助手（存储在持有者附件）。
 * <p>
 * 用途：按“飞剑 UUID + 域”组合存取冷却，避免不同飞剑互相污染。
 * </p>
 */
public final class FlyingSwordCooldownOps {

    private static final String KEY_PREFIX = "guzhenrenext:flying_sword/";
    public static final String DOMAIN_ATTACK = "attack";
    public static final String DOMAIN_BLOCK_BREAK = "block_break";

    private FlyingSwordCooldownOps() {}

    public static int getAttackCooldown(FlyingSwordEntity sword) {
        return get(sword, DOMAIN_ATTACK);
    }

    public static boolean setAttackCooldown(FlyingSwordEntity sword, int ticks) {
        return set(sword, DOMAIN_ATTACK, ticks);
    }

    public static boolean isAttackReady(FlyingSwordEntity sword) {
        return getAttackCooldown(sword) <= 0;
    }

    public static int tickDownAttackCooldown(FlyingSwordEntity sword) {
        return tickDown(sword, DOMAIN_ATTACK);
    }

    public static int get(FlyingSwordEntity sword, String domain) {
        FlyingSwordCooldownAttachment att = getAttachment(sword);
        if (att == null) {
            return 0;
        }
        return att.get(makeKey(sword.getUUID(), domain));
    }

    public static boolean set(FlyingSwordEntity sword, String domain, int ticks) {
        FlyingSwordCooldownAttachment att = getAttachment(sword);
        if (att == null) {
            return false;
        }
        att.set(makeKey(sword.getUUID(), domain), Math.max(0, ticks));
        return true;
    }

    public static int tickDown(FlyingSwordEntity sword, String domain) {
        FlyingSwordCooldownAttachment att = getAttachment(sword);
        if (att == null) {
            return 0;
        }
        return att.tickDown(makeKey(sword.getUUID(), domain));
    }

    private static String makeKey(UUID swordUuid, String domain) {
        String suffix = domain == null ? "" : domain;
        return KEY_PREFIX + swordUuid + "/" + suffix;
    }

    private static FlyingSwordCooldownAttachment getAttachment(FlyingSwordEntity sword) {
        if (sword == null) {
            return null;
        }
        LivingEntity owner = sword.getOwner();
        if (owner == null) {
            return null;
        }
        return KongqiaoAttachments.getFlyingSwordCooldowns(owner);
    }
}
