package me.rigamortis.seppuku.api.util;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * @author noil
 */

public final class InventoryUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean hasItem(Item input) {
        for (int i = 0; i < 36; i++) {
            final Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == input) {
                return true;
            }
        }

        return false;
    }

    public static int getItemCount(Item input) {
        int items = 0;
        for (int i = 0; i < 45; i++) {
            final Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == input) {
                items += 1;
            }
        }

        return items;
    }

    public static int getBlockCount(Block input) {
        int blocks = 0;
        for (int i = 0; i < 45; i++) {
            final ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() instanceof ItemBlock) {
                final ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                if (itemBlock.getBlock() == input) {
                    blocks += itemStack.getCount();
                }
            }
        }

        return blocks;
    }

    public static int getSlotForItem(Item input) {
        for (int i = 0; i < 36; i++) {
            final Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == input) {
                return i;
            }
        }
        return -1;
    }
}

