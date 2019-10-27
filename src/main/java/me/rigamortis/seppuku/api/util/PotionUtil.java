package me.rigamortis.seppuku.api.util;

import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public final class PotionUtil {

    public static String getFriendlyPotionName(PotionEffect potionEffect) {
        String effectName = I18n.format(potionEffect.getPotion().getName());
        if (potionEffect.getAmplifier() == 1) {
            effectName = effectName + " " + I18n.format("enchantment.level.2");
        } else if (potionEffect.getAmplifier() == 2) {
            effectName = effectName + " " + I18n.format("enchantment.level.3");
        } else if (potionEffect.getAmplifier() == 3) {
            effectName = effectName + " " + I18n.format("enchantment.level.4");
        }

        return effectName;
    }

    public static String getNameDurationString(PotionEffect potionEffect) {
        return String.format("%s (%s)", getFriendlyPotionName(potionEffect), Potion.getPotionDurationString(potionEffect, 1.0F));
    }
}
