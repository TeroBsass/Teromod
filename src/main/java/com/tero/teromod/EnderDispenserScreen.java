package com.tero.teromod;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EnderDispenserScreen extends AbstractContainerScreen<EnderDispenserMenu> {
    public EnderDispenserScreen(EnderDispenserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
