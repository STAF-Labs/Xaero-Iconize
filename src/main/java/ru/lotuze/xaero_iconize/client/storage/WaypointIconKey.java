package ru.lotuze.xaero_iconize.client.storage;

import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.world.MinimapWorld;

public record WaypointIconKey(
        String worldPath,
        String setId,
        String name,
        String initials,
        int x,
        int y,
        int z,
        String purpose
) {

    public static WaypointIconKey from(
            MinimapWorld world,
            String setId,
            Waypoint waypoint
    ) {
        return new WaypointIconKey(
                world.getFullPath().toString(),
                setId,
                waypoint.getName(),
                waypoint.getInitials(),
                waypoint.getX(),
                waypoint.getY(),
                waypoint.getZ(),
                waypoint.getPurpose().name()
        );
    }

    public String serialize() {
        return escape(worldPath())
                + "|" + escape(setId())
                + "|" + escape(name())
                + "|" + escape(initials())
                + "|" + x()
                + "|" + y()
                + "|" + z()
                + "|" + escape(purpose());
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\").replace("|", "\\|");
    }
}