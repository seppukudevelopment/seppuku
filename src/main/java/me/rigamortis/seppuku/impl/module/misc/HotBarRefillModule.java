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

/**
 * Automatically refills the players hot-bar.
 *
 * @author Old Chum
 * @since 12/7/19
 */
public class HotBarRefillModule extends Module {
    public final Value<Float> delay = new Value<>("Delay", new String[]{"Del"}, "The delay(ms) per item transfer to hot-bar.", 500.0f, 0.0f, 2000.0f, 1.0f);
    public final Value<Integer> percentage = new Value<>("RefillPercentage", new String[]{"percent", "p", "percent"}, "The percentage a slot should be filled to get refilled.", 50, 0, 100, 1);
    public final Value<Boolean> offHand = new Value<>("OffHand", new String[]{"oh", "off", "hand"}, "If the off hand should be refilled.", true);

    private final Timer timer = new Timer();

    public HotBarRefillModule() {
        super("HotBarRefill", new String[]{"Replenish", "Refill", "AutoHotBar", "hbr", "Restock", "HBRestock", "HBRefill", "Hotbar", "Hot-bar"}, "NONE", -1, ModuleType.MISC);
        this.setDesc("Automatically refills the players hot-bar.");
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (this.timer.passed(this.delay.getValue())) {
            if (event.getStage() == EventStageable.EventStage.PRE) {
                Minecraft mc = Minecraft.getMinecraft();

                if (mc.currentScreen instanceof GuiInventory) {
                    return;
                }

                int toRefill = getRefillable(mc.player);
                if (toRefill != -1) {
                    refillHotbarSlot(mc, toRefill);
                }
            }

            timer.reset();
        }
    }

    /**
     * Checks all items in the hotbar that can be refilled
     * If offhand is on, it is checked first
     *
     * @param player The player
     * @return The index of the first item to be refilled, -1 if there are no refillable items
     */
    private int getRefillable(EntityPlayerSP player) {
        if (offHand.getValue()) {
            if (player.getHeldItemOffhand().getItem() != Items.AIR
                    && player.getHeldItemOffhand().getCount() < player.getHeldItemOffhand().getMaxStackSize()
                    && (double) player.getHeldItemOffhand().getCount() / player.getHeldItemOffhand().getMaxStackSize() <= (percentage.getValue() / 100.0)) {
                return 45;
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);
            if (stack.getItem() != Items.AIR && stack.getCount() < stack.getMaxStackSize()
                    && (double) stack.getCount() / stack.getMaxStackSize() <= (percentage.getValue() / 100.0)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Searches the player's inventory for the smallest stack.
     * Gets the smallest stack so that there are not a a bunch
     * of partially full stacks left in the player's inventory.
     *
     * @param player    The player
     * @param itemStack The item type that should be found
     * @return The index of the smallest stack of the given item, -1 if the given item does not exist
     */
    private int getSmallestStack(EntityPlayerSP player, ItemStack itemStack) {
        if (itemStack == null) {
            return -1;
        }

        int minCount = itemStack.getMaxStackSize() + 1;
        int minIndex = -1;

        // i starts at 9 so that the hotbar is not checked
        for (int i = 9; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);

            if (stack.getItem() != Items.AIR
                    && stack.getItem() == itemStack.getItem()
                    && stack.getCount() < minCount) {

                minCount = stack.getCount();
                minIndex = i;
            }
        }

        return minIndex;
    }

    /**
     * Refills a given slot in the hotbar from an item in the player's inventory.
     * Uses the slot's current ItemStack to decide what it should be refilled with.
     *
     * @param mc   The Mincraft instance
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
        int biggestStack = getSmallestStack(mc.player, stack);
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

        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, biggestStack, 0, ClickType.QUICK_MOVE, mc.player);

        // If the two stacks don't overflow when combined we don't have to move overflow
        if (overflow != -1 && mc.player.inventory.mainInventory.get(overflow).getItem() != Items.AIR) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, biggestStack, overflow, ClickType.SWAP, mc.player);
        }
    }
}
