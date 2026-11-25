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
 * any living target, a player or a mob. The armor, its toughness and the protection enchantments are
 * all ignored, as if the target stood there without armor. Effects (Resistance) and golden hearts
 * are not armor and still apply.
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
            // As if the target wore no armor at all: no armor/toughness reduction and no reduction from
            // the protection enchantments on that armor. Resistance and absorption hearts still apply.
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> 0.0F);
            event.addReductionModifier(DamageContainer.Reduction.ENCHANTMENTS, (container, reduction) -> 0.0F);
        }
    }
}
