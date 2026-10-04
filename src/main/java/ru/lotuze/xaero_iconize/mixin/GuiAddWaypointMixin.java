package ru.lotuze.xaero_iconize.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.lotuze.xaero_iconize.XaeroIconize;
import ru.lotuze.xaero_iconize.client.gui.IconSelectButton;
import ru.lotuze.xaero_iconize.client.gui.ItemIconPickerPopup;
import ru.lotuze.xaero_iconize.client.storage.WaypointIconKey;
import ru.lotuze.xaero_iconize.client.storage.WaypointIconStorage;
import xaero.common.gui.GuiAddWaypoint;
import xaero.common.gui.GuiWaypointSets;
import xaero.common.gui.GuiWaypointWorlds;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.lib.client.config.ClientConfigManager;

import java.util.List;
import java.util.ArrayList;

@Mixin(value = GuiAddWaypoint.class, remap = false)
public class GuiAddWaypointMixin extends Screen {

    @Shadow
    private EditBox nameTextField;

    @Unique
    private ItemStack xaeroIconize$selectedIcon = ItemStack.EMPTY;

    @Unique
    private IconSelectButton xaeroIconize$iconButton;

    @Unique
    private ItemIconPickerPopup xaeroIconize$picker;

    @Shadow
    private ArrayList<Waypoint> waypointsEdited;

    @Shadow
    private MinimapWorld defaultWorld;

    @Shadow
    private String fromSet;

    @Shadow
    private boolean adding;

    @Unique
    private boolean xaeroIconize$initialIconLoaded = false;

    @Shadow
    private GuiWaypointWorlds worlds;

    @Shadow
    private GuiWaypointSets sets;

    @Shadow
    private MinimapWorldManager manager;

    @Unique
    private WaypointIconKey xaeroIconize$oldKey;

    @Unique
    private ResourceLocation xaeroIconize$oldIcon;

    @Unique
    private boolean xaeroIconize$iconCleared = false;

    double rawMouseX = Minecraft.getInstance().mouseHandler.xpos() * this.width / Minecraft.getInstance().getWindow().getScreenWidth();
    double rawMouseY = Minecraft.getInstance().mouseHandler.ypos() * this.height / Minecraft.getInstance().getWindow().getScreenHeight();

