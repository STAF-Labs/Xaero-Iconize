package ru.lotuze.xaero_iconize.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class IconSelectButton extends Button {

    private ItemStack icon = ItemStack.EMPTY;
    private final Runnable onClear;

    public IconSelectButton(
            int x,
            int y,
            int width,
            int height,
            OnPress onPress,
            Runnable onClear
    ) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);

        this.onClear = onClear;
    }

    public void setIcon(ItemStack stack) {
        this.icon = stack.copy();
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (!this.icon.isEmpty()) {
            int itemX = this.getX() + (this.getWidth() - 16) / 2;
            int itemY = this.getY() + (this.getHeight() - 16) / 2;

            graphics.renderItem(this.icon, itemX, itemY);
        } else {
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    "+",
                    this.getX() + this.getWidth() / 2,
                    this.getY() + (this.getHeight() - 8) / 2,
                    0xFFFFFF
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) {
            return false;
        }

        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        if (button == 1) {
            if (this.onClear != null) {
                this.onClear.run();
            }

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
