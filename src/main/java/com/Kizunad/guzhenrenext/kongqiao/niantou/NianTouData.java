package com.Kizunad.guzhenrenext.kongqiao.niantou;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
JSON 结构示例:
{
  "itemID": "minecraft:apple",
  "usages": [
    {
      "usageID": "guzhenren:xiaohungu_passive_regen",
      "usageTitle": "滋养魂魄",
      "usageDesc": "存放在空窍中时，持续滋养魂魄。",
      "usageInfo": "每秒回复 {regen} 点魂魄",
      "cost_duration": 100,
      "cost_total_niantou": 10,
      "metadata": {
        "regen": "2.0"
      }
    }
  ]
}
*/

/**
 * 念头鉴定信息数据结构。
 * 用于描述一个物品被鉴定后显示的多条用途/效果信息。
 */
public record NianTouData(String itemID, List<Usage> usages) {
    public static final Codec<NianTouData> CODEC = RecordCodecBuilder.create(
        instance ->
            instance
                .group(
                    Codec.STRING.fieldOf("itemID").forGetter(
                        NianTouData::itemID
                    ),
                    Usage.CODEC.listOf()
                        .fieldOf("usages")
                        .forGetter(NianTouData::usages)
                )
                .apply(instance, NianTouData::new)
    );

    public record Usage(
        String usageID,
        String usageTitle,
        String usageDesc,
        String usageInfo,
        int costDuration,
        int costTotalNiantou,
        Map<String, String> metadata
    ) {
        public static final Codec<Usage> CODEC = RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.STRING.fieldOf("usageID").forGetter(
                            Usage::usageID
                        ),
                        Codec.STRING.fieldOf("usageTitle").forGetter(
                            Usage::usageTitle
                        ),
                        Codec.STRING.fieldOf("usageDesc").forGetter(
                            Usage::usageDesc
                        ),
                        Codec.STRING.fieldOf("usageInfo").forGetter(
                            Usage::usageInfo
                        ),
                        Codec.INT.fieldOf("cost_duration").forGetter(
                            Usage::costDuration
                        ),
                        Codec.INT.fieldOf("cost_total_niantou").forGetter(
                            Usage::costTotalNiantou
                        ),
                        Codec.unboundedMap(Codec.STRING, Codec.STRING)
                            .optionalFieldOf("metadata", Map.of())
                            .forGetter(Usage::metadata)
                    )
                    .apply(instance, Usage::new)
        );

        private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");

        /**
         * 获取格式化后的 UsageInfo。
         * 将 {key} 替换为 metadata 中的 value。
         */
        public String getFormattedInfo() {
            if (usageInfo == null || usageInfo.isEmpty()) {
                return "";
            }
            if (metadata == null || metadata.isEmpty()) {
                return usageInfo;
            }

            Matcher matcher = PLACEHOLDER_PATTERN.matcher(usageInfo);
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
}
