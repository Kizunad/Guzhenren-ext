package com.Kizunad.guzhenrenext.kongqiao.menu;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 空窍系统相关菜单注册表。
 */
public final class KongqiaoMenus {

    private KongqiaoMenus() {}

    private static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, GuzhenrenExt.MODID);

    public static final DeferredHolder<
        MenuType<?>,
        MenuType<KongqiaoMenu>
    > KONGQIAO = MENUS.register("kongqiao", () ->
        IMenuTypeExtension.create(KongqiaoMenu::fromNetwork)
    );

    public static final DeferredHolder<
        MenuType<?>,
        MenuType<AttackInventoryMenu>
    > ATTACK_INVENTORY = MENUS.register("attack_inventory", () ->
        IMenuTypeExtension.create(AttackInventoryMenu::fromNetwork)
    );

    public static final DeferredHolder<
        MenuType<?>,
        MenuType<GuchongFeedMenu>
    > GUCHONG_FEED = MENUS.register("guchong_feed", () ->
        IMenuTypeExtension.create(GuchongFeedMenu::fromNetwork)
    );

    public static final DeferredHolder<
        MenuType<?>,
        MenuType<NianTouMenu>
    > NIANTOU = MENUS.register("niantou", () ->
        IMenuTypeExtension.create(NianTouMenu::fromNetwork)
    );

    public static final DeferredHolder<
        MenuType<?>,
        MenuType<com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge.FlyingSwordForgeMenu>
    > FLYING_SWORD_FORGE = MENUS.register("flying_sword_forge", () ->
        IMenuTypeExtension.create(
            com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge.FlyingSwordForgeMenu::fromNetwork
        )
    );

    public static final DeferredHolder<
        MenuType<?>,
        MenuType<com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingMenu>
    > FLYING_SWORD_TRAINING = MENUS.register("flying_sword_training", () ->
        IMenuTypeExtension.create(
            com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingMenu::fromNetwork
        )
    );

    public static final DeferredHolder<
        MenuType<?>,
        MenuType<com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster.FlyingSwordClusterMenu>
    > FLYING_SWORD_CLUSTER = MENUS.register("flying_sword_cluster", () ->
        IMenuTypeExtension.create(
            com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster.FlyingSwordClusterMenu::fromNetwork
        )
    );

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
