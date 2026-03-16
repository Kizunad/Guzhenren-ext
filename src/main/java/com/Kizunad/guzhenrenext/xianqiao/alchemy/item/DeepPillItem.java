package com.Kizunad.guzhenrenext.xianqiao.alchemy.item;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.effect.DeepPillEffectState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;
import java.util.Objects;

public class DeepPillItem extends Item {

    private static final int CONSUME_ONE = 1;
    private static final float SHOCKWAVE_BASE_DAMAGE = 6.0F;
    private static final double SHOCKWAVE_RADIUS = 6.0D;
    private static final float FULL_HEALTH_PERCENT = 1.0F;
    private static final float BODY_RESHAPE_HEAL = 2.0F;
    private static final float USE_SOUND_VOLUME = 0.7F;
    private static final float USE_SOUND_PITCH = 1.1F;
    private static final int USE_COOLDOWN_TICKS = 20;
    private static final String APPLY_FAILED_MESSAGE = "当前上下文无法触发该深度丹药效果";
    private static final String BRIDGE_UNAVAILABLE_MESSAGE = "跨模组变量总线不可用，深度丹药效果未生效";

    private final Mechanism mechanism;

    public DeepPillItem(Properties properties, Mechanism mechanism) {
        super(properties);
        this.mechanism = Objects.requireNonNull(mechanism);
    }

    public Mechanism getMechanism() {
        return mechanism;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof ServerPlayer player)) {
            return result;
        }
        applyMechanism(player, stack, level);
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        TooltipContext context,
        List<Component> tooltipComponents,
        TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, flag);
        tooltipComponents.add(
            Component.literal("机制: " + mechanism.tooltip()).withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    private void applyMechanism(ServerPlayer player, ItemStack stack, Level level) {
        long gameTime = level.getGameTime();
        boolean applied;
        try {
            applied = switch (mechanism) {
                case LIFE_DEATH_REBIRTH -> {
                    DeepPillEffectState.grantNearDeathToken(player);
                    yield true;
                }
                case FORCED_BREAKTHROUGH -> DeepPillEffectState.applyForceBreakthrough(player);
                case MARROW_REFORGE -> DeepPillEffectState.reshuffleDaoMarks(player);
                case TIME_REVERSAL -> DeepPillEffectState.activateTimeReversal(player);
                case TRIBULATION_LURE -> DeepPillEffectState.triggerDampenedTribulation(player);
                case POWER_DISPERSE -> DeepPillEffectState.disperseCultivation(player, stack) > 0;
                case BEAST_BRAND -> {
                    DeepPillEffectState.activateBeastDomination(player, gameTime);
                    yield true;
                }
                case HEAVEN_SNATCH -> {
                    DeepPillEffectState.activateWorldSuppression(player, gameTime);
                    yield true;
                }
                case ENLIGHTENMENT_TEA -> {
                    DeepPillEffectState.activateEnlightenment(player, gameTime);
                    yield true;
                }
                case BODY_RESHAPE -> DeepPillEffectState.applyBodyReshape(player);
            };
        } catch (LinkageError linkageError) {
            applyLinkageFallback(player, stack, gameTime);
            player.displayClientMessage(Component.literal(BRIDGE_UNAVAILABLE_MESSAGE), true);
            return;
        }
        if (!applied) {
            player.displayClientMessage(
                Component.literal(APPLY_FAILED_MESSAGE),
                true
            );
            return;
        }
        player.playNotifySound(
            SoundEvents.AMETHYST_BLOCK_CHIME,
            SoundSource.PLAYERS,
            USE_SOUND_VOLUME,
            USE_SOUND_PITCH
        );
        player.getCooldowns().addCooldown(this, USE_COOLDOWN_TICKS);
        stack.shrink(CONSUME_ONE);
        if (mechanism == Mechanism.BODY_RESHAPE) {
            player.heal(BODY_RESHAPE_HEAL);
        }
    }

    private void applyLinkageFallback(ServerPlayer player, ItemStack stack, long gameTime) {
        switch (mechanism) {
            case FORCED_BREAKTHROUGH -> DeepPillEffectState.markForceBreakthroughUsed(player);
            case MARROW_REFORGE -> DeepPillEffectState.markDaoResetUsed(player);
            case POWER_DISPERSE -> DeepPillEffectState.recordPowerDisperseFallback(player, stack);
            case ENLIGHTENMENT_TEA -> DeepPillEffectState.markEnlightenmentWindow(player, gameTime);
            case BODY_RESHAPE -> DeepPillEffectState.markBodyReshapeUsed(player);
            default -> {
            }
        }
    }

    public enum Mechanism {
        LIFE_DEATH_REBIRTH("致死保命触发冲击波"),
        FORCED_BREAKTHROUGH("强制破境并永久扣减最大真元"),
        MARROW_REFORGE("道痕重洗并按仙窍环境重分配"),
        TIME_REVERSAL("仙窍时间流速拉满后进入虚弱"),
        TRIBULATION_LURE("提前触发灾劫并削弱后续窗口"),
        POWER_DISPERSE("散功返还道蕴碎片计数"),
        BEAST_BRAND("短时启用深度生灵强制驯服"),
        HEAVEN_SNATCH("短时冻结仙窍活动"),
        ENLIGHTENMENT_TEA("念头产出翻倍并提高容量"),
        BODY_RESHAPE("永久提高种族倍率标签");

        private final String tooltip;

        Mechanism(String tooltip) {
            this.tooltip = tooltip;
        }

        public String tooltip() {
            return tooltip;
        }
    }

    @EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class DeepPillHooks {

        private DeepPillHooks() {
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) {
                return;
            }
            float incoming = event.getNewDamage();
            if (incoming <= 0.0F) {
                return;
            }
            float effectiveHealth = player.getHealth() + player.getAbsorptionAmount();
            if (effectiveHealth - incoming > 0.0F) {
                return;
            }
            if (!DeepPillEffectState.consumeNearDeathToken(player)) {
                return;
            }
            event.setNewDamage(0.0F);
            player.setHealth(player.getMaxHealth() * FULL_HEALTH_PERCENT);
            player.removeAllEffects();
            emitShockwave(player);
        }

        private static void emitShockwave(ServerPlayer player) {
            AABB area = player.getBoundingBox().inflate(SHOCKWAVE_RADIUS);
            for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, area)) {
                if (target == player || !target.isAlive()) {
                    continue;
                }
                target.hurt(player.damageSources().playerAttack(player), SHOCKWAVE_BASE_DAMAGE);
            }
            player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.TOTEM_USE,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
            );
        }
    }

}
