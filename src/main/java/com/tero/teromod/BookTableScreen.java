package com.tero.teromod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public class BookTableScreen extends AbstractContainerScreen<BookTableMenu> {
    private static final Identifier GUI_TEXTURE =
            Identifier.fromNamespaceAndPath(TeroMod.MODID, "textures/gui/book_table_gui.png");
    private static final int GUI_TEXTURE_WIDTH = 1126;
    private static final int GUI_TEXTURE_HEIGHT = 966;

    public BookTableScreen(BookTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 151);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                GUI_TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                GUI_TEXTURE_WIDTH,
                GUI_TEXTURE_HEIGHT,
                GUI_TEXTURE_WIDTH,
                GUI_TEXTURE_HEIGHT
        );
    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }
}
