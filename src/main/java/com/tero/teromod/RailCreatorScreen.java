package com.tero.teromod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class RailCreatorScreen extends AbstractContainerScreen<RailCreatorMenu> {
    private static final Identifier GUI_TEXTURE =
            Identifier.fromNamespaceAndPath(TeroMod.MODID, "textures/block/rail_creator_gui.png");
    private static final int GUI_TEXTURE_WIDTH = 1292;
    private static final int GUI_TEXTURE_HEIGHT = 1218;

    public RailCreatorScreen(RailCreatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
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
                GUI_TEXTURE_HEIGHT
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
