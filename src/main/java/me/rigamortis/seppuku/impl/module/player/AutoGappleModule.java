package me.rigamortis.seppuku.impl.module.player;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.world.EventLoadWorld;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import me.rigamortis.seppuku.impl.module.combat.AutoTotemModule;
import me.rigamortis.seppuku.impl.module.combat.MultitaskModule;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class AutoGappleModule extends Module {

    public final Value<Float> health = new Value<Float>("Health", new String[]{"Hp", "h"}, "The amount of health needed to acquire a notch apple.", 15.0f, 0.0f, 20.0f, 0.5f);
    public final Value<Integer> forcedSlot = new Value<Integer>("Slot", new String[]{"s"}, "The hot-bar slot to put the notch apple into. (45 for offhand)", 44, 0, 44, 1);
    public final Value<Boolean> enchantedOnly = new Value<Boolean>("EnchantedOnly", new String[]{"Enchanted", "Enchant", "EnchantOnly", "Notch", "NotchOnly", "NO", "EO"}, "Only allow enchanted golden apples to be used.", true);

    private int previousHeldItem = -1;
    private int notchAppleSlot = -1;
    private boolean activeMainHand;
    private boolean activeOffHand;

    private AutoTotemModule autoTotemModule;
    private MultitaskModule multitaskModule;

    public AutoGappleModule() {
        super("AutoGapple", new String[]{"Gapple", "AutoApple"}, "Automatically swaps & eats a (notch) apple when health is below the set threshold.", "NONE", -1, ModuleType.PLAYER);
    }

    @Override
    public String getMetaData() {
        return "" + this.getNotchAppleCount();
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            this.autoTotemModule = (AutoTotemModule) Seppuku.INSTANCE.getModuleManager().find(AutoTotemModule.class);
            this.multitaskModule = (MultitaskModule) Seppuku.INSTANCE.getModuleManager().find(MultitaskModule.class);
        }
    }

    @Listener
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null)
            return;

        if (this.autoTotemModule != null) {
            if (this.autoTotemModule.isEnabled()) {
                if (this.autoTotemModule.getTotemCount() > 0) {
                    if (mc.player.getHealth() <= this.autoTotemModule.health.getValue() && !mc.player.getHeldItemOffhand().getItem().equals(Items.TOTEM_OF_UNDYING))
                        return;
                }
            }
        }

        if (mc.player.getHealth() < this.health.getValue() && mc.player.getAbsorptionAmount() == 0) {
            this.notchAppleSlot = this.findNotchApple();
        } else {
            this.setActiveMainHand(false);
            this.setActiveOffHand(false);
        }

        if (this.notchAppleSlot != -1) {
            if (this.forcedSlot.getValue() != 45) { // we aren't trying to put it in the offhand
                if (this.previousHeldItem == -1) {
                    this.previousHeldItem = mc.player.inventory.currentItem;
                }

                if (this.notchAppleSlot < 36) {
                    this.setActiveMainHand(true);
                    mc.playerController.windowClick(0, this.forcedSlot.getValue(), 0, ClickType.QUICK_MOVE, mc.player); // last hotbar slot
                    mc.playerController.windowClick(0, this.notchAppleSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, this.forcedSlot.getValue(), 0, ClickType.PICKUP, mc.player);
                    mc.player.inventory.currentItem = this.forcedSlot.getValue() - 36;
                } else {
                    this.setActiveMainHand(true);
                    mc.player.inventory.currentItem = this.notchAppleSlot - 36; // in the hotbar, so remove the inventory offset
                }
            } else { // we need this notch apple in the offhand
                if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {
                    this.setActiveOffHand(true);
                    mc.playerController.windowClick(0, 45, 0, ClickType.QUICK_MOVE, mc.player); // offhand slot
                    mc.playerController.windowClick(0, this.notchAppleSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                }
            }

            if (mc.player.getHealth() >= this.health.getValue() && mc.player.getAbsorptionAmount() > 0) {
                this.stop();
            } else {
                if (this.forcedSlot.getValue() != 45) {
                    this.setActiveMainHand(true);
                    if (this.multitaskModule != null) {
                        if (this.multitaskModule.isEnabled()) {
                            mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                        } else {
                            mc.gameSettings.keyBindUseItem.pressed = true;
                        }
                    }
                } else {
                    this.setActiveOffHand(true);
                    if (this.multitaskModule != null) {
                        if (this.multitaskModule.isEnabled()) {
                            mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
                        } else {
                            mc.gameSettings.keyBindUseItem.pressed = true;
                        }
                    }
                }

                if (this.multitaskModule == null) {
                    mc.gameSettings.keyBindUseItem.pressed = true;
                }
            }
        }
    }

    public void stop() {
        Minecraft.getMinecraft().gameSettings.keyBindUseItem.pressed = false;
        if (this.previousHeldItem != -1) {
            Minecraft.getMinecraft().player.inventory.currentItem = this.previousHeldItem;
        }
        this.notchAppleSlot = -1;
        this.previousHeldItem = -1;
        this.setActiveMainHand(false);
        this.setActiveOffHand(false);
    }

    private int findNotchApple() {
        for (int slot = 44; slot > 8; slot--) {
            ItemStack itemStack = Minecraft.getMinecraft().player.inventoryContainer.getSlot(slot).getStack();
            if (itemStack.isEmpty()) {
                continue;
            }

            if (this.enchantedOnly.getValue()) {
                if (itemStack.getItemDamage() == 0)
                    continue;
            }

            if (itemStack.getItem() == Items.GOLDEN_APPLE) {
                return slot;
            }
        }
        return -1;
    }

    private int getNotchAppleCount() {
        int gapples = 0;

        if (Minecraft.getMinecraft().player == null)
            return gapples;

        for (int i = 0; i < 45; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() != Items.GOLDEN_APPLE) {
                continue;
            }

            if (this.enchantedOnly.getValue()) {
                if (stack.getItemDamage() == 0)
                    continue;
            }

            gapples += stack.getCount();
        }

        return gapples;
    }

    public boolean isActiveMainHand() {
        return activeMainHand;
    }

    public void setActiveMainHand(boolean activeMainHand) {
        this.activeMainHand = activeMainHand;
    }

    public boolean isActiveOffHand() {
        return activeOffHand;
    }

    public void setActiveOffHand(boolean activeOffHand) {
        this.activeOffHand = activeOffHand;
    }
}
