package com.sk89q.worldedit.fabric.gui.screens;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.PlatformCommandManager;
import com.sk89q.worldedit.fabric.data.WorldEditData;
import com.sk89q.worldedit.fabric.gui.components.ScrollBar;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldEditShortcutSelectionScreen extends Screen implements GuiEventListener {

    private static final Component TITLE = Component.translatable("Select Command to bind");
    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_PADDING = 5;
    private static final int SCROLLBAR_WIDTH = 12;

    private final Map<String, String> commandDescriptions = new HashMap<>();

    private EditBox searchField;
    private List<Map.Entry<String, String>> filteredCommands;
    private ScrollBar scrollBar;

    private int shortcutIndex;

    public WorldEditShortcutSelectionScreen(int i) {
        super(GameNarrator.NO_TITLE);
        this.shortcutIndex = i;
        loadCommands();
    }

    @Override
    protected void init() {
        initializeSearchField();
        initializeScrollBar();
        initializeCommandButtons();
        updateButtonPositions();
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTitleAndLabels(pGuiGraphics);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrollBar.isMouseOver(mouseX, mouseY) && Math.abs(deltaY) > 0) {
            scrollBar.handleMouseScroll(deltaY > 0 ? -1 : 1);
            updateButtonPositions();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    private void loadCommands() {
        PlatformCommandManager commandManager = WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager();
        commandManager.getCommandManager().getAllCommands().forEach(cmd -> {
            String commandName = cmd.getName();
            String description = String.valueOf(cmd.getDescription()).split("=")[1].split(",")[0];
            commandDescriptions.put(commandName, description);
        });
    }

    private void initializeSearchField() {
        int searchFieldWidth = this.width - 120;
        int searchFieldHeight = 18;
        int searchFieldX = (BUTTON_WIDTH + BUTTON_PADDING) / 2 + (this.width - (4 * (BUTTON_WIDTH + BUTTON_PADDING) + SCROLLBAR_WIDTH)) / 2;
        int searchFieldY = 20;

        searchField = new EditBox(this.font, searchFieldX, searchFieldY, searchFieldWidth, searchFieldHeight, Component.literal("Search..."));
        searchField.setResponder(this::onSearchUpdated);
        this.addRenderableWidget(searchField);
    }

    private void initializeScrollBar() {
        int rows = (int) Math.ceil((double) commandDescriptions.size() / 4);
        int totalButtonHeight = rows * (BUTTON_HEIGHT + BUTTON_PADDING);
        scrollBar = new ScrollBar(this.width - SCROLLBAR_WIDTH - 5, 40, this.height - 80, totalButtonHeight);
        this.addRenderableWidget(scrollBar);
    }

    private void initializeCommandButtons() {
        int xStart = (this.width - (4 * (BUTTON_WIDTH + BUTTON_PADDING) + SCROLLBAR_WIDTH)) / 2;
        int yStart = 50;

        int index = 0;
        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            int x = xStart + (index % 4) * (BUTTON_WIDTH + BUTTON_PADDING);
            int y = yStart + (index / 4) * (BUTTON_HEIGHT + BUTTON_PADDING);
            String command = entry.getKey();
            this.addRenderableWidget(Button.builder(Component.literal(command),
                            btn -> setShortcut(command))
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(Component.literal(entry.getValue())))
                    .build());
            index++;
        }
    }

    private void updateButtonPositions() {
        int yStart = 50;
        int index = 0;
        for (var widget : this.children()) {
            if (widget instanceof Button button && !button.getMessage().equals(CommonComponents.GUI_DONE)) {
                int y = yStart + (index / 4) * (BUTTON_HEIGHT + BUTTON_PADDING) - scrollBar.getScrollOffset();
                button.setY(y);
                button.visible = y + BUTTON_HEIGHT > 55 && y < this.height - 80;
                index++;
            }
        }
    }

    private void onSearchUpdated(String searchText) {
        filteredCommands = commandDescriptions.entrySet()
                .stream()
                .filter(entry -> entry.getKey().toLowerCase().contains(searchText.toLowerCase()))
                .toList();
        updateFilteredButtons();
    }

    private void updateFilteredButtons() {
        clearWidgets();
        this.addRenderableWidget(searchField);

        int xStart = (this.width - (4 * (BUTTON_WIDTH + BUTTON_PADDING) + SCROLLBAR_WIDTH)) / 2;
        int yStart = 50;

        int maxVisibleRows = (this.height - 80) / (BUTTON_HEIGHT + BUTTON_PADDING) - 1;
        int maxVisibleButtons = maxVisibleRows * 4;

        int index = 0;
        for (Map.Entry<String, String> entry : filteredCommands) {
            if (index >= maxVisibleButtons) break;

            int x = xStart + (index % 4) * (BUTTON_WIDTH + BUTTON_PADDING);
            int y = yStart + (index / 4) * (BUTTON_HEIGHT + BUTTON_PADDING);
            String command = entry.getKey();
            this.addRenderableWidget(Button.builder(Component.literal(command),
                            btn -> setShortcut(command))
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(Component.literal(entry.getValue())))
                    .build());
            index++;
        }

        this.addRenderableWidget(scrollBar);
    }

    private void renderTitleAndLabels(GuiGraphics pGuiGraphics) {
        pGuiGraphics.drawCenteredString(this.font, TITLE, this.width / 2, 6, 0xFFFFFF);
        int labelX = (this.width - (4 * (BUTTON_WIDTH + BUTTON_PADDING) + SCROLLBAR_WIDTH)) / 2;
        pGuiGraphics.drawString(this.font, "Search:", labelX + 5, 25, 0xFFFFFF);
    }


    private void setShortcut(String command) {
        WorldEditData.setShortcutCommand(shortcutIndex, command);
        onClose();
    }
}
