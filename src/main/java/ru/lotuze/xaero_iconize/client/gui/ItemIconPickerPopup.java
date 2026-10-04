package ru.lotuze.xaero_iconize.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemIconPickerPopup {

    private static final int WIDTH = 190;
    private static final int HEIGHT = 150;

    private static final int PADDING = 8;
    private static final int SLOT_SIZE = 20;

    private static final int SEARCH_HEIGHT = 20;

    private final Minecraft minecraft;
    private final Consumer<ItemStack> onSelect;

    private final List<Item> allItems;
    private List<Item> filteredItems;

    private EditBox searchBox;

    private int x;
    private int y;

    private int scrollRow = 0;

    private boolean open = false;

    public ItemIconPickerPopup(List<Item> items, Consumer<ItemStack> onSelect) {
        this.minecraft = Minecraft.getInstance();
        this.onSelect = onSelect;

        this.allItems = items.stream().filter(item -> item != Items.AIR).toList();
        this.filteredItems = new ArrayList<>(this.allItems);
    }

    public void open(int x, int y) {
        this.x = x;
        this.y = y;
        this.open = true;

        this.searchBox = new EditBox(
                minecraft.font,
                x + PADDING,
                y + PADDING,
                WIDTH - PADDING * 2,
                SEARCH_HEIGHT,
                Component.literal("Search")
        );

        this.searchBox.setHint(Component.literal("Search..."));
        this.searchBox.setResponder(this::applySearch);
        this.searchBox.setValue("");
        this.searchBox.setFocused(true);

        this.filteredItems = new ArrayList<>(this.allItems);
        this.scrollRow = 0;
    }

    public void close() {
        this.open = false;
    }

    public boolean isOpen() {
        return open;
    }

    private void applySearch(String text) {
        String query = text.toLowerCase();

        if (query.isBlank()) {
            this.filteredItems = new ArrayList<>(allItems);
        } else {
            this.filteredItems = allItems.stream()
                    .filter(item -> {
                        ItemStack stack = new ItemStack(item);
                        String name = stack.getHoverName().getString().toLowerCase();

                        return name.contains(query);

                    }).toList();
        }

        this.scrollRow = 0;
    }

    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (!open) {
            return;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 500.0F);

        renderBackground(graphics);

        this.searchBox.render(graphics, mouseX, mouseY, partialTick);

        renderItems(graphics, mouseX, mouseY);

        graphics.pose().popPose();
    }

    private void renderBackground(GuiGraphics graphics) {

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xEE202020);
        graphics.fill(x, y, x + WIDTH, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0xFFFFFFFF);
        graphics.fill(x, y, x + 1, y + HEIGHT, 0xFFFFFFFF);
        graphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, 0xFFFFFFFF);
    }

    private void renderItems(GuiGraphics graphics, int mouseX, int mouseY) {
        int contentX = x + PADDING;
        int contentY = y + PADDING + SEARCH_HEIGHT + PADDING;

        int contentWidth = WIDTH - PADDING * 2;
        int contentHeight = HEIGHT - (PADDING * 3) - SEARCH_HEIGHT;

        int columns = Math.max(1, contentWidth / SLOT_SIZE);
        int visibleRows = Math.max(1, contentHeight / SLOT_SIZE);

        int startIndex = scrollRow * columns;
        int endIndex = Math.min(filteredItems.size(), startIndex + columns * visibleRows);

        ItemStack hoveredStack = ItemStack.EMPTY;

        for (int i = startIndex; i < endIndex; i++) {
            int localIndex = i - startIndex;

            int column = localIndex % columns;
            int row = localIndex / columns;

            int slotX = contentX + column * SLOT_SIZE;
            int slotY = contentY + row * SLOT_SIZE;

            ItemStack stack = new ItemStack(filteredItems.get(i));

            boolean hovered = mouseX >= slotX
                    && mouseX < slotX + SLOT_SIZE
                    && mouseY >= slotY
                    && mouseY < slotY + SLOT_SIZE;

            if (hovered) {
                graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x55FFFFFF);

                hoveredStack = stack;
            }

            graphics.renderItem(stack, slotX + 2, slotY + 2);
        }

        if (!hoveredStack.isEmpty()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 500.0F);

            graphics.renderTooltip(minecraft.font, hoveredStack, mouseX, mouseY);

            graphics.pose().popPose();
        }
    }

    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (!open) {
            return false;
        }

        if (searchBox.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (!isInside(mouseX, mouseY)) {
            close();
            return true;
        }

        int contentX = x + PADDING;
        int contentY = y + PADDING + SEARCH_HEIGHT + PADDING;
        int contentWidth = WIDTH - PADDING * 2;

        int columns = Math.max(1, contentWidth / SLOT_SIZE);

        if (mouseY >= contentY) {
            int column = (int) (mouseX - contentX) / SLOT_SIZE;
            int row = (int) (mouseY - contentY) / SLOT_SIZE;

            if (column >= 0 && column < columns && row >= 0) {
                int index = (scrollRow + row) * columns + column;

                if (index >= 0 && index < filteredItems.size()) {
                    ItemStack selected = new ItemStack(filteredItems.get(index));

                    onSelect.accept(selected);
                    close();
                    return true;
                }
            }
        }

        return true;
    }

    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        if (!open || !isInside(mouseX, mouseY)) {
            return false;
        }

        int contentWidth = WIDTH - PADDING * 2;
        int columns = Math.max(1, contentWidth / SLOT_SIZE);

        int totalRows = (int) Math.ceil(filteredItems.size() / (double) columns);
        int contentHeight = HEIGHT - (PADDING * 3) - SEARCH_HEIGHT;
        int visibleRows = Math.max(1, contentHeight / SLOT_SIZE);
        int maxScrollRow = Math.max(0, totalRows - visibleRows);

        if (verticalAmount < 0) {
            scrollRow++;
        } else if (verticalAmount > 0) {
            scrollRow--;
        }

        scrollRow = Math.max(0, Math.min(scrollRow, maxScrollRow));

        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!open) {
            return false;
        }

        if (keyCode == 256) {
            close();
            return true;
        }

        return searchBox.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!open) {
            return false;
        }

        return searchBox.charTyped(codePoint, modifiers);
    }

    private boolean isInside(double mouseX, double mouseY) {
        return mouseX >= x
                && mouseX <= x + WIDTH
                && mouseY >= y
                && mouseY <= y + HEIGHT;
    }
}