package ru.lotuze.xaero_iconize.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.lotuze.xaero_iconize.client.render.WaypointIconResolver;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.map.icon.XaeroIcon;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointRenderer;
import xaero.map.mods.gui.WaypointSymbolCreator;
import xaero.map.element.render.ElementRenderInfo;

import net.minecraft.client.renderer.MultiBufferSource;

@Mixin(value = WaypointRenderer.class, remap = false)
public abstract class WaypointRendererMixin {

    @Unique
    private Waypoint xaeroIconize$currentWaypoint;

    @Inject(
            method = "renderElement(Lxaero/map/mods/gui/Waypoint;ZDFDDLxaero/map/element/render/ElementRenderInfo;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;)Z",
            at = @At("HEAD")
    )
    private void xaeroIconize$captureWaypoint(
            Waypoint waypoint,
            boolean highlighted,
            double arg3,
            float scale,
            double x,
            double y,
            ElementRenderInfo renderInfo,
            GuiGraphics graphics,
            MultiBufferSource.BufferSource bufferSource,
            MultiTextureRenderTypeRendererProvider provider,
            CallbackInfoReturnable<Boolean> cir
    ) {
        this.xaeroIconize$currentWaypoint = waypoint;
    }

    @Redirect(
            method = "renderElement(Lxaero/map/mods/gui/Waypoint;ZDFDDLxaero/map/element/render/ElementRenderInfo;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lxaero/map/mods/gui/WaypointSymbolCreator;getSymbolTexture(Lnet/minecraft/client/gui/GuiGraphics;Ljava/lang/String;)Lxaero/map/icon/XaeroIcon;"
            )
    )
    private XaeroIcon xaeroIconize$replaceSymbol(WaypointSymbolCreator symbolCreator, GuiGraphics graphics, String symbol) {
        Waypoint waypoint = this.xaeroIconize$currentWaypoint;

        if (waypoint == null) {
            return symbolCreator.getSymbolTexture(graphics, symbol);
        }

        ItemStack stack = WaypointIconResolver.resolve(waypoint);

        if (stack.isEmpty()) {
            return symbolCreator.getSymbolTexture(graphics, symbol);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(-12.8F, -39.0F, 100.0F);
        graphics.pose().scale(1.6F, 1.6F, 1.0F);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();

        return null;
    }
}