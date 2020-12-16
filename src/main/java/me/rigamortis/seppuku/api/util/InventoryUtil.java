package me.rigamortis.seppuku.api.util;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;

/**
 * @author noil
 */

public final class InventoryUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean hasItem(Item input) {
        for (int i = 0; i < 36; i++) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == input) {
                return true;
            }
        }

        return false;
    }

    public static int getItemCount(Item input) {
        int items = 0;
        for (int i = 0; i < 45; i++) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == input) {
                items += 1;
            }
        }

        return items;
    }

    public static int getSlotForItem(Item input) {
        for (int i = 0; i < 36; i++) {
            Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == input) {
                return i;
            }
        }
        return -1;
    }
}

