package me.rigamortis.seppuku.api.util;

import net.minecraft.enchantment.Enchantment;

/**
 * created by noil on 10/13/2019 at 10:22 AM
 */
public final class ItemUtil {

    public static boolean isIllegalEnchant(Enchantment enc, short lvl) {
        final int maxPossibleLevel = enc.getMaxLevel();
        if (lvl == 0 || lvl > maxPossibleLevel)
            return true;

        return lvl == Short.MAX_VALUE;
    }
}
