package com.tero.teromod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Absorption (armor, 2 levels per piece): every piece adds a chance to absorb a hit completely
 * (the hit does no damage at all). The chances of the four pieces add up:
 *
 * <pre>
 *   level I  : 12.5% per piece -> 1 piece 12.5%, 2 pieces 25%, 3 pieces 37.5%, full set 50%
 *   level II : 20%   per piece -> 1 piece 20%,   2 pieces 40%, 3 pieces 60%,   full set 80%
 * </pre>
 *
 * Mixed levels simply add up (for example a full set of three level II pieces and one level I piece
 * is 3 * 20% + 12.5% = 72.5%). It works on any damage except what bypasses even invulnerability
 * (the void and the /kill command), so the player can never become impossible to kill.
 *
 * Absorption only decides whether the hit happens. It does not touch effects, so Effect Protection
 * keeps working independently of it. A melee hit with a successful Imperception roll ignores
 * Absorption together with the rest of the armor.
 */
@EventBusSubscriber(modid = TeroMod.MODID)
public class AbsorptionHandler {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final float[] CHANCE_PER_PIECE = {0.0F, 0.125F, 0.20F};

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        if (ImperceptionHandler.ignoresArmor(event)) {
            return; // a successful Imperception hit ignores the armor and its Absorption
        }
        float chance = 0.0F;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            int level = ModEnchantments.level(entity.getItemBySlot(slot), ModEnchantments.ABSORPTION);
            chance += CHANCE_PER_PIECE[Math.min(level, CHANCE_PER_PIECE.length - 1)];
        }
        if (chance <= 0.0F || entity.getRandom().nextFloat() >= chance) {
            return;
        }

        event.setCanceled(true);
        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.6, entity.getZ(),
                    8, 0.3, 0.4, 0.3, 0.05);
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F,
                    1.2F + entity.getRandom().nextFloat() * 0.4F);
        }
    }
}
