package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical;

import java.util.Objects;
import net.minecraft.network.chat.Component;

public record TacticalBadgeSpec(Component label, TacticalTone tone) {

    public TacticalBadgeSpec {
        label = Objects.requireNonNullElse(label, Component.empty());
        tone = Objects.requireNonNullElse(tone, TacticalTone.NEUTRAL);
    }

    public static TacticalBadgeSpec of(final String label, final TacticalTone tone) {
        return new TacticalBadgeSpec(
            Component.literal(Objects.requireNonNullElse(label, "")),
            tone
        );
    }
}
