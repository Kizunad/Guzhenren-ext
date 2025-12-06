package com.Kizunad.customNPCs.capabilities.mind;

import net.minecraft.nbt.CompoundTag;

/**
 * 材料点数账本，区分 Owner 与 NPC 自有存量。
 */
public class MaterialWallet {

    private static final String OWNER_KEY = "owner_material";
    private static final String ENTITY_KEY = "entity_material";

    private double ownerMaterial;
    private double entityMaterial;

    public double getOwnerMaterial() {
        return ownerMaterial;
    }

    public void setOwnerMaterial(double value) {
        this.ownerMaterial = clamp(value);
    }

    public double addOwnerMaterial(double delta) {
        this.ownerMaterial = clamp(this.ownerMaterial + delta);
        return this.ownerMaterial;
    }

    public double getEntityMaterial() {
        return entityMaterial;
    }

    public void setEntityMaterial(double value) {
        this.entityMaterial = clamp(value);
    }

    public double addEntityMaterial(double delta) {
        this.entityMaterial = clamp(this.entityMaterial + delta);
        return this.entityMaterial;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(OWNER_KEY, ownerMaterial);
        tag.putDouble(ENTITY_KEY, entityMaterial);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(OWNER_KEY)) {
            ownerMaterial = clamp(tag.getDouble(OWNER_KEY));
        }
        if (tag.contains(ENTITY_KEY)) {
            entityMaterial = clamp(tag.getDouble(ENTITY_KEY));
        }
    }

    private double clamp(double value) {
        return Math.max(0.0D, value);
    }
}
