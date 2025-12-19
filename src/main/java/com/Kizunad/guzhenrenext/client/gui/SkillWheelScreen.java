package com.Kizunad.guzhenrenext.client.gui;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager.UsageLookup;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoId;
import com.Kizunad.guzhenrenext.network.ServerboundSkillWheelSelectPayload;
import com.Kizunad.tinyUI.component.RadialMenu;
import com.Kizunad.tinyUI.core.ScaleConfig;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.neoforge.TinyUIScreen;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 技能轮盘（按住-选择-松开）。
 * <p>
 * 当前实现的“技能列表”来自玩家热键栏（0-8），用于提供可视化图标与最小可验证闭环。
 * 后续若要接入真实技能/蛊虫数据，只需替换 {@link #buildOptions(Player)}，并调整服务器侧处理逻辑即可。
 * </p>
 */
public final class SkillWheelScreen extends TinyUIScreen {

    private static final int DESIGN_WIDTH = 1920 / 2;
    private static final int DESIGN_HEIGHT = 1080 / 2;

    private static final int MENU_INNER_RADIUS = 90;
    private static final int MENU_OUTER_RADIUS = 220;

    private static final int MOUSE_BUTTON_LEFT = 0;

    private static final int CENTER_LABEL_COLOR = 0xFFFFFFFF;
    private static final int EMPTY_HINT_COLOR = 0xFFAAAAAA;
    private static final int EMPTY_HINT_Y_OFFSET = 30;

    private final KeyMapping holdKey;
    private RadialMenu radialMenu;
    private boolean closeFinalized;
    private List<String> optionUsageIds = List.of();
    private int optionCount;

    public SkillWheelScreen(final KeyMapping holdKey) {
        super(
            Component.translatable("screen.guzhenrenext.skill_wheel.title"),
            createRoot()
        );
        this.holdKey = holdKey;
    }

    private static UIRoot createRoot() {
        final UIRoot root = new UIRoot();
        root.setDesignResolution(DESIGN_WIDTH, DESIGN_HEIGHT);
        return root;
    }

    @Override
    protected void init() {
        super.init();

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            minecraft.setScreen(null);
            return;
        }

        final WheelOptions options = buildWheelOptions(minecraft.player);
        this.optionUsageIds = options.usageIds();
        this.optionCount = options.options().size();
        radialMenu = new RadialMenu(
            MENU_INNER_RADIUS,
            MENU_OUTER_RADIUS,
            options.options()
        );
    }

    @Override
    public void tick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || radialMenu == null) {
            minecraft.setScreen(null);
            return;
        }

        // 松开按键：确认当前悬停项并关闭。
        if (!isHoldKeyDown(minecraft)) {
            confirmSelectionAndClose();
            return;
        }

        radialMenu.tick();
    }

    @Override
    protected void renderScaledContent(
        final GuiGraphics graphics,
        final int mouseX,
        final int mouseY,
        final float partialTick
    ) {
        if (radialMenu == null) {
            return;
        }

        final int centerX = resolveDesignCenterX();
        final int centerY = resolveDesignCenterY();

        radialMenu.updateSelection(mouseX, mouseY, centerX, centerY);
        radialMenu.render(graphics, centerX, centerY);

        if (optionCount <= 0) {
            final Component hint = Component.literal("未配置轮盘技能");
            final int hintWidth = font.width(hint);
            graphics.drawString(
                font,
                hint,
                centerX - hintWidth / 2,
                centerY - font.lineHeight / 2 + EMPTY_HINT_Y_OFFSET,
                EMPTY_HINT_COLOR,
                false
            );
            return;
        }

        final RadialMenu.Option hovered = radialMenu.getHoveredOption();
        if (hovered == null || hovered.label().getString().isBlank()) {
            return;
        }

        final int labelWidth = font.width(hovered.label());
        graphics.drawString(
            font,
            hovered.label(),
            centerX - labelWidth / 2,
            centerY - font.lineHeight / 2,
            CENTER_LABEL_COLOR,
            false
        );
    }

    @Override
    public boolean mouseClicked(
        final double mouseX,
        final double mouseY,
        final int button
    ) {
        if (button == MOUSE_BUTTON_LEFT) {
            confirmSelectionAndClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(
        final int keyCode,
        final int scanCode,
        final int modifiers
    ) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            // ESC 视为取消，不触发选择。
            closeFinalized = true;
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void confirmSelectionAndClose() {
        if (closeFinalized) {
            return;
        }
        closeFinalized = true;

        final int hoveredIndex = radialMenu.getHoveredIndex();
        if (hoveredIndex >= 0 && hoveredIndex < optionUsageIds.size()) {
            PacketDistributor.sendToServer(
                new ServerboundSkillWheelSelectPayload(
                    optionUsageIds.get(hoveredIndex)
                )
            );
        }

        Minecraft.getInstance().setScreen(null);
    }

    private int resolveDesignCenterX() {
        final ScaleConfig scaleConfig = getRoot().getScaleConfig();
        final double designWidth = scaleConfig.unscale(width);
        return (int) (designWidth / 2.0D);
    }

    private int resolveDesignCenterY() {
        final ScaleConfig scaleConfig = getRoot().getScaleConfig();
        final double designHeight = scaleConfig.unscale(height);
        return (int) (designHeight / 2.0D);
    }

    private boolean isHoldKeyDown(final Minecraft minecraft) {
        final long window = minecraft.getWindow().getWindow();
        final InputConstants.Key boundKey = holdKey.getKey();
        if (boundKey.getType() == InputConstants.Type.MOUSE) {
            return (
                GLFW.glfwGetMouseButton(window, boundKey.getValue()) ==
                GLFW.GLFW_PRESS
            );
        }
        if (boundKey.getType() == InputConstants.Type.KEYSYM) {
            return (
                GLFW.glfwGetKey(window, boundKey.getValue()) == GLFW.GLFW_PRESS
            );
        }
        return holdKey.isDown();
    }

    private static WheelOptions buildWheelOptions(final Player player) {
        final TweakConfig config = KongqiaoAttachments.getTweakConfig(player);
        final List<String> wheelSkills = config != null
            ? config.getWheelSkills()
            : List.of();
        if (wheelSkills.isEmpty()) {
            return new WheelOptions(List.of(), List.of());
        }

        final List<RadialMenu.Option> options = new ArrayList<>(
            wheelSkills.size()
        );
        final List<String> usageIds = new ArrayList<>(wheelSkills.size());
        for (String usageId : wheelSkills) {
            final ResolvedOption resolved = resolveOption(usageId);
            options.add(
                new RadialMenu.Option(resolved.icon(), resolved.label())
            );
            usageIds.add(usageId);
        }
        return new WheelOptions(List.copyOf(options), List.copyOf(usageIds));
    }

    private static ResolvedOption resolveOption(final String usageId) {
        if (ShazhaoId.isActive(usageId)) {
            final ResolvedOption resolved = resolveShazhaoOption(usageId);
            if (resolved != null) {
                return resolved;
            }
        }
        final UsageLookup lookup = NianTouDataManager.findUsageLookup(usageId);
        if (
            lookup == null ||
            lookup.item() == null ||
            lookup.item() == Items.AIR
        ) {
            return new ResolvedOption(
                ItemStack.EMPTY,
                Component.literal(usageId)
            );
        }
        final String title = lookup.usage() != null &&
            lookup.usage().usageTitle() != null
            ? lookup.usage().usageTitle()
            : usageId;
        return new ResolvedOption(
            new ItemStack(lookup.item()),
            Component.literal(title)
        );
    }

    private static ResolvedOption resolveShazhaoOption(final String shazhaoId) {
        final ResourceLocation id;
        try {
            id = ResourceLocation.parse(shazhaoId);
        } catch (Exception e) {
            return null;
        }
        final ShazhaoData data = ShazhaoDataManager.get(id);
        if (data == null) {
            return null;
        }
        final String title = data.title() != null ? data.title() : shazhaoId;
        return new ResolvedOption(
            resolveShazhaoIcon(data),
            Component.literal(title)
        );
    }

    private static ItemStack resolveShazhaoIcon(final ShazhaoData data) {
        if (data.requiredItems() == null || data.requiredItems().isEmpty()) {
            return ItemStack.EMPTY;
        }
        final String itemId = data.requiredItems().get(0);
        if (itemId == null || itemId.isBlank()) {
            return ItemStack.EMPTY;
        }
        final ResourceLocation id;
        try {
            id = ResourceLocation.parse(itemId);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
        final Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    private record ResolvedOption(ItemStack icon, Component label) {}

    private record WheelOptions(
        List<RadialMenu.Option> options,
        List<String> usageIds
    ) {}
}
