package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.module.player.AutoGappleModule;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/30/2019 @ 3:37 AM.
 */
public final class AutoTotemModule extends Module {

    public final Value<Float> health = new Value<>("Health", new String[]{"Hp", "h"}, "The amount of health needed to acquire a totem.", 7.0f, 0.0f, 20.0f, 0.5f);
    public final Value<Boolean> crystals = new Value<>("Crystals", new String[]{"cry", "c"}, "Go back to crystals in offhand after health is replenished.", false);
    //public final Value<Boolean> force = new Value<>("Force", new String[]{"f"}, "Prioritize AutoTotem over AutoGapple, etc.", true);
    public final Value<Boolean> checkScreen = new Value<>("CheckScreen", new String[]{"screen", "check", "cs"}, "Checks if a screen is not opened to begin (usually disabled).", false);

    private AutoGappleModule autoGappleModule;

    public AutoTotemModule() {
        super("AutoTotem", new String[]{"Totem"}, "Automatically places a totem of undying in your offhand", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            this.autoGappleModule = (AutoGappleModule) Seppuku.INSTANCE.getModuleManager().find(AutoGappleModule.class);
        }
    }

    @Override
    public String getMetaData() {
        return "" + this.getTotemCount();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (this.checkScreen.getValue()) {
            if (mc.currentScreen != null)
                return;
        }

        final ItemStack offHand = mc.player.getHeldItemOffhand();

        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (mc.player.getHealth() <= this.health.getValue()) {
                if (offHand.getItem() == Items.TOTEM_OF_UNDYING) {
                    return;
                }

                final int totemSlot = this.getTotemSlot();

                if (totemSlot != -1) {
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, totemSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, totemSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.updateController();
                }
            }
        } else {
            if (mc.player.getHealth() > this.health.getValue() && this.crystals.getValue()) {
                if (this.autoGappleModule != null) {
                    if (this.autoGappleModule.isEnabled()) {
                        if (this.autoGappleModule.isActiveOffHand()) {
                            return;
                        }
                    }
                }

                if (offHand.getItem() == Items.END_CRYSTAL) {
                    return;
                }

                final int crystalSlot = this.getCrystalSlot();

                if (crystalSlot != -1) {
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, crystalSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, crystalSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.updateController();
                }
            }
        }
    }

    private int getCrystalSlot() {
        for (int i = 0; i < 36; i++) {
            final Item item = Minecraft.getMinecraft().player.inventory.getStackInSlot(i).getItem();
            if (item == Items.END_CRYSTAL) {
                if (i < 9) {
                    i += 36;
                }
                return i;
            }
        }
        return -1;
    }

    private int getTotemSlot() {
        for (int i = 0; i < 36; i++) {
            final Item item = Minecraft.getMinecraft().player.inventory.getStackInSlot(i).getItem();
            if (item == Items.TOTEM_OF_UNDYING) {
                if (i < 9) {
                    i += 36;
                }
                return i;
            }
        }
        return -1;
    }

    public int getTotemCount() {
        int totems = 0;

        if (Minecraft.getMinecraft().player == null)
            return totems;

        for (int i = 0; i < 45; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                totems += stack.getCount();
            }
        }

        return totems;
    }

}
