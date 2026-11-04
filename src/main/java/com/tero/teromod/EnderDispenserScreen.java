package com.tero.teromod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public class EnderDispenserScreen extends AbstractContainerScreen<EnderDispenserMenu> {
    public EnderDispenserScreen(EnderDispenserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                  float partialTick) {
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth,
                this.topPos + this.imageHeight, 0xFF17191D);
        graphics.fill(this.leftPos + 1, this.topPos + 1, this.leftPos + this.imageWidth - 1,
                this.topPos + this.imageHeight - 1, 0xFF777B82);
        graphics.fill(this.leftPos + 4, this.topPos + 4, this.leftPos + this.imageWidth - 4,
                this.topPos + 74, 0xFF3B3E44);
        graphics.fill(this.leftPos + 4, this.topPos + 78, this.leftPos + this.imageWidth - 4,
                this.topPos + this.imageHeight - 4, 0xFF3B3E44);
    }
}
