package ru.lotuze.xaero_iconize.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.lotuze.xaero_iconize.client.storage.WaypointIconKey;
import ru.lotuze.xaero_iconize.client.storage.WaypointIconStorage;
import xaero.common.gui.GuiWaypoints;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.world.MinimapWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Mixin(
        value = GuiWaypoints.class,
        remap = false
)
public abstract class GuiWaypointsMixin {

    @Shadow
    private MinimapWorld displayedWorld;

    @Shadow
    protected abstract ArrayList<Waypoint> getSelectedWaypointsList();

    /*
     * Иконки точек, которые пользователь пометил на удаление.
     *
     * Нужны потому, что повторное нажатие Delete в Xaero
     * отменяет удаление.
     */
    @Unique
    private final Map<Waypoint, ResourceLocation>
            xaeroIconize$deletedIcons = new HashMap<>();

    @Inject(
            method = "lambda$init$0",
            at = @At("TAIL")
    )
    private void xaeroIconize$afterDeleteToggle(
            Button button,
            CallbackInfo ci
    ) {
        if (this.displayedWorld == null) {
            return;
        }

        String setId =
                this.displayedWorld.getCurrentWaypointSetId();

        if (setId == null) {
            return;
        }

        for (Waypoint waypoint : this.getSelectedWaypointsList()) {
            WaypointIconKey key =
                    WaypointIconKey.from(
                            this.displayedWorld,
                            setId,
                            waypoint
                    );

            /*
             * После оригинальной lambda состояние temporary
             * уже переключено.
             */
            if (waypoint.isTemporary()) {

                ResourceLocation icon =
                        WaypointIconStorage.getIcon(key);

                if (icon != null) {
                    this.xaeroIconize$deletedIcons.put(
                            waypoint,
                            icon
                    );

                    WaypointIconStorage.removeIcon(key);
                }

            } else {

                /*
                 * Пользователь нажал Delete повторно:
                 * Xaero отменил удаление.
                 */
                ResourceLocation icon =
                        this.xaeroIconize$deletedIcons.remove(
                                waypoint
                        );

                if (icon != null) {
                    WaypointIconStorage.setIcon(
                            key,
                            icon
                    );
                }
            }
        }
    }
}