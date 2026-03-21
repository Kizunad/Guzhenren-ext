package com.Kizunad.guzhenrenext.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.client.effect.BackPngTimedEffects;
import com.Kizunad.guzhenrenext.client.gui.SkillWheelScreen;
import com.Kizunad.guzhenrenext.kongqiao.client.ui.TweakScreen;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState;
import com.Kizunad.guzhenrenext.network.PacketOpenNianTouGui;
import com.Kizunad.guzhenrenext.network.ServerboundBenmingSwordActionPayload;
import com.Kizunad.guzhenrenext.network.ServerboundFlyingSwordActionPayload;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
    modid = GuzhenrenExt.MODID,
    value = Dist.CLIENT,
    bus = EventBusSubscriber.Bus.GAME
)
public final class GuClientEvents {

    private GuClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        BackPngTimedEffects.tick();

        // ===== 念头/轮盘/调整面板 =====

        // 检查按键是否按下，并消耗点击（防止连续触发）
        while (GuKeyBindings.OPEN_NIANTOU_GUI.consumeClick()) {
            // 发送网络包请求打开 GUI
            PacketDistributor.sendToServer(new PacketOpenNianTouGui());
        }

        // 技能轮盘属于纯客户端 Screen：按住打开，松开由 Screen 内部确认。
        while (GuKeyBindings.OPEN_SKILL_WHEEL.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(
                    new SkillWheelScreen(GuKeyBindings.OPEN_SKILL_WHEEL)
                );
            }
        }

        // 调整面板属于纯客户端 Screen，但依赖服务端同步配置；由 Screen init() 内主动发起同步请求。
        while (GuKeyBindings.OPEN_TWEAK_UI.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new TweakScreen());
            }
        }

        // ===== 飞剑（Phase 2 最小输入） =====

        while (GuKeyBindings.FLYING_SWORD_SELECT_NEAREST.consumeClick()) {
            PacketDistributor.sendToServer(
                new ServerboundFlyingSwordActionPayload(
                    ServerboundFlyingSwordActionPayload.Action.SELECT_NEAREST
                )
            );
        }

        while (GuKeyBindings.FLYING_SWORD_CYCLE_MODE.consumeClick()) {
            PacketDistributor.sendToServer(
                new ServerboundFlyingSwordActionPayload(
                    ServerboundFlyingSwordActionPayload.Action.CYCLE_MODE_NEAREST
                )
            );
        }

        while (GuKeyBindings.FLYING_SWORD_RECALL_NEAREST.consumeClick()) {
            PacketDistributor.sendToServer(
                new ServerboundFlyingSwordActionPayload(
                    ServerboundFlyingSwordActionPayload.Action.RECALL_NEAREST
                )
            );
        }

        while (GuKeyBindings.FLYING_SWORD_RECALL_ALL.consumeClick()) {
            PacketDistributor.sendToServer(
                new ServerboundFlyingSwordActionPayload(
                    ServerboundFlyingSwordActionPayload.Action.RECALL_ALL
                )
            );
        }

        while (GuKeyBindings.FLYING_SWORD_RESTORE_ONE.consumeClick()) {
            PacketDistributor.sendToServer(
                new ServerboundFlyingSwordActionPayload(
                    ServerboundFlyingSwordActionPayload.Action.RESTORE_ONE
                )
            );
        }

        while (GuKeyBindings.FLYING_SWORD_RESTORE_ALL.consumeClick()) {
            PacketDistributor.sendToServer(
                new ServerboundFlyingSwordActionPayload(
                    ServerboundFlyingSwordActionPayload.Action.RESTORE_ALL
                )
            );
        }

        while (GuKeyBindings.FLYING_SWORD_BENMING_ACTION.consumeClick()) {
            final BenmingClientActionResolver.BenmingActionRoute actionRoute =
                BenmingClientActionResolver.resolveActionRoute(
                    Screen.hasShiftDown(),
                    Screen.hasControlDown()
                );
            final long currentTick = minecraft.player.level().getGameTime();
            if (BenmingClientActionResolver.shouldSendAction(actionRoute, currentTick)) {
                PacketDistributor.sendToServer(
                    BenmingClientActionResolver.createPayload(actionRoute)
                );
            }
        }

        // 切换飞剑 HUD 显示
        while (GuKeyBindings.FLYING_SWORD_TOGGLE_HUD.consumeClick()) {
            FlyingSwordHudState.toggleHud();
        }
    }

    static ServerboundBenmingSwordActionPayload resolveBenmingActionPayload() {
        return BenmingClientActionResolver.createPayload(
            BenmingClientActionResolver.resolveActionRoute(
                Screen.hasShiftDown(),
                Screen.hasControlDown()
            )
        );
    }
}

