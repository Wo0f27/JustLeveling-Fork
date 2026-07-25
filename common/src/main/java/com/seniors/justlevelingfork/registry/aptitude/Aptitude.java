package com.seniors.justlevelingfork.registry.aptitude;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class Aptitude {
    public final int index;
    public final ResourceLocation key;
    public final ResourceLocation[] lockedTexture;
    public final ResourceLocation background;

    public Aptitude(int index, ResourceLocation key, ResourceLocation[] lockedTexture, ResourceLocation background) {
        this.index = index;
        this.key = key;
        this.lockedTexture = lockedTexture;
        this.background = background;
    }

    public Aptitude get() {
        return this;
    }

    public String getName() {
        return this.key.getPath();
    }

    public String getKey() {
        return "aptitude." + this.key.toLanguageKey();
    }

    public String getDescription() {
        return getKey() + ".description";
    }

    public MutableComponent getRank(int aptitudeLevel, int aptitudeMaxLevel) {
        MutableComponent rank = Component.translatable("aptitude.justlevelingfork.rank.0");
        for (int i = 0; i < 9; i++) {
            if (aptitudeLevel >= (aptitudeMaxLevel / 8) * i) {
                rank = Component.translatable("aptitude.justlevelingfork.rank." + i);
            }
        }
        return rank;
    }

    public ResourceLocation getLockedTexture(int aptitudeLevel, int aptitudeMaxLevel) {
        int textureListSize = this.lockedTexture.length;
        int clampedLevel = Math.min(aptitudeLevel, aptitudeMaxLevel);
        int textureIndex = Math.floorDiv(clampedLevel * textureListSize, aptitudeMaxLevel);
        textureIndex = textureIndex == textureListSize ? textureIndex - 1 : textureIndex;

        if (textureIndex >= textureListSize) {
            textureIndex = textureListSize - 1;
        }

        return this.lockedTexture[textureIndex];
    }

    public ResourceLocation getLockedTextureForRequirement(int fromLevel, int aptitudeMaxLevel) {
        int textureListSize = this.lockedTexture.length;
        int textureIndex = Math.floorDiv(fromLevel * textureListSize, aptitudeMaxLevel);
        textureIndex = textureIndex == textureListSize ? textureIndex - 1 : textureIndex;

        if (textureIndex >= textureListSize) {
            textureIndex = textureListSize - 1;
        }

        return this.lockedTexture[textureIndex];
    }
}
