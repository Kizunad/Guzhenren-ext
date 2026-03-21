package com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum FlyingSwordResonanceType {

    OFFENSE(
        "offense",
        "烈",
        KongqiaoI18n.RESONANCE_OFFENSE_NAME,
        KongqiaoI18n.RESONANCE_OFFENSE_DESC,
        KongqiaoI18n.RESONANCE_OFFENSE_COLOR_SEMANTIC,
        0xFFE36A2E
    ),

    DEFENSE(
        "defense",
        "稳",
        KongqiaoI18n.RESONANCE_DEFENSE_NAME,
        KongqiaoI18n.RESONANCE_DEFENSE_DESC,
        KongqiaoI18n.RESONANCE_DEFENSE_COLOR_SEMANTIC,
        0xFF2FA8B8
    ),

    SPIRIT(
        "spirit",
        "巧",
        KongqiaoI18n.RESONANCE_SPIRIT_NAME,
        KongqiaoI18n.RESONANCE_SPIRIT_DESC,
        KongqiaoI18n.RESONANCE_SPIRIT_COLOR_SEMANTIC,
        0xFF3A8DFF
    ),

    DEVOUR(
        "devour",
        "噬",
        KongqiaoI18n.RESONANCE_DEVOUR_NAME,
        KongqiaoI18n.RESONANCE_DEVOUR_DESC,
        KongqiaoI18n.RESONANCE_DEVOUR_COLOR_SEMANTIC,
        0xFF8A4FD6
    );

    private final String code;
    private final String chineseAlias;
    private final String displayNameKey;
    private final String descriptionKey;
    private final String colorSemanticKey;
    private final int primaryColor;

    FlyingSwordResonanceType(
        String code,
        String chineseAlias,
        String displayNameKey,
        String descriptionKey,
        String colorSemanticKey,
        int primaryColor
    ) {
        this.code = code;
        this.chineseAlias = chineseAlias;
        this.displayNameKey = displayNameKey;
        this.descriptionKey = descriptionKey;
        this.colorSemanticKey = colorSemanticKey;
        this.primaryColor = primaryColor;
    }

    public String getCode() {
        return code;
    }

    public String getChineseAlias() {
        return chineseAlias;
    }

    public String getDisplayNameKey() {
        return displayNameKey;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public String getColorSemanticKey() {
        return colorSemanticKey;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public static Optional<FlyingSwordResonanceType> resolve(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        final String normalized = raw.trim();
        return Arrays.stream(values())
            .filter(type -> type.matches(normalized))
            .findFirst();
    }

    private boolean matches(String raw) {
        if (chineseAlias.equals(raw)) {
            return true;
        }
        final String lower = raw.toLowerCase(Locale.ROOT);
        return code.equals(lower) || name().equalsIgnoreCase(lower);
    }
}
