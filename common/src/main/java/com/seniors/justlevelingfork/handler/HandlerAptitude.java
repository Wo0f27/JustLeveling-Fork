package com.seniors.justlevelingfork.handler;

import com.seniors.justlevelingfork.Constants;
import com.seniors.justlevelingfork.client.core.Aptitudes;
import com.seniors.justlevelingfork.common.config.CommonConfigService;
import com.seniors.justlevelingfork.common.player.AptitudeWarningService;
import com.seniors.justlevelingfork.common.player.PlayerProgressService;
import com.seniors.justlevelingfork.config.conditions.AptitudeLevelProvider;
import com.seniors.justlevelingfork.config.models.LockItem;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class HandlerAptitude {
    private static Supplier<List<LockItem>> lockItems = List::of;
    private static Map<String, List<Aptitudes>> aptitudes;

    private HandlerAptitude() {
    }

    public static void setLockItems(Supplier<List<LockItem>> lockItems) {
        HandlerAptitude.lockItems = Optional.ofNullable(lockItems).orElse(List::of);
        forceRefresh();
    }

    public static void updateLockItems(List<LockItem> lockItems) {
        aptitudes = mapLockItems(lockItems);
    }

    public static void forceRefresh() {
        aptitudes = mapLockItems(lockItems.get());
    }

    public static List<Aptitudes> getValue(String key) {
        if (aptitudes == null) {
            forceRefresh();
        }

        return aptitudes.get(key);
    }

    public static boolean canUseItem(ServerPlayer player, ItemStack stack, AptitudeLevelProvider aptitudeLevelProvider) {
        if (stack.isEmpty()) {
            return true;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return itemId == null || canUseItem(player, itemId, aptitudeLevelProvider);
    }

    public static boolean canUseItem(ServerPlayer player, ItemStack stack) {
        return canUseItem(player, stack, PlayerProgressService.aptitudeLevelProvider());
    }

    public static boolean canUseItem(
            ServerPlayer player, ResourceLocation itemId, AptitudeLevelProvider aptitudeLevelProvider) {
        return canUse(player, itemId.toString(), aptitudeLevelProvider);
    }

    public static boolean canUseItem(ServerPlayer player, ResourceLocation itemId) {
        return canUseItem(player, itemId, PlayerProgressService.aptitudeLevelProvider());
    }

    public static boolean canUseBlock(ServerPlayer player, Block block, AptitudeLevelProvider aptitudeLevelProvider) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        return blockId == null || canUse(player, blockId.toString(), aptitudeLevelProvider);
    }

    public static boolean canUseBlock(ServerPlayer player, Block block) {
        return canUseBlock(player, block, PlayerProgressService.aptitudeLevelProvider());
    }

    public static boolean canUseEntity(ServerPlayer player, Entity entity, AptitudeLevelProvider aptitudeLevelProvider) {
        ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return entityTypeId == null || canUse(player, entityTypeId.toString(), aptitudeLevelProvider);
    }

    public static boolean canUseEntity(ServerPlayer player, Entity entity) {
        return canUseEntity(player, entity, PlayerProgressService.aptitudeLevelProvider());
    }

    public static boolean canUseSpecificId(
            ServerPlayer player, String restrictionId, AptitudeLevelProvider aptitudeLevelProvider) {
        return canUse(player, restrictionId, aptitudeLevelProvider);
    }

    public static boolean canUseSpecificId(ServerPlayer player, String restrictionId) {
        return canUseSpecificId(player, restrictionId, PlayerProgressService.aptitudeLevelProvider());
    }

    private static boolean canUse(
            ServerPlayer player, String restrictionId, AptitudeLevelProvider aptitudeLevelProvider) {
        List<Aptitudes> requirements = getValue(restrictionId);
        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        for (Aptitudes requirement : requirements) {
            if (aptitudeLevelProvider.getAptitudeLevel(player, requirement.getKey()) < requirement.getAptitudeLvl()) {
                AptitudeWarningService.send(player, restrictionId);
                return false;
            }
        }

        return true;
    }

    public static boolean shouldDropLockedItem(
            ServerPlayer player, ItemStack stack, AptitudeLevelProvider aptitudeLevelProvider) {
        if (stack.isEmpty() || CommonConfigService.allowLockedItemsInInventory()) {
            return false;
        }

        if (canUseItem(player, stack, aptitudeLevelProvider)) {
            return false;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) {
            return false;
        }

        List<Aptitudes> requirements = getValue(itemId.toString());
        return shouldDropLockedItem(
                true,
                false,
                CommonConfigService.dropLockedItems(),
                requirements != null && requirements.stream().anyMatch(Aptitudes::isDroppable));
    }

    public static boolean shouldDropLockedItem(ServerPlayer player, ItemStack stack) {
        return shouldDropLockedItem(player, stack, PlayerProgressService.aptitudeLevelProvider());
    }

    static boolean shouldDropLockedItem(
            boolean locked, boolean allowInInventory, boolean dropAllLockedItems, boolean droppable) {
        return locked && !allowInInventory && (dropAllLockedItems || droppable);
    }

    public static Map<String, List<Aptitudes>> mapLockItems(List<LockItem> lockItems) {
        Map<String, List<Aptitudes>> aptitudeMap = new HashMap<>();

        for (LockItem lockItem : lockItems) {
            if (lockItem == null || lockItem.Item == null || lockItem.Item.isBlank() || lockItem.Aptitudes == null) {
                Constants.LOG.warn("Ignoring malformed lock-item config entry.");
                continue;
            }
            List<Aptitudes> aptitudesList = new ArrayList<>();
            for (LockItem.Aptitude aptitude : lockItem.Aptitudes) {
                if (aptitude == null || aptitude.Aptitude == null) {
                    Constants.LOG.warn("Item {} with wrong aptitude (APTITUDE NOT FOUND), skipping...", lockItem.Item);
                    continue;
                }

                Aptitude aptitudeName = RegistryAptitudes.getAptitude(aptitude.Aptitude.toString());
                if (aptitudeName == null) {
                    Constants.LOG.warn(
                            "Item {} with wrong aptitude (APTITUDE \"{}\" NOT FOUND), skipping...",
                            lockItem.Item,
                            aptitude.Aptitude);
                    continue;
                }

                aptitudesList.add(new Aptitudes(
                        aptitudeName.getName(),
                        lockItem.Item,
                        lockItem.Droppable,
                        aptitudeName,
                        aptitude.Level));
            }

            if (aptitudesList.isEmpty()) {
                Constants.LOG.warn("Item {} with no aptitudes (ITEM WITH NO APTITUDES), skipping...", lockItem.Item);
                continue;
            }

            aptitudeMap.put(lockItem.Item, aptitudesList);
        }

        return aptitudeMap;
    }
}
