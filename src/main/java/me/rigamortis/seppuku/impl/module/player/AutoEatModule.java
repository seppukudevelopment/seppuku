package me.rigamortis.seppuku.impl.module.player;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class AutoEatModule extends Module {

    public final Value<Float> hunger = new Value<Float>("Hunger", new String[]{"food", "h"}, "The amount of hunger needed to acquire some food", 9.0f, 0.0f, 20.0f, 0.5f);
    public final Value<Integer> forcedSlot = new Value<Integer>("Slot", new String[]{"s"}, "The hot-bar slot to put the food into (45 for offhand)", 43, 0, 43, 1);
    public final Value<Boolean> gapples = new Value<Boolean>("Gapples", new String[]{"gap", "gapple", "goldenapple", "goldenapples"}, "Allow eating golden apples for food", false);

    private int previousHeldItem = -1;
    private int foodSlot = -1;

    public AutoEatModule() {
        super("AutoEat", new String[]{"Eat", "AutoFeed"}, "Automatically swaps & eats food when hunger is below the set threshold", "NONE", -1, ModuleType.PLAYER);
    }

    @Override
    public String getMetaData() {
        return "" + this.getFoodCount();
    }

    @Listener
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null)
            return;

        if (mc.player.getFoodStats().getFoodLevel() < this.hunger.getValue()) {
            this.foodSlot = this.findFood();
        }

        if (this.foodSlot != -1) {
            if (this.forcedSlot.getValue() != 45) { // we aren't trying to put it in the offhand
                if (this.previousHeldItem == -1) {
                    this.previousHeldItem = mc.player.inventory.currentItem;
                }

                if (this.foodSlot < 36) {
                    mc.playerController.windowClick(0, this.forcedSlot.getValue(), 0, ClickType.QUICK_MOVE, mc.player); // last hot-bar slot
                    mc.playerController.windowClick(0, this.foodSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, this.forcedSlot.getValue(), 0, ClickType.PICKUP, mc.player);
                    mc.player.inventory.currentItem = this.forcedSlot.getValue() - 36;
                } else {
                    mc.player.inventory.currentItem = this.foodSlot - 36; // in the hot-bar, so remove the inventory offset
                }
            } else { // we need this notch apple in the offhand
                if (!(mc.player.getHeldItemOffhand().getItem() instanceof ItemFood)) {
                    mc.playerController.windowClick(0, 45, 0, ClickType.QUICK_MOVE, mc.player); // offhand slot
                    mc.playerController.windowClick(0, this.foodSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                }
            }

            if (mc.player.getFoodStats().getFoodLevel() >= this.hunger.getValue()) {
                mc.gameSettings.keyBindUseItem.pressed = false;
                if (this.previousHeldItem != -1) {
                    mc.player.inventory.currentItem = this.previousHeldItem;
                }
                this.foodSlot = -1;
                this.previousHeldItem = -1;
            } else {
                mc.displayGuiScreen(null);
                mc.gameSettings.keyBindUseItem.pressed = true;
            }
        }
    }

    private int findFood() {
        float bestSaturation = -1;
        int bestFoodSlot = -1;
        for (int slot = 44; slot > 8; slot--) {
            ItemStack itemStack = Minecraft.getMinecraft().player.inventoryContainer.getSlot(slot).getStack();
            if (itemStack.isEmpty())
                continue;

            if (this.isFoodItem(itemStack.getItem())) {
                float saturation = ((ItemFood) itemStack.getItem()).getSaturationModifier(itemStack);
                if (saturation > bestSaturation) {
                    bestSaturation = saturation;
                    bestFoodSlot = slot;
                }
            }
        }
        return bestFoodSlot;
    }

    private int getFoodCount() {
        int food = 0;

        if (Minecraft.getMinecraft().player == null)
            return food;

        for (int i = 0; i < 45; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && this.isFoodItem(stack.getItem())) {
                food += stack.getCount();
            }
        }

        return food;
    }

    private boolean isFoodItem(Item item) {
        if (!(item instanceof ItemFood))
            return false; // is not of ItemFood class

        if (this.gapples.getValue())
            return item == Items.GOLDEN_APPLE;

        return item != Items.GOLDEN_APPLE && item != Items.CHORUS_FRUIT && item != Items.ROTTEN_FLESH && item != Items.POISONOUS_POTATO && item != Items.SPIDER_EYE;
    }
}
