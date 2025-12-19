package com.Kizunad.guzhenrenext.kongqiao.shazhao;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 杀招数据结构。
 * <p>
 * 用于描述一条杀招的显示信息、推演消耗，以及所需蛊虫列表。
 * </p>
 */
public record ShazhaoData(
    String shazhaoID,
    String title,
    String desc,
    String info,
    int costTotalNiantou,
    List<String> requiredItems,
    Map<String, String> metadata
) {

    public static final Codec<ShazhaoData> CODEC = RecordCodecBuilder.create(
        instance ->
            instance
                .group(
                    Codec.STRING.fieldOf("shazhaoID").forGetter(
                        ShazhaoData::shazhaoID
                    ),
                    Codec.STRING.fieldOf("title").forGetter(ShazhaoData::title),
                    Codec.STRING.fieldOf("desc").forGetter(ShazhaoData::desc),
                    Codec.STRING.fieldOf("info").forGetter(ShazhaoData::info),
                    Codec.INT.fieldOf("cost_total_niantou").forGetter(
                        ShazhaoData::costTotalNiantou
                    ),
                    Codec.STRING.listOf().fieldOf("required_items").forGetter(
                        ShazhaoData::requiredItems
                    ),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING)
                        .optionalFieldOf("metadata", Map.of())
                        .forGetter(ShazhaoData::metadata)
                )
                .apply(instance, ShazhaoData::new)
    );

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
        "\\{([a-zA-Z0-9_]+)\\}"
    );

    /**
     * 获取格式化后的杀招信息描述。
     * <p>
     * 将 info 中的 {key} 替换为 metadata 中的 value。
     * </p>
     */
    public String getFormattedInfo() {
        if (info == null || info.isEmpty()) {
            return "";
        }
        if (metadata == null || metadata.isEmpty()) {
            return info;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(info);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = metadata.getOrDefault(key, "{" + key + "}");
            matcher.appendReplacement(sb, value);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
