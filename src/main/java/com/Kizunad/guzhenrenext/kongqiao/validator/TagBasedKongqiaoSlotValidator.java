package com.Kizunad.guzhenrenext.kongqiao.validator;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoConstants;
import java.util.Set;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 基于标签的空窍槽位校验器。
 * <p>
 * 该实现直接复用配置中的标签集合，可以在运行时扩展，也可按需更换实现。
 * </p>
 */
public class TagBasedKongqiaoSlotValidator implements KongqiaoSlotValidator {

    private final Set<TagKey<Item>> allowedTags;

    public TagBasedKongqiaoSlotValidator() {
        this(KongqiaoConstants.ALLOWED_TAGS);
    }

    public TagBasedKongqiaoSlotValidator(Set<TagKey<Item>> allowedTags) {
        this.allowedTags = allowedTags;
    }

    @Override
    public boolean canPlace(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (TagKey<Item> tag : allowedTags) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }
}
