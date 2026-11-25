package com.tero.teromod;

import java.util.Iterator;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/**
 * Magnet (mining tools, 1 level): the drops of a block broken with the tool go straight into the
 * player's inventory. Whatever does not fit drops on the ground as usual.
 *
 * It works on the drops themselves, so it also covers extra blocks broken by Veinminer.
 */
@EventBusSubscriber(modid = TeroMod.MODID)
public class MagnetHandler {
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player)
                || ModEnchantments.level(event.getTool(), ModEnchantments.MAGNET) <= 0) {
            return;
        }

        boolean pickedUp = false;
        Iterator<ItemEntity> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemEntity drop = iterator.next();
            ItemStack stack = drop.getItem().copy();
            int before = stack.getCount();
            player.getInventory().add(stack);
            if (stack.getCount() == before) {
                continue; // nothing fit, the item drops normally
            }
            pickedUp = true;
            if (stack.isEmpty()) {
                iterator.remove();
            } else {
                drop.setItem(stack); // part of the stack did not fit
            }
        }

        if (pickedUp) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    1.4F + player.getRandom().nextFloat() * 0.4F);
        }
    }
}
