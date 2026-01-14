package com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthTuning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class FlyingSwordImprint {

    private static final String TAG_MAIN_DAO = "MainDao";
    private static final String TAG_MARKS = "Marks";
    private static final String TAG_TOTAL = "Total";
    private static final String TAG_TIER = "Tier";
    private static final String TAG_TRAITS = "Traits";

    private static final int TIER_0 = 0;
    private static final int TIER_1 = 1;
    private static final int TIER_2 = 2;
    private static final int TIER_3 = 3;

    private String mainDao = DaoHenHelper.DaoType.GENERIC.getKey();
    private final Map<String, Integer> marks = new HashMap<>();
    private int total = 0;
    private int tier = 0;
    private final List<String> traits = new ArrayList<>();

    public FlyingSwordImprint() {}

    public String getMainDao() {
        return mainDao;
    }

    public void setMainDao(@Nullable String mainDao) {
        this.mainDao = normalizeKey(mainDao);
    }

    public Map<String, Integer> getMarks() {
        return Collections.unmodifiableMap(marks);
    }

    public int getTotal() {
        return total;
    }

    public int getTier() {
        return tier;
    }

    public List<String> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    public boolean isEmpty() {
        return marks.isEmpty() || total <= 0;
    }

    public void putMark(@Nullable String daoKey, int points) {
        final String key = normalizeKey(daoKey);
        if (key.isBlank() || points <= 0) {
            return;
        }
        marks.merge(key, points, Integer::sum);
        recalculate();
    }

    public int getMark(@Nullable String daoKey) {
        final String key = normalizeKey(daoKey);
        if (key.isBlank()) {
            return 0;
        }
        return marks.getOrDefault(key, 0);
    }

    public void setMarks(@Nullable Map<String, Integer> input) {
        marks.clear();
        if (input != null) {
            for (var entry : input.entrySet()) {
                if (entry == null) {
                    continue;
                }
                final String key = normalizeKey(entry.getKey());
                final int value = entry.getValue() == null ? 0 : entry.getValue();
                if (key.isBlank() || value <= 0) {
                    continue;
                }
                marks.put(key, value);
            }
        }
        recalculate();
    }

    public void setTraits(@Nullable List<String> input) {
        traits.clear();
        if (input != null) {
            for (String trait : input) {
                if (trait == null) {
                    continue;
                }
                String t = trait.trim();
                if (!t.isBlank()) {
                    traits.add(t);
                }
            }
        }
    }

    private static final int SUB_TRAIT_THRESHOLD = 12;
    private static final int SUB_TRAIT_MAX_COUNT = 2;

    private static final String TRAIT_PREFIX_PROC = "proc:";
    private static final String TRAIT_PREFIX_STAT = "stat:";

    public void recalculate() {
        total = 0;
        for (var entry : marks.entrySet()) {
            if (entry == null) {
                continue;
            }
            Integer v = entry.getValue();
            if (v != null && v > 0) {
                total += v;
            }
        }

        tier = computeTier(total);
        mainDao = resolveMainDao(marks);

        generateTraits();
    }

    private void generateTraits() {
        traits.clear();

        if (tier <= 0 || mainDao == null || mainDao.isBlank()) {
            return;
        }

        traits.add(TRAIT_PREFIX_PROC + mainDao);

        if (marks.isEmpty()) {
            return;
        }

        List<Map.Entry<String, Integer>> subs = new ArrayList<>();
        for (var entry : marks.entrySet()) {
            if (entry == null) {
                continue;
            }
            String key = normalizeKey(entry.getKey());
            Integer value = entry.getValue();
            if (key.isBlank() || value == null || value <= 0) {
                continue;
            }
            if (key.equals(mainDao)) {
                continue;
            }
            if (value < SUB_TRAIT_THRESHOLD) {
                continue;
            }
            subs.add(Map.entry(key, value));
        }

        subs.sort((a, b) -> {
            int va = a.getValue() == null ? 0 : a.getValue();
            int vb = b.getValue() == null ? 0 : b.getValue();
            int c = Integer.compare(vb, va);
            if (c != 0) {
                return c;
            }
            return a.getKey().compareTo(b.getKey());
        });

        int added = 0;
        for (var entry : subs) {
            if (added >= SUB_TRAIT_MAX_COUNT) {
                break;
            }
            String key = normalizeKey(entry.getKey());
            if (key.isBlank()) {
                continue;
            }
            traits.add(TRAIT_PREFIX_STAT + key);
            added++;
        }
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();

        if (mainDao != null && !mainDao.isBlank()) {
            tag.putString(TAG_MAIN_DAO, mainDao);
        }

        if (!marks.isEmpty()) {
            CompoundTag marksTag = new CompoundTag();
            for (var entry : marks.entrySet()) {
                if (entry == null) {
                    continue;
                }
                final String key = normalizeKey(entry.getKey());
                final Integer value = entry.getValue();
                if (key.isBlank() || value == null || value <= 0) {
                    continue;
                }
                marksTag.putInt(key, value);
            }
            if (!marksTag.isEmpty()) {
                tag.put(TAG_MARKS, marksTag);
            }
        }

        if (total > 0) {
            tag.putInt(TAG_TOTAL, total);
        }
        if (tier > 0) {
            tag.putInt(TAG_TIER, tier);
        }

        if (!traits.isEmpty()) {
            ListTag list = new ListTag();
            for (String trait : traits) {
                if (trait == null) {
                    continue;
                }
                String t = trait.trim();
                if (!t.isBlank()) {
                    list.add(StringTag.valueOf(t));
                }
            }
            if (!list.isEmpty()) {
                tag.put(TAG_TRAITS, list);
            }
        }

        return tag;
    }

    public static FlyingSwordImprint fromNBT(@Nullable CompoundTag tag) {
        FlyingSwordImprint imprint = new FlyingSwordImprint();
        if (tag == null) {
            return imprint;
        }

        if (tag.contains(TAG_MAIN_DAO, Tag.TAG_STRING)) {
            imprint.mainDao = normalizeKey(tag.getString(TAG_MAIN_DAO));
        }

        if (tag.contains(TAG_MARKS, Tag.TAG_COMPOUND)) {
            CompoundTag marksTag = tag.getCompound(TAG_MARKS);
            for (String key : marksTag.getAllKeys()) {
                if (key == null || key.isBlank()) {
                    continue;
                }
                int v = marksTag.getInt(key);
                if (v > 0) {
                    imprint.marks.put(normalizeKey(key), v);
                }
            }
        }

        if (tag.contains(TAG_TRAITS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_TRAITS, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                String t = list.getString(i);
                if (t != null && !t.isBlank()) {
                    imprint.traits.add(t);
                }
            }
        }

        imprint.recalculate();

        if (tag.contains(TAG_TOTAL, Tag.TAG_INT)) {
            int storedTotal = tag.getInt(TAG_TOTAL);
            if (storedTotal > imprint.total) {
                imprint.total = storedTotal;
            }
        }
        if (tag.contains(TAG_TIER, Tag.TAG_INT)) {
            int storedTier = tag.getInt(TAG_TIER);
            if (storedTier > imprint.tier) {
                imprint.tier = storedTier;
            }
        }

        return imprint;
    }

    public static int computeTier(int totalPoints) {
        if (totalPoints < SwordGrowthTuning.IMPRINT_TIER_1_THRESHOLD) {
            return TIER_0;
        }
        if (totalPoints < SwordGrowthTuning.IMPRINT_TIER_2_THRESHOLD) {
            return TIER_1;
        }
        if (totalPoints < SwordGrowthTuning.IMPRINT_TIER_3_THRESHOLD) {
            return TIER_2;
        }
        if (totalPoints < SwordGrowthTuning.IMPRINT_TIER_4_THRESHOLD) {
            return TIER_3;
        }
        return SwordGrowthTuning.IMPRINT_TIER_CAP;
    }

    private static final String DAO_KEY_GENERIC =
        DaoHenHelper.DaoType.GENERIC.getKey();
    private static final String DAO_KEY_JIANDAO =
        DaoHenHelper.DaoType.JIAN_DAO.getKey();
    private static final String DAO_KEY_YANDAO = DaoHenHelper.DaoType.HUO_DAO.getKey();
    private static final String DAO_KEY_LEIDAO = DaoHenHelper.DaoType.LEI_DAO.getKey();

    private static final int PRIORITY_JIANDAO = 0;
    private static final int PRIORITY_YANDAO = 1;
    private static final int PRIORITY_LEIDAO = 2;
    private static final int PRIORITY_DEFAULT = 100;

    private static String resolveMainDao(Map<String, Integer> marks) {
        if (marks == null || marks.isEmpty()) {
            return DAO_KEY_GENERIC;
        }

        String bestKey = DAO_KEY_GENERIC;
        int bestValue = 0;
        int bestPriority = PRIORITY_DEFAULT;

        for (var entry : marks.entrySet()) {
            if (entry == null) {
                continue;
            }
            String key = normalizeKey(entry.getKey());
            Integer value = entry.getValue();
            if (key.isBlank() || value == null) {
                continue;
            }
            int v = value;
            if (v <= 0) {
                continue;
            }

            int priority = getDaoPriority(key);
            if (v > bestValue) {
                bestValue = v;
                bestKey = key;
                bestPriority = priority;
                continue;
            }
            if (v < bestValue) {
                continue;
            }

            if (priority < bestPriority) {
                bestKey = key;
                bestPriority = priority;
                continue;
            }

            if (priority == bestPriority && key.compareTo(bestKey) < 0) {
                bestKey = key;
            }
        }

        return bestKey;
    }

    private static int getDaoPriority(String daoKey) {
        if (daoKey == null) {
            return PRIORITY_DEFAULT;
        }
        if (DAO_KEY_JIANDAO.equals(daoKey)) {
            return PRIORITY_JIANDAO;
        }
        if (DAO_KEY_YANDAO.equals(daoKey)) {
            return PRIORITY_YANDAO;
        }
        if (DAO_KEY_LEIDAO.equals(daoKey)) {
            return PRIORITY_LEIDAO;
        }
        return PRIORITY_DEFAULT;
    }

    private static String normalizeKey(@Nullable String key) {
        if (key == null) {
            return "";
        }
        return key.trim().toLowerCase();
    }
}
