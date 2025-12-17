package com.Kizunad.guzhenrenext.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ClientConfig {

    public static final ClientConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    public static final double MIN_UI_SCALE = 0.1;
    public static final double MAX_UI_SCALE = 5.0;

    public final ModConfigSpec.DoubleValue kongQiaoUiScale;

    static {
        Pair<ClientConfig, ModConfigSpec> pair = new ModConfigSpec.Builder()
            .configure(ClientConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    private ClientConfig(ModConfigSpec.Builder builder) {
        builder.push("client");

        kongQiaoUiScale = builder
            .comment("The scale of the Kong Qiao (Aperture) UI.")
            .defineInRange("kongQiaoUiScale", 1.0, MIN_UI_SCALE, MAX_UI_SCALE);

        builder.pop();
    }
}