    protected GuiAddWaypointMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void xaeroIconize$addIconButton(CallbackInfo ci) {

        if (this.xaeroIconize$picker == null) {
            List<Item> items = BuiltInRegistries.ITEM.stream().toList();

            this.xaeroIconize$picker = new ItemIconPickerPopup(
                        items,
                        stack -> {
                            this.xaeroIconize$selectedIcon = stack.copy();
                            this.xaeroIconize$iconCleared = false;

                            if (this.xaeroIconize$iconButton != null) {
                                this.xaeroIconize$iconButton.setIcon(stack);
                            }

                            XaeroIconize.LOGGER.info("Selected icon: {}", stack.getHoverName().getString());
                        }
                );
        }

        int buttonSize = 20;
        int gap = 4;

        int x = this.nameTextField.getX() - buttonSize - gap;
        int y = this.nameTextField.getY();

        this.xaeroIconize$iconButton =
                new IconSelectButton(
                        x,
                        y,
                        buttonSize,
                        buttonSize,
                        pressed -> {
                            int popupX = this.nameTextField.getX();
                            int popupY = this.nameTextField.getY() + this.nameTextField.getHeight() + 4;

                            this.xaeroIconize$picker.open(popupX, popupY);
                        },

                        () -> {
                            this.xaeroIconize$selectedIcon = ItemStack.EMPTY;
                            this.xaeroIconize$iconCleared = true;

                            if (this.xaeroIconize$iconButton != null) {
                                this.xaeroIconize$iconButton.setIcon(ItemStack.EMPTY);
                            }
                        }
                );

        this.addRenderableWidget(this.xaeroIconize$iconButton);

        if (!this.xaeroIconize$selectedIcon.isEmpty()) {
            this.xaeroIconize$iconButton.setIcon(this.xaeroIconize$selectedIcon);
        }

        this.xaeroIconize$loadExistingIcon();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void xaeroIconize$renderPicker(
            GuiGraphics graphics,
            int ignoredMouseX,
            int ignoredMouseY,
            float partialTick,
            CallbackInfo ci
    ) {
        if (this.xaeroIconize$picker == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        int mouseX = (int) (mc.mouseHandler.xpos() * this.width / mc.getWindow().getScreenWidth());
        int mouseY = (int) (mc.mouseHandler.ypos() * this.height / mc.getWindow().getScreenHeight());

        this.xaeroIconize$picker.render(graphics, mouseX, mouseY, partialTick);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void xaeroIconize$mouseClicked(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this.xaeroIconize$picker != null
                && this.xaeroIconize$picker.isOpen()
        ) {
            this.xaeroIconize$picker.mouseClicked(mouseX, mouseY, button);
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {

        if (this.xaeroIconize$picker != null
                && this.xaeroIconize$picker.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        ) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void xaeroIconize$keyPressed(
            int keyCode,
            int scanCode,
            int modifiers,
            CallbackInfoReturnable<Boolean> cir
    ) {

        if (this.xaeroIconize$picker != null
                && this.xaeroIconize$picker.keyPressed(keyCode, scanCode, modifiers)
        ) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void xaeroIconize$charTyped(char codePoint, int modifiers, CallbackInfoReturnable<Boolean> cir) {

        if (this.xaeroIconize$picker != null
                && this.xaeroIconize$picker.charTyped(codePoint, modifiers)
        ) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private int xaeroIconize$effectiveMouseX(int mouseX) {
        return this.xaeroIconize$picker != null && this.xaeroIconize$picker.isOpen()
                ? -10000
                : mouseX;
    }

    @Unique
    private int xaeroIconize$effectiveMouseY(int mouseY) {
        return this.xaeroIconize$picker != null && this.xaeroIconize$picker.isOpen()
                ? -10000
                : mouseY;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int xaeroIconize$hideMouseXFromXaero(int mouseX) {

        if (this.xaeroIconize$picker != null && this.xaeroIconize$picker.isOpen()) {
            return -10000;
        }

        return mouseX;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int xaeroIconize$hideMouseYFromXaero(int mouseY) {

        if (this.xaeroIconize$picker != null && this.xaeroIconize$picker.isOpen()) {
            return -10000;
        }

        return mouseY;
    }

    @Unique
    private void xaeroIconize$loadExistingIcon() {

        if (this.xaeroIconize$initialIconLoaded) {
            return;
        }

        this.xaeroIconize$initialIconLoaded = true;

        if (this.adding) {
            return;
        }

        if (this.waypointsEdited == null || this.waypointsEdited.size() != 1) {
            return;
        }

        if (this.defaultWorld == null) {
            return;
        }

        Waypoint waypoint = this.waypointsEdited.getFirst();
        String setId = this.fromSet;

        if (setId == null || setId.isBlank()) {
            setId = this.defaultWorld.getCurrentWaypointSetId();
        }

        WaypointIconKey key = WaypointIconKey.from(this.defaultWorld, setId, waypoint);

        ResourceLocation itemId = WaypointIconStorage.getIcon(key);

        if (itemId == null) {
            return;
        }

        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            XaeroIconize.LOGGER.warn("Waypoint icon item '{}' is not registered", itemId);
            return;
        }

        Item item = BuiltInRegistries.ITEM.get(itemId);
        ItemStack stack = new ItemStack(item);

        this.xaeroIconize$selectedIcon = stack;
        this.xaeroIconize$iconCleared = false;

        if (this.xaeroIconize$iconButton != null) {
            this.xaeroIconize$iconButton.setIcon(stack);
        }
    }

    @Inject(method = "lambda$init$3", at = @At("HEAD"))
    private void xaeroIconize$beforeConfirm(ClientConfigManager configManager, Button button, CallbackInfo ci) {
        this.xaeroIconize$oldKey = null;
        this.xaeroIconize$oldIcon = null;

        if (this.adding) {
            return;
        }

        if (this.waypointsEdited == null || this.waypointsEdited.size() != 1) {
            return;
        }

        if (this.defaultWorld == null) {
            return;
        }

        Waypoint waypoint = this.waypointsEdited.getFirst();

        String setId = this.fromSet;

        if (setId == null || setId.isBlank()) {
            setId = this.defaultWorld.getCurrentWaypointSetId();
        }

        this.xaeroIconize$oldKey = WaypointIconKey.from(this.defaultWorld, setId, waypoint);
        this.xaeroIconize$oldIcon = WaypointIconStorage.getIcon(this.xaeroIconize$oldKey);
    }

    @Inject(method = "lambda$init$3", at = @At("TAIL"))
    private void xaeroIconize$afterConfirm(ClientConfigManager configManager, Button button, CallbackInfo ci) {
        if (this.waypointsEdited == null || this.waypointsEdited.size() != 1) {
            return;
        }

        if (this.worlds == null || this.sets == null || this.manager == null) {
            return;
        }

        MinimapWorld currentWorld = this.manager.getWorld(this.worlds.getCurrentKey());

        if (currentWorld == null) {
            return;
        }

        String currentSetId = this.sets.getCurrentSetKey();

        if (currentSetId == null || currentSetId.isBlank()) {
            return;
        }

        Waypoint waypoint = this.waypointsEdited.getFirst();
        WaypointIconKey newKey = WaypointIconKey.from(currentWorld, currentSetId, waypoint);

        if (this.xaeroIconize$iconCleared) {

            if (this.xaeroIconize$oldKey != null) {
                WaypointIconStorage.removeIcon(this.xaeroIconize$oldKey);
            }

            WaypointIconStorage.removeIcon(newKey);

            this.xaeroIconize$oldKey = null;
            this.xaeroIconize$oldIcon = null;

            return;
        }

        ResourceLocation iconId = null;

        if (!this.xaeroIconize$selectedIcon.isEmpty()) {
            iconId = BuiltInRegistries.ITEM.getKey(this.xaeroIconize$selectedIcon.getItem());
        } else if (this.xaeroIconize$oldIcon != null) {
            iconId = this.xaeroIconize$oldIcon;
        }

        if (this.xaeroIconize$oldKey != null && !this.xaeroIconize$oldKey.equals(newKey)) {
            WaypointIconStorage.removeIcon(this.xaeroIconize$oldKey);
        }

        if (iconId != null) {
            WaypointIconStorage.setIcon(newKey, iconId);
        }
    }
}
