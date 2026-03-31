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

@EventBusSubscriber(
    modid = GuzhenrenExt.MODID,
    value = Dist.CLIENT,
    bus = EventBusSubscriber.Bus.MOD
)
public final class GuKeyBindings {

    public static final String KEY_CATEGORY = "key.categories.guzhenrenext";

    // ===== 念头/轮盘/调整面板 =====

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

    public static final KeyMapping OPEN_FACTION_INFO = new KeyMapping(
        "key.guzhenrenext.open_faction_info",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_J,
        KEY_CATEGORY
    );

    // ===== 飞剑（Phase 2 最小输入） =====

    /**
     * 选择最近飞剑。
     * <p>
     * 说明：Phase 2 暂时用“最近目标”完成闭环；后续可以接入 UI/轮盘/锁定系统。
     * </p>
     */
    public static final KeyMapping FLYING_SWORD_SELECT_NEAREST = new KeyMapping(
        "key.guzhenrenext.flyingsword.select_nearest",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
        KEY_CATEGORY
    );

    /**
     * 切换最近/选中飞剑模式。
     */
    public static final KeyMapping FLYING_SWORD_CYCLE_MODE = new KeyMapping(
        "key.guzhenrenext.flyingsword.cycle_mode",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_X,
        KEY_CATEGORY
    );

    /**
     * 召回最近/选中飞剑。
     */
    public static final KeyMapping FLYING_SWORD_RECALL_NEAREST = new KeyMapping(
        "key.guzhenrenext.flyingsword.recall_nearest",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_C,
        KEY_CATEGORY
    );

    /**
     * 恢复一把已召回飞剑。
     */
    public static final KeyMapping FLYING_SWORD_RESTORE_ONE = new KeyMapping(
        "key.guzhenrenext.flyingsword.restore_one",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        KEY_CATEGORY
    );

    /**
     * 召回全部飞剑（默认不绑定，避免误触）。
     */
    public static final KeyMapping FLYING_SWORD_RECALL_ALL = new KeyMapping(
        "key.guzhenrenext.flyingsword.recall_all",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN,
        KEY_CATEGORY
    );

    /**
     * 恢复全部飞剑（默认不绑定，避免误触）。
     */
    public static final KeyMapping FLYING_SWORD_RESTORE_ALL = new KeyMapping(
        "key.guzhenrenext.flyingsword.restore_all",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN,
        KEY_CATEGORY
    );

    public static final KeyMapping FLYING_SWORD_TOGGLE_HUB = new KeyMapping(
        "key.guzhenrenext.flyingsword.toggle_hub",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_H,
        KEY_CATEGORY
    );

    public static final KeyMapping FLYING_SWORD_BENMING_ACTION = new KeyMapping(
        "key.guzhenrenext.flyingsword.benming_action",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        KEY_CATEGORY
    );

    private GuKeyBindings() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_NIANTOU_GUI);
        event.register(OPEN_SKILL_WHEEL);
        event.register(OPEN_TWEAK_UI);
        event.register(OPEN_FACTION_INFO);

        event.register(FLYING_SWORD_SELECT_NEAREST);
        event.register(FLYING_SWORD_CYCLE_MODE);
        event.register(FLYING_SWORD_RECALL_NEAREST);
        event.register(FLYING_SWORD_RESTORE_ONE);
        event.register(FLYING_SWORD_RECALL_ALL);
        event.register(FLYING_SWORD_RESTORE_ALL);
        event.register(FLYING_SWORD_TOGGLE_HUB);
        event.register(FLYING_SWORD_BENMING_ACTION);
    }
}
