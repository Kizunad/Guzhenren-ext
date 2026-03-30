package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.inventory.AttackInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.GuchongFeedInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

final class KongqiaoSharedRuntimeFixtureRuntimeHelper {

    private static final UUID FIXTURE_OWNER_UUID = UUID.nameUUIDFromBytes(
        "task4_shared_runtime_fixture".getBytes(StandardCharsets.UTF_8)
    );
    private static final String FIXTURE_OWNER_NAME = "task4_shared_runtime";

    private KongqiaoSharedRuntimeFixtureRuntimeHelper() {}

    static SharedRuntimeFixture newSharedRuntimeFixtureOnLiveLevel(
        final ServerLevel liveServerLevel
    ) {
        if (liveServerLevel == null) {
            throw new IllegalArgumentException("liveServerLevel 不能为空");
        }

        final ServerPlayer owner = FakePlayerFactory.get(
            liveServerLevel,
            new GameProfile(FIXTURE_OWNER_UUID, FIXTURE_OWNER_NAME)
        );
        ensureOwnerAttachments(owner);

        final KongqiaoData data = KongqiaoAttachments.getData(owner);
        final NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(owner);
        final TweakConfig tweakConfig = KongqiaoAttachments.getTweakConfig(owner);
        final ActivePassives activePassives = KongqiaoAttachments.getActivePassives(owner);
        final KongqiaoInventory kongqiaoInventory = data == null
            ? null
            : data.getKongqiaoInventory();
        final AttackInventory attackInventory = data == null
            ? null
            : data.getAttackInventory();
        final GuchongFeedInventory feedInventory = data == null
            ? null
            : data.getFeedInventory();

        return new SharedRuntimeFixture(
            owner,
            data,
            unlocks,
            tweakConfig,
            activePassives,
            kongqiaoInventory,
            attackInventory,
            feedInventory
        );
    }

    private static void ensureOwnerAttachments(final ServerPlayer owner) {
        if (!owner.hasData(KongqiaoAttachments.KONGQIAO.get())) {
            owner.setData(KongqiaoAttachments.KONGQIAO.get(), new KongqiaoData());
        }

        final KongqiaoData data = KongqiaoAttachments.getData(owner);
        if (data != null) {
            data.bind(owner);
        }

        if (!owner.hasData(KongqiaoAttachments.NIANTOU_UNLOCKS.get())) {
            owner.setData(KongqiaoAttachments.NIANTOU_UNLOCKS.get(), new NianTouUnlocks());
        }

        KongqiaoAttachments.getTweakConfig(owner);
        KongqiaoAttachments.getActivePassives(owner);
    }

    record SharedRuntimeFixture(
        ServerPlayer owner,
        KongqiaoData data,
        NianTouUnlocks unlocks,
        TweakConfig tweakConfig,
        ActivePassives activePassives,
        KongqiaoInventory kongqiaoInventory,
        AttackInventory attackInventory,
        GuchongFeedInventory feedInventory
    ) {}
}
