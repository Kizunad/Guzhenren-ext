package com.Kizunad.customNPCs.ai.logging;

import java.util.EnumMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MindLog {

    private static final Logger LOGGER = LoggerFactory.getLogger("NpcMind");
    private static final EnumMap<MindLogCategory, Boolean> CATEGORY_SWITCHES =
        new EnumMap<>(MindLogCategory.class);

    static {
        for (MindLogCategory category : MindLogCategory.values()) {
            CATEGORY_SWITCHES.put(category, false);
        }
    }

    private MindLog() {}

    public static void decision(
        MindLogLevel level,
        String message,
        Object... args
    ) {
        log(MindLogCategory.DECISION, level, message, args);
    }

    public static void planning(
        MindLogLevel level,
        String message,
        Object... args
    ) {
        log(MindLogCategory.PLANNING, level, message, args);
    }

    public static void execution(
        MindLogLevel level,
        String message,
        Object... args
    ) {
        log(MindLogCategory.EXECUTION, level, message, args);
    }

    public static void setCategoryEnabled(
        MindLogCategory category,
        boolean enabled
    ) {
        CATEGORY_SWITCHES.put(category, enabled);
    }

    public static void setAllEnabled(boolean enabled) {
        for (MindLogCategory category : MindLogCategory.values()) {
            CATEGORY_SWITCHES.put(category, enabled);
        }
    }

    public static boolean isEnabled(MindLogCategory category) {
        return Boolean.TRUE.equals(CATEGORY_SWITCHES.get(category));
    }

    public static void log(
        MindLogCategory category,
        MindLogLevel level,
        String message,
        Object... args
    ) {
        if (!shouldLog(category, level)) {
            return;
        }

        String formatted = format(category, message);
        switch (level) {
            case DEBUG:
                LOGGER.debug(formatted, args);
                break;
            case INFO:
                LOGGER.info(formatted, args);
                break;
            case WARN:
                LOGGER.warn(formatted, args);
                break;
            case ERROR:
                LOGGER.error(formatted, args);
                break;
            default:
                LOGGER.info(formatted, args);
                break;
        }
    }

    private static boolean shouldLog(
        MindLogCategory category,
        MindLogLevel level
    ) {
        if (level == MindLogLevel.ERROR || level == MindLogLevel.WARN) {
            return true;
        }
        return isEnabled(category);
    }

    private static String format(MindLogCategory category, String message) {
        return "[NpcMind|" + category.getDisplayName() + "] " + message;
    }
}
