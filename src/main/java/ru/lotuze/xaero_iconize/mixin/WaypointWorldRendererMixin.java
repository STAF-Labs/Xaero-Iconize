package ru.lotuze.xaero_iconize.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.lotuze.xaero_iconize.client.render.WaypointIconResolver;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.waypoint.render.world.WaypointWorldRenderer;

@Mixin(value = WaypointWorldRenderer.class, remap = false)
public abstract class WaypointWorldRendererMixin {

    @Shadow
    protected int opacity;

    @Unique
    private GuiGraphics xaeroIconize$graphics;

    @Inject(
            method = "renderElement(Lxaero/common/minimap/waypoints/Waypoint;ZZDFDDLxaero/hud/minimap/element/render/MinimapElementRenderInfo;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)Z",
            at = @At("HEAD")
    )
    private void xaeroIconize$captureGraphics(
            Waypoint waypoint,
            boolean arg2,
            boolean arg3,
            double arg4,
            float arg6,
            double arg7,
            double arg9,
            MinimapElementRenderInfo renderInfo,
            GuiGraphics graphics,
            MultiBufferSource.BufferSource bufferSource,
            CallbackInfoReturnable<Boolean> cir
    ) {
        this.xaeroIconize$graphics = graphics;
    }

    @Inject(method = "renderIcon", at = @At("HEAD"), cancellable = true)
    private void xaeroIconize$renderIcon(
            Waypoint waypoint,
            boolean highlighted,
            PoseStack poseStack,
            Font font,
            MultiBufferSource.BufferSource bufferSource,
            CallbackInfo ci
    ) {
        ItemStack stack = WaypointIconResolver.resolve(waypoint);

        if (stack.isEmpty() || this.xaeroIconize$graphics == null) {
            return;
        }

        GuiGraphics graphics = this.xaeroIconize$graphics;

        int rgb = waypoint.getWaypointColor().getHex();
        int alpha = Math.round(255.0F * 0.52274513F * (this.opacity / 100.0F));
        int backgroundColor = (alpha << 24) | (rgb & 0x00FFFFFF);

        graphics.fill(-5, -9, 4, 0, backgroundColor);

        poseStack.pushPose();
        poseStack.translate(-4.5F, -8.5F, 1.0F);
        poseStack.scale(0.5F, 0.5F, 1.0F);

        graphics.renderItem(stack, 0, 0);

        poseStack.popPose();

        ci.cancel();
    }
}