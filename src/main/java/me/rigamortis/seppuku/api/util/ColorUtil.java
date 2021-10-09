package me.rigamortis.seppuku.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;

/**
 * created by noil on 9/22/2019 at 1:22 PM
 */
public final class ColorUtil {

    public static int changeAlpha(int origColor, int userInputedAlpha) {
        origColor = origColor & 0x00FFFFFF;
        return (userInputedAlpha << 24) | origColor;
    }

    public static int getRandomColor() {
        String[] letters = "0123456789ABCDEF".split("");
        StringBuilder color = new StringBuilder();
        for (int i = 0; i < 6; i++)
            color.append(letters[(int) Math.round(Math.random() * 15)]);

        return Integer.parseInt(color.toString(), 16);
    }

    public static String toHex(Color color) {
        String hex = String.format("#%02x%02x%02x%02x", color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
        return hex;
    }

    public static int getHealthColor(Entity entity) {
        int scale = (int) Math.round(255.0 - (double) ((EntityLivingBase) entity).getHealth() * 255.0 / (double) ((EntityLivingBase) entity).getMaxHealth());
        int damageColor = 255 - scale << 8 | scale << 16;

        return (255 << 24) | damageColor;
    }
}
