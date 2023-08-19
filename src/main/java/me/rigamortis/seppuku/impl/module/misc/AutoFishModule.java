package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author Seth 2019
 * @author uoil 2023
 */
public final class AutoFishModule extends Module {

    public final Value<Boolean> durability = new Value<>("Durability", new String[]{"dura", "durabilitycheck", "d"}, "Saves the current rod before it breaks", true);
    public final Value<Integer> durabilityLimit = new Value<>("DurabilityLimit", new String[]{"duralimit", "limit", "dl"}, "Minimum durability to stop using a rod", 5, 1, 10, 1);
    public final Value<Boolean> swap = new Value<>("Swap", new String[]{"autoswap", "newrod", "s"}, "If the current rod is almost broken, swaps to a new rod", true);
    public final Value<Integer> swapSlot = new Value<Integer>("Slot", new String[]{"s"}, "The hot-bar slot to put the fishing rod into (45 for offhand)", 43, 0, 45, 1);

    public AutoFishModule() {
        super("AutoFish", new String[]{"AutomaticFish"}, "Automatically catches fish and recasts", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketSoundEffect) {
                final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                if (packet.getCategory() == SoundCategory.NEUTRAL && packet.getSound() == SoundEvents.ENTITY_BOBBER_SPLASH) {
                    final Minecraft mc = Minecraft.getMinecraft();
                    final ItemStack itemStackInMainHand = mc.player.getHeldItemMainhand();
                    final ItemStack itemStackInOffHand = mc.player.getHeldItemOffhand();
                    final boolean holdingFishingRodMain = itemStackInMainHand.getItem() instanceof ItemFishingRod;
                    final boolean holdingFishingRodOff = itemStackInOffHand.getItem() instanceof ItemFishingRod;
                    final boolean holdingFishingRod = holdingFishingRodMain || holdingFishingRodOff;
                    final EnumHand rodHand = holdingFishingRodMain ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
                    final int damageMainHand = itemStackInMainHand.getMaxDamage() - itemStackInMainHand.getItemDamage();
                    final int damageOffHand = itemStackInOffHand.getMaxDamage() - itemStackInOffHand.getItemDamage();
                    if (holdingFishingRod) {
                        if ((holdingFishingRodMain && (damageMainHand) <= this.durabilityLimit.getValue() + 1)
                                || (holdingFishingRodOff && (damageOffHand) <= this.durabilityLimit.getValue() + 1)) {
                            if (this.swap.getValue()) {
                                this.doSwap(mc.player, mc.playerController);
                                this.catchAndThrow(mc.player, rodHand);
                                return;
                            }
                            if (this.durability.getValue()) { // check durability before re-throw
                                return;
                            }
                        }

                        this.catchAndThrow(mc.player, rodHand);
                    }
                }
            }
        }
    }

    private void catchAndThrow(final EntityPlayerSP player, final EnumHand rodHand) {
        player.connection.sendPacket(new CPacketPlayerTryUseItem(rodHand));
        player.swingArm(rodHand);
        player.connection.sendPacket(new CPacketPlayerTryUseItem(rodHand));
        player.swingArm(rodHand);
    }

    private void doSwap(final EntityPlayerSP player, final PlayerControllerMP playerController) {
        int bestDurability = -1;
        int bestSlot = -1;
        for (int slot = 44; slot > 8; slot--) {
            ItemStack itemStack = Minecraft.getMinecraft().player.inventoryContainer.getSlot(slot).getStack();
            if (itemStack.isEmpty())
                continue;

            int dura = itemStack.getMaxDamage() - itemStack.getItemDamage();
            if (itemStack.getItem() instanceof ItemFishingRod && dura > this.durabilityLimit.getValue()) {
                if (dura > bestDurability) {
                    bestDurability = dura;
                    bestSlot = slot;
                }
            }
        }

        if (bestSlot != -1) {
            if (this.swapSlot.getValue() != 45) {
                if (bestSlot < 36) {
                    playerController.windowClick(0, this.swapSlot.getValue(), 0, ClickType.QUICK_MOVE, player); // last hot-bar slot
                    playerController.windowClick(0, bestSlot, 0, ClickType.PICKUP, player);
                    playerController.windowClick(0, this.swapSlot.getValue(), 0, ClickType.PICKUP, player);
                    player.inventory.currentItem = this.swapSlot.getValue() - 36;
                } else {
                    player.inventory.currentItem = bestSlot - 36; // in the hot-bar, so remove the inventory offset
                }
            } // we need this rod in the offhand
            else if (!(player.getHeldItemOffhand().getItem() instanceof ItemFishingRod)) {
                playerController.windowClick(0, 45, 0, ClickType.QUICK_MOVE, player); // offhand slot
                playerController.windowClick(0, bestSlot, 0, ClickType.PICKUP, player);
                playerController.windowClick(0, 45, 0, ClickType.PICKUP, player);
            }
        }

    }
}
