package com.tero.teromod;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * Effect Protection (chestplate, 3 levels): harmful effects, however they were applied (a hit, a potion,
 * a splash or lingering cloud, a command ...), are shorter on level 1 (-50%) and level 2 (-75%),
 * and are not applied at all on level 3.
 *
 * This works on the effect itself and never looks at damage, so it is independent of Absorption: an
 * absorbed hit simply never reaches the effect, and an effect that is not absorbed is still reduced.
 */
@EventBusSubscriber(modid = TeroMod.MODID)
public class EffectProtectionHandler {
    /** True while we re-apply a shortened effect, so that our own call is not processed again. */
    private static boolean reapplying = false;

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance instance = event.getEffectInstance();
        if (reapplying || entity.level().isClientSide() || instance == null) {
            return;
        }
        if (instance.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) {
            return;
        }
        int level = ModEnchantments.level(entity.getItemBySlot(EquipmentSlot.CHEST), ModEnchantments.EFFECT_PROTECTION);
        if (level <= 0) {
            return;
        }
        if (level >= 3) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            return;
        }
        if (instance.isInfiniteDuration()) {
            // There is nothing to shorten; leave such effects (commands, special mobs) alone.
            return;
        }

        float remaining = level == 1 ? 0.5F : 0.25F;
        int duration = Math.max(1, (int) (instance.getDuration() * remaining));
        MobEffectInstance shortened = new MobEffectInstance(instance.getEffect(), duration, instance.getAmplifier(),
                instance.isAmbient(), instance.isVisible(), instance.showIcon());

        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        reapplying = true;
        try {
            entity.addEffect(shortened);
        } finally {
            reapplying = false;
        }
    }
}
