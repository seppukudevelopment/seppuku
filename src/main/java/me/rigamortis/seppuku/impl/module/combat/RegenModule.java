package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.old.BooleanValue;
import me.rigamortis.seppuku.api.value.old.NumberValue;
import me.rigamortis.seppuku.api.value.old.OptionalValue;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/1/2019 @ 12:13 AM.
 */
public final class RegenModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Potion", "Gapple"});

    public final NumberValue health = new NumberValue("Health", new String[]{"Hp"}, 8.0f, Float.class, 0.0f, 20.0f, 0.5f);

    public final BooleanValue refill = new BooleanValue("Refill", new String[]{"ref"}, true);

    private int gappleSlot = -1;

    public RegenModule() {
        super("Regen", new String[]{"AutoHeal"}, "Automatically heals you once your health is low enough", "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Override
    public void onToggle() {
        super.onToggle();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            final ItemStack stack = mc.player.inventory.getCurrentItem();

            switch (this.mode.getInt()) {
                case 0:
                    break;
                case 1:
                    if (mc.player.getHealth() <= this.health.getFloat() && mc.player.getAbsorptionAmount() <= 0) {
                        gappleSlot = getItemHotbar(Items.GOLDEN_APPLE);
                    }

                    if (gappleSlot != -1) {
                        mc.player.inventory.currentItem = gappleSlot;
                        mc.playerController.updateController();

                        if (stack.getItem() != Items.AIR && stack.getItem() == Items.GOLDEN_APPLE) {
                            if (mc.currentScreen == null) {
                                mc.gameSettings.keyBindUseItem.pressed = true;
                            } else {
                                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                            }

                            if (mc.player.getAbsorptionAmount() > 0) {
                                mc.gameSettings.keyBindUseItem.pressed = false;
                                gappleSlot = -1;
                            }
                        }
                    }else{
                        if (mc.player.getHealth() <= this.health.getFloat() && mc.player.getAbsorptionAmount() <= 0) {
                            if (this.refill.getBoolean()) {
                                final int invSlot = findStackInventory(Items.GOLDEN_APPLE);
                                if (invSlot != -1) {
                                    final int empty = findEmptyhotbar();
                                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, invSlot, empty == -1 ? mc.player.inventory.currentItem : empty, ClickType.SWAP, mc.player);
                                    mc.playerController.updateController();
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    private int getItemHotbar(Item input) {
        for (int i = 0; i < 9; i++) {
            final Item item = Minecraft.getMinecraft().player.inventory.getStackInSlot(i).getItem();
            if (item == input) {
                return i;
            }
        }
        return -1;
    }

    private int findEmptyhotbar() {
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);

            if(stack.getItem() == Items.AIR) {
                return i;
            }
        }
        return -1;
    }

    private int findStackInventory(Item input) {
        for (int i = 9; i < 36; i++) {
            final Item item = Minecraft.getMinecraft().player.inventory.getStackInSlot(i).getItem();
            if (item == input) {
                return i;
            }
        }
        return -1;
    }

}
