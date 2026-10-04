package ru.lotuze.xaero_iconize.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.lotuze.xaero_iconize.client.render.WaypointIconResolver;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.render.WaypointMapRenderer;

@Mixin(value = WaypointMapRenderer.class, remap = false)
public abstract class WaypointMapRendererMixin {

    @Inject(
            method = "drawIconOnGUI(Lnet/minecraft/client/gui/GuiGraphics;Lxaero/common/minimap/render/MinimapRendererHelper;Lxaero/common/minimap/waypoints/Waypoint;IIILnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lxaero/common/misc/Misc;drawNormalText(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;FFIZLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"
            ),
            cancellable = true
    )
    private void xaeroIconize$renderItemIcon(
            GuiGraphics graphics,
            MinimapRendererHelper helper,
            Waypoint waypoint,
            int x,
            int y,
            int opacity,
            MultiBufferSource.BufferSource bufferSource,
            com.mojang.blaze3d.vertex.VertexConsumer backgroundConsumer,
            com.mojang.blaze3d.vertex.VertexConsumer texturedConsumer,
            CallbackInfo ci
    ) {
        ItemStack stack = WaypointIconResolver.resolve(waypoint);

        if (stack.isEmpty()) {
            return;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(x - 3.5F, y - 4.5F, 100.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);

        graphics.renderItem(stack, 0, 0);

        graphics.pose().popPose();

        ci.cancel();
    }
}