final class BenmingClientActionResolver {

    enum BenmingActionRoute {
        RITUAL_BIND,
        SWITCH_RESONANCE,
        BURST_ATTEMPT;
    }

    static final long BENMING_ACTION_THROTTLE_WINDOW_TICKS = 4L;

    private static final Map<BenmingActionRoute, Long> LAST_SENT_TICK_BY_ACTION =
        new HashMap<>();

    private BenmingClientActionResolver() {}

    static BenmingActionRoute resolveActionRoute(
        final boolean shiftDown,
        final boolean controlDown
    ) {
        if (controlDown) {
            return BenmingActionRoute.BURST_ATTEMPT;
        }
        if (shiftDown) {
            return BenmingActionRoute.SWITCH_RESONANCE;
        }
        return BenmingActionRoute.RITUAL_BIND;
    }

    static String resolveActionName(final boolean shiftDown, final boolean controlDown) {
        return resolveActionRoute(shiftDown, controlDown).name();
    }

    static ServerboundBenmingSwordActionPayload createPayload(
        final boolean shiftDown,
        final boolean controlDown
    ) {
        return createPayload(resolveActionRoute(shiftDown, controlDown));
    }

    static ServerboundBenmingSwordActionPayload createPayload(
        final BenmingActionRoute actionRoute
    ) {
        return switch (actionRoute) {
            case RITUAL_BIND -> createRitualBindPayload();
            case SWITCH_RESONANCE -> createSwitchResonancePayload();
            case BURST_ATTEMPT -> createBurstAttemptPayload();
        };
    }

    private static ServerboundBenmingSwordActionPayload createRitualBindPayload() {
        return createPayloadForActionName(BenmingActionRoute.RITUAL_BIND.name());
    }

    private static ServerboundBenmingSwordActionPayload createSwitchResonancePayload() {
        return createPayloadForActionName(BenmingActionRoute.SWITCH_RESONANCE.name());
    }

    private static ServerboundBenmingSwordActionPayload createBurstAttemptPayload() {
        return createPayloadForActionName(BenmingActionRoute.BURST_ATTEMPT.name());
    }

    private static ServerboundBenmingSwordActionPayload createPayloadForActionName(
        final String actionName
    ) {
        try {
            final Class<?> actionClass = Class.forName(
                "com.Kizunad.guzhenrenext.network.BenmingAction"
            );
            final Constructor<ServerboundBenmingSwordActionPayload> constructor =
                resolvePayloadConstructor(actionClass);
            return constructor.newInstance(resolveActionConstant(actionClass, actionName));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("无法构造本命动作数据包: " + actionName, exception);
        }
    }

    static boolean shouldSendAction(
        final BenmingActionRoute actionRoute,
        final long currentTick
    ) {
        return shouldSendAction(actionRoute, currentTick, BENMING_ACTION_THROTTLE_WINDOW_TICKS);
    }

    static boolean shouldSendAction(
        final BenmingActionRoute actionRoute,
        final long currentTick,
        final long throttleWindowTicks
    ) {
        final Long lastSentTick = LAST_SENT_TICK_BY_ACTION.get(actionRoute);
        if (lastSentTick != null && currentTick < lastSentTick) {
            LAST_SENT_TICK_BY_ACTION.put(actionRoute, currentTick);
            return true;
        }

        if (lastSentTick != null && currentTick - lastSentTick < throttleWindowTicks) {
            return false;
        }

        LAST_SENT_TICK_BY_ACTION.put(actionRoute, currentTick);
        return true;
    }

    static void resetThrottleStateForTests() {
        LAST_SENT_TICK_BY_ACTION.clear();
    }

    private static Constructor<ServerboundBenmingSwordActionPayload> resolvePayloadConstructor(
        final Class<?> actionClass
    ) throws NoSuchMethodException {
        final Constructor<ServerboundBenmingSwordActionPayload> constructor =
            ServerboundBenmingSwordActionPayload.class.getDeclaredConstructor(actionClass);
        constructor.setAccessible(true);
        return constructor;
    }

    private static Object resolveActionConstant(
        final Class<?> actionClass,
        final String actionName
    ) throws ReflectiveOperationException {
        final Method valueOfMethod = Enum.class.getMethod(
            "valueOf",
            Class.class,
            String.class
        );
        return valueOfMethod.invoke(null, actionClass.asSubclass(Enum.class), actionName);
    }
}
