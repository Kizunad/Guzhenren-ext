package com.Kizunad.guzhenrenext.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class GuKeyBindings {

    public static final String KEY_CATEGORY = "key.categories.guzhenrenext";
    
    // 定义快捷键：默认绑定到 'N' 键 (NianTou)
    public static final KeyMapping OPEN_NIANTOU_GUI = new KeyMapping(
        "key.guzhenrenext.open_niantou",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_N,
        KEY_CATEGORY
    );

    /**
     * 定义快捷键：按住呼出技能轮盘，松开确认选择。
     * <p>
     * 默认绑定到 'R' 键，符合大多数模组的“轮盘快捷键”习惯。
     * </p>
     */
    public static final KeyMapping OPEN_SKILL_WHEEL = new KeyMapping(
        "key.guzhenrenext.open_skill_wheel",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        KEY_CATEGORY
    );

    /**
     * 定义快捷键：打开空窍调整面板（Tweak UI）。
     * <p>
     * 默认绑定到 'O' 键（Options），便于与鉴定面板区分；可在按键设置中自行修改。
     * </p>
     */
    public static final KeyMapping OPEN_TWEAK_UI = new KeyMapping(
        "key.guzhenrenext.open_tweak_ui",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_O,
        KEY_CATEGORY
    );

    private GuKeyBindings() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_NIANTOU_GUI);
        event.register(OPEN_SKILL_WHEEL);
        event.register(OPEN_TWEAK_UI);
    }
}
