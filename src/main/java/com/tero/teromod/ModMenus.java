package com.tero.teromod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TeroMod.MODID);

    public static final Supplier<MenuType<RailCreatorMenu>> RAIL_CREATOR = MENUS.register(
            "rail_creator", () -> IMenuTypeExtension.create(RailCreatorMenu::new));
}
