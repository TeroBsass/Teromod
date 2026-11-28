package com.tero.teromod;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Makes a chestplate that was upgraded with elytra recognisable: a small wings badge in the corner of its
 * icon and an extra line in the tooltip. Nothing about the item itself changes, so the icon, the trim and
 * the dye stay exactly as they were.
 */
@EventBusSubscriber(modid = TeroMod.MODID, value = Dist.CLIENT)
public class ElytraChestplateClient {
    private static final Identifier BADGE = Identifier.fromNamespaceAndPath(TeroMod.MODID, "textures/gui/elytra_badge.png");
    private static final int BADGE_SIZE = 8;

    /** A chestplate (or other chest piece) that glides, but is not the elytra item itself. */
    private static boolean hasElytra(ItemStack stack) {
        return !stack.isEmpty() && !stack.is(Items.ELYTRA) && stack.has(DataComponents.GLIDER);
    }

    @SubscribeEvent
    public static void registerDecorations(RegisterItemDecorationsEvent event) {
        IItemDecorator badge = (graphics, font, stack, x, y) -> {
            if (!hasElytra(stack)) {
                return false;
            }
            // Top right corner of the 16x16 slot icon.
            graphics.blit(RenderPipelines.GUI_TEXTURED, BADGE, x + 16 - BADGE_SIZE + 1, y - 1,
                    0, 0, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE);
            return true;
        };
        // The item components are not bound yet while this event fires, so the items cannot be filtered
        // here. The decorator is registered for every item and decides per stack (hasElytra) instead.
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.ELYTRA) {
                event.register(item, badge);
            }
        }
    }

    @SubscribeEvent
    public static void addTooltip(ItemTooltipEvent event) {
        if (hasElytra(event.getItemStack())) {
            event.getToolTip().add(1, Component.translatable("item.teromod.elytra_upgrade").withStyle(ChatFormatting.AQUA));
        }
    }
}
