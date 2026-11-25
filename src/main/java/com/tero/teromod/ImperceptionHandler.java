package com.tero.teromod;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Imperception (sword, 3 levels): a melee hit has a 25% / 50% / 75% chance to ignore the target's
 * armor completely, i.e. to deal the damage the sword would deal to an unarmored target. It works on
 * any living target, a player or a mob. Other protection (Protection enchantments, resistance, ...)
 * still applies; only the armor and armor toughness reduction is dropped.
 */
@EventBusSubscriber(modid = TeroMod.MODID)
public class ImperceptionHandler {
    private static final float[] CHANCE = {0.0F, 0.25F, 0.50F, 0.75F};

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) {
            return; // only direct melee hits, not arrows, potions or other indirect damage
        }
        int level = ModEnchantments.level(attacker.getMainHandItem(), ModEnchantments.IMPERCEPTION);
        if (level <= 0) {
            return;
        }
        float chance = CHANCE[Math.min(level, CHANCE.length - 1)];
        if (attacker.getRandom().nextFloat() < chance) {
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> 0.0F);
        }
    }
}
