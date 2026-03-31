package com.Kizunad.guzhenrenext.entity.mind;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class EntityPersonality {

    private static final float MIN_VALUE = 0.0F;

    private static final float MAX_VALUE = 1.0F;

    private static final int FIRST_INDEX = 0;

    private static final String KEY_EMOTIONS = "emotions";

    private static final String KEY_DRIVES = "drives";

    private final float[] emotions;

    private final float[] drives;

    public EntityPersonality() {
        this.emotions = new float[EmotionType.values().length];
        this.drives = new float[DriveType.values().length];
    }

    public float getEmotion(EmotionType type) {
        Objects.requireNonNull(type, "type 不能为空");
        return emotions[type.ordinal()];
    }

    public void setEmotion(EmotionType type, float value) {
        Objects.requireNonNull(type, "type 不能为空");
        emotions[type.ordinal()] = clampValue(value);
    }

    public float getDrive(DriveType type) {
        Objects.requireNonNull(type, "type 不能为空");
        return drives[type.ordinal()];
    }

    public void setDrive(DriveType type, float value) {
        Objects.requireNonNull(type, "type 不能为空");
        drives[type.ordinal()] = clampValue(value);
    }

    public EmotionType getDominantEmotion() {
        EmotionType dominant = EmotionType.values()[FIRST_INDEX];
        float dominantValue = emotions[dominant.ordinal()];
        for (EmotionType type : EmotionType.values()) {
            float currentValue = emotions[type.ordinal()];
            if (currentValue > dominantValue) {
                dominant = type;
                dominantValue = currentValue;
            }
        }
        return dominant;
    }

    public DriveType getStrongestDrive() {
        DriveType strongest = DriveType.values()[FIRST_INDEX];
        float strongestValue = drives[strongest.ordinal()];
        for (DriveType type : DriveType.values()) {
            float currentValue = drives[type.ordinal()];
            if (currentValue > strongestValue) {
                strongest = type;
                strongestValue = currentValue;
            }
        }
        return strongest;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        CompoundTag emotionsTag = new CompoundTag();
        CompoundTag drivesTag = new CompoundTag();

        for (EmotionType type : EmotionType.values()) {
            emotionsTag.putFloat(type.id(), emotions[type.ordinal()]);
        }
        for (DriveType type : DriveType.values()) {
            drivesTag.putFloat(type.id(), drives[type.ordinal()]);
        }

        tag.put(KEY_EMOTIONS, emotionsTag);
        tag.put(KEY_DRIVES, drivesTag);
        return tag;
    }

    public void load(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag 不能为空");

        if (tag.contains(KEY_EMOTIONS, Tag.TAG_COMPOUND)) {
            CompoundTag emotionsTag = tag.getCompound(KEY_EMOTIONS);
            for (EmotionType type : EmotionType.values()) {
                float loaded = emotionsTag.getFloat(type.id());
                emotions[type.ordinal()] = clampValue(loaded);
            }
        }

        if (tag.contains(KEY_DRIVES, Tag.TAG_COMPOUND)) {
            CompoundTag drivesTag = tag.getCompound(KEY_DRIVES);
            for (DriveType type : DriveType.values()) {
                float loaded = drivesTag.getFloat(type.id());
                drives[type.ordinal()] = clampValue(loaded);
            }
        }
    }

    private static float clampValue(float value) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
    }

    public enum EmotionType {
        JOY("joy"),

        ANGER("anger"),

        WORRY("worry"),

        LONGING("longing"),

        GRIEF("grief"),

        FEAR("fear"),

        SURPRISE("surprise");

        private final String id;

        EmotionType(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    public enum DriveType {
        FOOD("food"),

        SLEEP("sleep"),

        SAFETY("safety"),

        WEALTH("wealth"),

        POWER("power"),

        AFFECTION("affection");

        private final String id;

        DriveType(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }
}
