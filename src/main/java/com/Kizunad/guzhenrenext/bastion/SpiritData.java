package com.Kizunad.guzhenrenext.bastion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;

/**
 * 地灵（狐仙）的持久化数据。
 *
 * @param favorability 好感度
 * @param expressionId 当前表情 ID
 * @param vaultItems   虚拟行囊物品列表（固定 9 格）
 */
public record SpiritData(float favorability, String expressionId, List<ItemStack> vaultItems) {

    /** 地灵虚拟行囊固定槽位数量。 */
    public static final int VAULT_SIZE = 9;

    /** 默认地灵数据。 */
    public static final SpiritData DEFAULT = new SpiritData(
        0.0F,
        "idle",
        createEmptyVaultItems()
    );

    /** 序列化/反序列化编解码器。 */
    public static final Codec<SpiritData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.FLOAT.optionalFieldOf("favorability", 0.0F).forGetter(SpiritData::favorability),
            Codec.STRING.optionalFieldOf("expression_id", "idle").forGetter(SpiritData::expressionId),
            ItemStack.CODEC.listOf().optionalFieldOf("vault_items", createEmptyVaultItems())
                .forGetter(SpiritData::vaultItems)
        ).apply(instance, SpiritData::new)
    );

    /**
     * 规范化地灵数据，确保字段不为 null。
     */
    public SpiritData {
        expressionId = expressionId == null ? "idle" : expressionId;
        vaultItems = normalizeVaultItems(vaultItems);
    }

    /**
     * 构造默认的空行囊物品列表。
     *
     * @return 固定 9 格且全部为空物品的列表
     */
    private static List<ItemStack> createEmptyVaultItems() {
        return Stream.generate(() -> ItemStack.EMPTY)
            .limit(VAULT_SIZE)
            .toList();
    }

    /**
     * 规范化虚拟行囊，始终返回固定 9 格列表。
     *
     * @param source 输入列表，允许为 null
     * @return 固定 9 格的安全副本
     */
    private static List<ItemStack> normalizeVaultItems(List<ItemStack> source) {
        if (source == null || source.isEmpty()) {
            return createEmptyVaultItems();
        }
        return java.util.stream.IntStream.range(0, VAULT_SIZE)
            .mapToObj(index -> {
                if (index >= source.size()) {
                    return ItemStack.EMPTY;
                }
                ItemStack stack = source.get(index);
                return stack == null ? ItemStack.EMPTY : stack;
            })
            .toList();
    }
}
