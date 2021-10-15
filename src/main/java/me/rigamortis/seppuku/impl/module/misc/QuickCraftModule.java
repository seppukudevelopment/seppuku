package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.server.SPacketSetSlot;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Shift clicks the crafting result into the inventory.
 *
 * @author Old Chum
 * @since 10/18/20
 */
public final class QuickCraftModule extends Module {
    public QuickCraftModule() {
        super("QuickCraft", new String[]{"FastCraft", "qcraft"}, "Automatically collects the result when crafting", "NONE", -1, Module.ModuleType.MISC);
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketSetSlot) {
                // Check if this packet updates the recipe result and if the result is not empty
                if (((SPacketSetSlot) event.getPacket()).getSlot() == 0 && ((SPacketSetSlot) event.getPacket()).getStack().getItem() != Items.AIR) {
                    Minecraft mc = Minecraft.getMinecraft();

                    if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiCrafting) {
                        mc.playerController.windowClick(mc.player.openContainer.windowId, 0, 0, ClickType.QUICK_MOVE, mc.player);
                        mc.playerController.updateController();
                    }
                }
            }
        }
    }
}
