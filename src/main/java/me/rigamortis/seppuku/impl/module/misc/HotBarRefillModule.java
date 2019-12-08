package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;

/**
 * Automatically refills the players hotbar.
 *
 * @author Old Chum
 * @since 12/7/19
 */
public class HotBarRefillModule extends Module {
    public final Value<Float> delay = new Value<>("Delay", new String[]{"Del"}, "The amount of delay in milliseconds.", 50.0f);
    public final Value<Integer> refills = new Value<>("NumPerRefill", new String[]{"refill"}, "The number of total slots to be refilled at a time.", 2, 1, 10, 1);
    public final Value<Boolean> offHand = new Value<>("OffHand", new String[]{"oh", "off", "hand"}, "If the off hand should be refilled.", true);

    private Timer timer = new Timer();

    public HotBarRefillModule() {
        super("HotBarRefill", new String[]{"Replenish", "Refill", "AutoHotBar", "hbr"}, "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onWalkingUpdate (EventUpdateWalkingPlayer event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (this.timer.passed(this.delay.getValue())) {
            if (event.getStage() == EventStageable.EventStage.PRE) {
                if (mc.currentScreen instanceof GuiInventory) {
                    return;
                }

                ArrayList<Integer> toRefill = getRefillable(mc.player, getHotbar(mc.player));

                if (!toRefill.isEmpty()) {
                    for (int i = 0; i < toRefill.size() && i < refills.getValue(); i++) {
                        refillHotbarSlot(mc, toRefill.get(i));
                    }
                }
            }

            timer.reset();
        }
    }

    /**
     * Gets a list of slots in a hotbar that can be refilled.
     * If offhand is on the list includes the offhand
     *
     * @param hotbar The hotbar
     * @return A list of slots in a hotbar that can be refilled
     */
    private ArrayList<Integer> getRefillable(EntityPlayerSP player, ItemStack[] hotbar) {
        ArrayList<Integer> ret = new ArrayList<>();

        for (int i = 0; i < hotbar.length; i++) {
            ItemStack stack = hotbar[i];
            if (stack.getCount() < stack.getMaxStackSize() && stack.getItem() != Items.AIR) {
                ret.add(i);
            }
        }

        if (offHand.getValue()) {
            if (player.getHeldItemOffhand().getCount() < player.getHeldItemOffhand().getMaxStackSize()) {
                ret.add(45);
            }
        }

        return ret;
    }

    /**
     * Returns a given player's hotbar.
     *
     * @param player The player who's hotbar should be returned.
     * @return The passed player's hotbar as a primitive array.
     */
    private ItemStack[] getHotbar (EntityPlayerSP player) {
        ItemStack[] ret = new ItemStack[9];

        if (player != null) {
            for (int i = 0; i <= 8; i++) {
                    ret[i] = player.inventory.mainInventory.get(i);
            }
        }

        return ret;
    }

    /**
     * Retrieves the largest stack of a given ItemStack's Item in the player's inventory.
     *
     * @param player The player
     * @param item The item type that should be found
     * @param includeHotbar If the search should include the hobar
     * @return The index of the largest stack of the given item, -1 if the given item does not exist.
     */
    public int getBiggestStack (EntityPlayerSP player, ItemStack item, boolean includeHotbar) {
        if (item == null) {
            return -1;
        }

        int maxCount = -1;
        int maxIndex = -1;
        for (int i = includeHotbar ? 0 : 9; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);

            if (stack.getItem() != Items.AIR
                && stack.getItem() == item.getItem()
                && stack.getCount() > maxCount) {

                maxCount = stack.getCount();
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    /**
     * Refills a given slot in the hotbar from an item in the player's inventory.
     * Uses the slot's current ItemStack to decide what it should be refilled with.
     *
     * @param mc The passed Mincraft instance
     * @param slot The slot that should be refilled
     */
    public void refillHotbarSlot(Minecraft mc, int slot) {
        ItemStack stack;
        if (slot == 45) { // Special case for offhand
            stack = mc.player.getHeldItemOffhand();
        } else {
            stack = mc.player.inventory.mainInventory.get(slot);
        }

        // If the slot is air it cant be refilled
        if (stack.getItem() == Items.AIR) {
            return;
        }

        // The slot can't be refilled if there is nothing to refill it with
        int biggestStack = getBiggestStack(mc.player, stack, false);
        if (biggestStack == -1) {
            return;
        }

        // Special case for offhand (can't use QUICK_CLICK)
        if (slot == 45) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, biggestStack, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, biggestStack, 0, ClickType.PICKUP, mc.player);
            return;
        }

        int overflow = -1; // The slot a shift click will overflow to
        for (int i = 0; i < 9 && overflow == -1; i++) {
            if (mc.player.inventory.mainInventory.get(i).getItem() == Items.AIR) {
                overflow = i;
            }
        }


        if (overflow == -1) { // If the hotbar is full we can just shift click biggestStack to refill slot
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, biggestStack, 0, ClickType.QUICK_MOVE, mc.player);
        } else { // If the hotbar isn't full, we might have to click the overflow stack back into the inventory
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, biggestStack, 0, ClickType.QUICK_MOVE, mc.player);

            // If the two stacks don't overflow when combined we don't have to move overflow
            if (mc.player.inventory.mainInventory.get(overflow).getItem() != Items.AIR) {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, biggestStack, overflow, ClickType.SWAP, mc.player);
            }
        }
    }
}