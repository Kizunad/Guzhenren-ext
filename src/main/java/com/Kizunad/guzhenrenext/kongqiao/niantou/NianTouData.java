package com.Kizunad.guzhenrenext.kongqiao.niantou;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

/*
JSON 结构示例:
{
  "itemID": "minecraft:apple",
  "usages": [
    {
      "usageID": "eat",
      "usageTitle": "食用",
      "usageDesc": "恢复饥饿值",
      "usageInfo": "饱食度: 2.4",
      "cost_duration": 100,
      "cost_total_niantou": 10
    },
    {
      "usageID": "compost",
      "usageTitle": "堆肥",
      "usageDesc": "放入堆肥桶",
      "usageInfo": "概率: 65%",
      "cost_duration": 50,
      "cost_total_niantou": 5
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
        int costTotalNiantou
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
                        )
                    )
                    .apply(instance, Usage::new)
        );
    }
}
