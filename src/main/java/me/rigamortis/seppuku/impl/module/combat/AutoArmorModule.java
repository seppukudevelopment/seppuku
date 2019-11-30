package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.Timer;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/1/2019 @ 12:31 AM.
 */
public final class AutoArmorModule extends Module {

    public final Value<Float> delay = new Value("Delay", new String[]{"Del"}, "The amount of delay in milliseconds.", 50.0f, 0.0f, 1000.0f, 1.0f);
    public final Value<Boolean> curse = new Value("Curse", new String[]{"Curses"}, "Prevents you from equipping armor with cursed enchantments.", false);

    private Timer timer = new Timer();

    public AutoArmorModule() {
        super("AutoArmor", new String[]{"AutoArm", "AutoArmour"}, "Automatically equips armor", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen instanceof GuiInventory) {
                return;
            }

            final ItemStack helm = mc.player.inventoryContainer.getSlot(5).getStack();

            if (helm.getItem() == Items.AIR) {
                final int slot = this.findArmorSlot(EntityEquipmentSlot.HEAD);

                if (slot != -1) {
                    this.clickSlot(slot, 0, ClickType.QUICK_MOVE);
                }
            }

            final ItemStack chest = mc.player.inventoryContainer.getSlot(6).getStack();

            if (chest.getItem() == Items.AIR) {
                final int slot = this.findArmorSlot(EntityEquipmentSlot.CHEST);

                if (slot != -1) {
                    this.clickSlot(slot, 0, ClickType.QUICK_MOVE);
                }
            }

            final ItemStack legging = mc.player.inventoryContainer.getSlot(7).getStack();

            if (legging.getItem() == Items.AIR) {
                final int slot = this.findArmorSlot(EntityEquipmentSlot.LEGS);

                if (slot != -1) {
                    this.clickSlot(slot, 0, ClickType.QUICK_MOVE);
                }
            }

            final ItemStack feet = mc.player.inventoryContainer.getSlot(8).getStack();

            if (feet.getItem() == Items.AIR) {
                final int slot = this.findArmorSlot(EntityEquipmentSlot.FEET);

                if (slot != -1) {
                    this.clickSlot(slot, 0, ClickType.QUICK_MOVE);
                }
            }
        }
    }

    private void clickSlot(int slot, int mouse, ClickType type) {
        if (this.timer.passed(this.delay.getValue())) {
            Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.inventoryContainer.windowId, slot, mouse, type, Minecraft.getMinecraft().player);
            this.timer.reset();
        }
    }

    private int findArmorSlot(EntityEquipmentSlot type) {
        int slot = -1;
        float damage = 0;

        for (int i = 9; i < 45; i++) {
            final ItemStack s = Minecraft.getMinecraft().player.inventoryContainer.getSlot(i).getStack();
            if (s != null && s.getItem() != Items.AIR) {

                if (s.getItem() instanceof ItemArmor) {
                    final ItemArmor armor = (ItemArmor) s.getItem();
                    if (armor.armorType == type) {
                        final float currentDamage = (armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, s));

                        final boolean cursed = this.curse.getValue() ? (EnchantmentHelper.hasBindingCurse(s)) : false;

                        if (currentDamage > damage && !cursed) {
                            damage = currentDamage;
                            slot = i;
                        }
                    }
                }
            }
        }

        return slot;
    }

}
