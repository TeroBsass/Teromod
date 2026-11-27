package com.tero.teromod;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Imperception (sword, 3 levels): a melee hit has a 25% / 50% / 75% chance to ignore the target's
 * armor completely, i.e. to deal the damage the sword would deal to an unarmored target. It works on
 * any living target, a player or a mob.
 *
 * One roll per hit decides everything at once. When it succeeds, the target's armor, its toughness,
 * the protection enchantments on it and the Absorption enchantment (see {@link AbsorptionHandler})
 * are all ignored. Effects (Resistance) and golden hearts are not armor and still apply.
 */
@EventBusSubscriber(modid = TeroMod.MODID)
public class ImperceptionHandler {
    private static final float[] CHANCE = {0.0F, 0.25F, 0.50F, 0.75F};

    private static LivingIncomingDamageEvent lastEvent;
    private static boolean lastResult;

    /**
     * Whether this hit ignores the target's armor. The roll is made once per event and shared with
     * {@link AbsorptionHandler}, so it does not matter which handler runs first.
     */
    public static synchronized boolean ignoresArmor(LivingIncomingDamageEvent event) {
        if (event != lastEvent) {
            lastEvent = event;
            lastResult = roll(event);
        }
        return lastResult;
    }

    private static boolean roll(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) {
            return false; // only direct melee hits, not arrows, potions or other indirect damage
        }
        int level = ModEnchantments.level(attacker.getMainHandItem(), ModEnchantments.IMPERCEPTION);
        if (level <= 0) {
            return false;
        }
        return attacker.getRandom().nextFloat() < CHANCE[Math.min(level, CHANCE.length - 1)];
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (ignoresArmor(event)) {
            // As if the target wore no armor at all: no armor/toughness reduction and no reduction from
            // the protection enchantments on that armor.
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> 0.0F);
            event.addReductionModifier(DamageContainer.Reduction.ENCHANTMENTS, (container, reduction) -> 0.0F);
        }
    }
}
