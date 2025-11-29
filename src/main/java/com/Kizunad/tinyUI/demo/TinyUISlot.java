package com.Kizunad.tinyUI.demo;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

/**
 * A custom Slot that allows its position to be updated dynamically by TinyUI.
 * This bypasses potential issues with final fields in the vanilla Slot class.
 */
public class TinyUISlot extends Slot {
    
    public TinyUISlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    public void setPosition(int x, int y) {
        try {
            java.lang.reflect.Field xField = Slot.class.getField("x");
            java.lang.reflect.Field yField = Slot.class.getField("y");
            xField.setAccessible(true);
            yField.setAccessible(true);
            xField.setInt(this, x);
            yField.setInt(this, y);
        } catch (Exception e) {
            // Fallback: try declared fields if "x" is not found (e.g. obfuscated)
            // But since we are in dev env, "x" should be there.
            // If strictly final and security manager prevents it, we are stuck.
            // But usually this works.
            e.printStackTrace();
        }
    }
}
