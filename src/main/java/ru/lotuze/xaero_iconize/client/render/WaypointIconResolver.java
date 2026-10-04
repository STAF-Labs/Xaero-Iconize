package ru.lotuze.xaero_iconize.client.render;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ru.lotuze.xaero_iconize.client.storage.WaypointIconKey;
import ru.lotuze.xaero_iconize.client.storage.WaypointIconStorage;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorld;

public final class WaypointIconResolver {

    private WaypointIconResolver() {}

    public static ItemStack resolve(Waypoint waypoint) {
        MinimapSession session = (MinimapSession) BuiltInHudModules.MINIMAP.getCurrentSession();

        if (session == null) {
            return ItemStack.EMPTY;
        }

        MinimapWorld world = session.getWorldManager().getCurrentWorld();

        if (world == null) {
            return ItemStack.EMPTY;
        }

        String setId = world.getCurrentWaypointSetId();

        if (setId == null) {
            return ItemStack.EMPTY;
        }

        WaypointIconKey key = WaypointIconKey.from(world, setId, waypoint);
        ResourceLocation itemId = WaypointIconStorage.getIcon(key);

        if (itemId == null) {
            return ItemStack.EMPTY;
        }

        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            return ItemStack.EMPTY;
        }

        Item item = BuiltInRegistries.ITEM.get(itemId);

        return new ItemStack(item);
    }

    public static ItemStack resolve(xaero.map.mods.gui.Waypoint mapWaypoint) {
        Object original = mapWaypoint.getOriginal();

        if (!(original instanceof Waypoint waypoint)) {
            return ItemStack.EMPTY;
        }

        MinimapSession session = (MinimapSession) BuiltInHudModules.MINIMAP.getCurrentSession();

        if (session == null) {
            return ItemStack.EMPTY;
        }

        MinimapWorld world = session.getWorldManager().getCurrentWorld();

        if (world == null) {
            return ItemStack.EMPTY;
        }

        String setId = mapWaypoint.getSetName();

        if (setId == null || setId.isBlank()) {
            setId = world.getCurrentWaypointSetId();
        }

        WaypointIconKey key = WaypointIconKey.from(world, setId, waypoint);

        ResourceLocation itemId = WaypointIconStorage.getIcon(key);

        if (itemId == null) {
            return ItemStack.EMPTY;
        }

        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(BuiltInRegistries.ITEM.get(itemId));
    }
}