package me.rigamortis.seppuku.api.util;

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
}
