package com.tero.teromod;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * All sound "recipes" of the mod in one place. Every effect is a few vanilla sounds layered on top of
 * each other with different volumes and pitches (plus a little random pitch variation), so it does not
 * sound like one repeated sample. To use a custom sound later, replace the SoundEvent in the matching
 * method here and nothing else has to change.
 */
public final class ModSounds {
    private ModSounds() {
    }

    // ---------------------------------------------------------------- helpers

    private static void play(Level level, BlockPos pos, SoundEvent sound, float volume, float pitch) {
        if (level.isClientSide()) {
            return;
        }
        level.playSound(null, pos, sound, SoundSource.BLOCKS, volume, pitch);
    }

    /** Pitch with a small random offset (+-spread/2) so repeated sounds feel alive. */
    private static float vary(Level level, float pitch, float spread) {
        return pitch + (level.getRandom().nextFloat() - 0.5F) * spread;
    }

    // ------------------------------------------------------------- Rail Creator

    /** Hatch-like mechanical sound when the menu opens. */
    public static void railCreatorOpen(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.IRON_TRAPDOOR_OPEN, 0.5F, vary(level, 1.3F, 0.1F));
        play(level, pos, SoundEvents.LEVER_CLICK, 0.3F, 0.8F);
    }

    public static void railCreatorClose(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, 0.5F, vary(level, 1.3F, 0.1F));
    }

    /** Small click when a valid recipe appears in the output slot. */
    public static void railCreatorReady(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, 0.25F, 1.6F);
    }

    /**
     * Crafting: iron-golem repair clink + hammer on an anvil + mechanism click + a bright chime.
     * The base pitch depends on the rail that was made (gold powered rails ring higher).
     */
    public static void railCreated(Level level, BlockPos pos, ItemStack result) {
        float base = railPitch(result);
        play(level, pos, SoundEvents.IRON_GOLEM_REPAIR, 0.7F, vary(level, base, 0.12F));
        play(level, pos, SoundEvents.ANVIL_USE, 0.3F, vary(level, 1.6F, 0.1F));
        play(level, pos, SoundEvents.LEVER_CLICK, 0.45F, vary(level, 0.9F, 0.1F));
        play(level, pos, SoundEvents.AMETHYST_BLOCK_CHIME, 0.5F, Math.min(2.0F, base + 0.4F));
    }

    private static float railPitch(ItemStack result) {
        if (result.is(Items.POWERED_RAIL)) {
            return 1.25F;
        }
        if (result.is(Items.DETECTOR_RAIL)) {
            return 1.1F;
        }
        if (result.is(Items.ACTIVATOR_RAIL)) {
            return 0.9F;
        }
        if (result.is(ModBlocks.PLAYER_DETECTOR_RAIL_ITEM.get())) {
            return 1.15F;
        }
        return 1.0F;
    }

    // --------------------------------------------------------------- Book Table

    public static void bookTableOpen(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.BOOK_PAGE_TURN, 0.7F, vary(level, 0.95F, 0.1F));
        play(level, pos, SoundEvents.AMETHYST_BLOCK_CHIME, 0.25F, 1.5F);
    }

    public static void bookTableClose(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.BOOK_PUT, 0.7F, vary(level, 1.0F, 0.1F));
    }

    /** Soft page rustle when a valid recipe appears in the output slot. */
    public static void bookTableReady(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.BOOK_PAGE_TURN, 0.35F, 1.5F);
    }

    /** Disenchanting: enchanting-table magic + a page turn + a low mystic chime + a short scrape. */
    public static void disenchant(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.ENCHANTMENT_TABLE_USE, 0.9F, 1.0F);
        play(level, pos, SoundEvents.BOOK_PAGE_TURN, 0.7F, vary(level, 0.85F, 0.1F));
        play(level, pos, SoundEvents.AMETHYST_BLOCK_CHIME, 0.55F, vary(level, 0.75F, 0.1F));
        play(level, pos, SoundEvents.GRINDSTONE_USE, 0.25F, 1.6F);
    }

    // ----------------------------------------------------------- Ender Dispenser

    public static void enderDispenserOpen(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.ENDER_CHEST_OPEN, 0.6F, vary(level, 1.0F, 0.1F));
    }

    public static void enderDispenserClose(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.ENDER_CHEST_CLOSE, 0.6F, vary(level, 1.0F, 0.1F));
    }

    /** Layered on top of the vanilla dispense sound. */
    public static void enderDispense(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.ENDERMAN_TELEPORT, 0.35F, vary(level, 1.5F, 0.2F));
        play(level, pos, SoundEvents.ENDER_EYE_LAUNCH, 0.25F, 1.6F);
    }

    // ------------------------------------------------------------------ Stopper

    /** A heavy clamp: anvil clang + piston + iron hatch slam. */
    public static void stopperCatch(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.ANVIL_LAND, 0.45F, vary(level, 1.25F, 0.1F));
        play(level, pos, SoundEvents.PISTON_CONTRACT, 0.5F, vary(level, 1.4F, 0.1F));
        play(level, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, 0.5F, 0.8F);
    }

    /** The caught minecart goes into a dispenser; Ender Dispensers get a teleport whoosh. */
    public static void cartStored(Level level, BlockPos dispenserPos, boolean ender) {
        if (ender) {
            play(level, dispenserPos, SoundEvents.ENDERMAN_TELEPORT, 0.5F, vary(level, 1.3F, 0.15F));
            play(level, dispenserPos, SoundEvents.ENDER_CHEST_CLOSE, 0.4F, 1.2F);
        } else {
            play(level, dispenserPos, SoundEvents.ITEM_PICKUP, 0.45F, vary(level, 0.9F, 0.1F));
        }
    }

    // ------------------------------------------------------ Player Detector Rail

    /** Pressure-plate style click when a player's minecart arrives (on) or leaves (off). */
    public static void detectorRail(Level level, BlockPos pos, boolean on) {
        if (on) {
            play(level, pos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, 0.35F, 1.0F);
        } else {
            play(level, pos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, 0.3F, 1.0F);
        }
    }
}
