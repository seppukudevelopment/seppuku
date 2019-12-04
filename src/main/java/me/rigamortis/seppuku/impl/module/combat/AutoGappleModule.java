package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author jvyden420
 * 9/4/2019 @ 3:51 PM.
 */

public final class AutoGappleModule extends Module {
    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The AutoGapple mode to use. Absorption is based off of how many absorption hearts you have, where as once will eat one gapple then return to whatever you were doing.", Mode.ONCE);
    public final Value<Integer> absorption = new Value<Integer>("AbsorptionThreshold", new String[]{"absorption", "a"}, "The threshold to eat a gapple in absorption mode. 1 = 0.5 hearts.", 8, 0, 16, 1);
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean eatingGapple = false;
    private int lastSlot = -1;
    private Timer timer = new Timer();
    public AutoGappleModule() {
        super("AutoGapple", new String[]{"AutoEat", "SilentGapple", "Gapple", "Eat", "EatGapple"}, "Automatically eats gapples for you", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        // wait 50ms because isHandActive() doesn't update immediately
        // should be optimised since there's no way in hell you're eating something within 50ms
        if(eatingGapple && !mc.player.isHandActive() && timer.passed(50)) {
            if(lastSlot != -1) mc.player.inventory.currentItem = lastSlot;
            eatingGapple = false;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            if (mode.getValue() == Mode.ONCE) this.toggle();
            return;
        }
        if(eatingGapple) return;
        // Max: 16
        if(mode.getValue() == Mode.ONCE || (mode.getValue() == Mode.ABSORPTION && mc.player.getAbsorptionAmount() < absorption.getValue())) {
            lastSlot = mc.player.inventory.currentItem;
            final Item gapple = Item.getByNameOrId("GOLDEN_APPLE");
            final int slot = findStackHotbar(new ItemStack(gapple));
            if(slot != -1) {
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();
            }
            if (hasStack(gapple)) {
                eatingGapple = true;
                // TODO: do this through a packet so we can do other things while we eat, possibly?
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                timer.reset();
            }
        }
    }

    private int findStackHotbar(ItemStack type) {
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if(stack.getItem() == Items.GOLDEN_APPLE) return i;
        }
        return -1;
    }

    private boolean slotEqualsBlock (int slot, Block type) {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    private boolean hasStack(Item type) {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof Item) {
            final Item item = (Item) mc.player.inventory.getCurrentItem().getItem();
            return item == type;
        }
        return false;
    }

    private enum Mode {
        ONCE, ABSORPTION
    }
}
